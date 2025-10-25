package me.keano.azurite.modules.events.payload;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.EventType;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Effect;

import java.util.*;
import java.util.stream.Collectors;
import me.keano.azurite.utils.Serializer;
import java.util.LinkedHashMap;
import org.bukkit.Color;
import org.bukkit.World;
// HolographicDisplays API
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 9/8/2025
 * Project: Zeus
 */

@Getter
@Setter
public class Payload extends Module<PayloadManager> {

    private Location start;
    private Location end;

    private final List<Location> path = new ArrayList<>();
    private Minecart cart;
    private BukkitRunnable task;

    private double progress = 0; // index in path
    private double speed = 0.1; // blocks per tick
    private double radius = 5; // distance players must be within

    private PlayerTeam controllingTeam;
    private boolean contesting; // true when multiple teams are in the aura
    private boolean returning; // true when heading back to start with no players around

    private long startTime;
    private String controllingName;
    private long controlStart;
    private final Map<String, Long> controlTimes = new HashMap<>();
    private long stoppedAt;
    private long stopDelayMillis = 10000L; // wait before returning

    private HCF hcf;

    public Payload(PayloadManager manager) {
        super(manager);
    }

    /**
     * Starts the payload event for the supplied duration in seconds.
     */
    private boolean auraEnabled = true;
    private String auraColorStr = "ORANGE";
    private double auraStepDegrees = 22.5D;
    private double auraYOffset = 0.1D;

    // Cache del color
    private Color auraColor = Color.fromRGB(255, 165, 0);

    private void loadAuraConfig() {
        this.auraEnabled = getConfig().getBoolean("PAYLOAD_EVENT.AURA.ENABLED", true);
        this.auraColorStr = getConfig().getString("PAYLOAD_EVENT.AURA.COLOR", "ORANGE");
        this.auraStepDegrees = getConfig().getDouble("PAYLOAD_EVENT.AURA.STEP_DEGREES", 22.5D);
        if (this.auraStepDegrees <= 0) this.auraStepDegrees = 22.5D;
        this.auraYOffset = getConfig().getDouble("PAYLOAD_EVENT.AURA.Y_OFFSET", 0.1D);

        this.auraColor = parseColor(this.auraColorStr, Color.fromRGB(255, 165, 0)); // ORANGE fallback
    }

    // ------------------------
    // Hologram configuration
    // ------------------------
    private boolean holoEnabled = true;
    private double holoOffsetY = 2.1D;
    private double holoLineSpacing = 0.25D; // not used with HD, kept for future
    private List<String> holoLines = new ArrayList<>();
    private Hologram hologram;

    private void loadHologramConfig() {
        this.holoEnabled = getConfig().getBoolean("PAYLOAD_EVENT.HOLOGRAM.ENABLED", true);
        this.holoOffsetY = getConfig().getDouble("PAYLOAD_EVENT.HOLOGRAM.OFFSET_Y", 2.1D);
        this.holoLineSpacing = getConfig().getDouble("PAYLOAD_EVENT.HOLOGRAM.LINE_SPACING", 0.25D);
        List<String> cfg = getConfig().getStringList("PAYLOAD_EVENT.HOLOGRAM.LINES");
        if (cfg == null || cfg.isEmpty());
        this.holoLines = new ArrayList<>(cfg);
    }

    public void start() {
        if (start == null || path.isEmpty()) return;

        loadAuraConfig();
        loadHologramConfig();

        if (cart != null) {
            cart.remove();
        }

        EntityType entityType = EntityType.valueOf(
                getConfig().getString("PAYLOAD_EVENT.ENTITY_TYPE", "MINECART_TNT")
        );

        cart = (Minecart) start.getWorld().spawnEntity(start.clone().add(0.5, 0, 0.5), entityType);
        cart.setMetadata("payloadCart", new FixedMetadataValue(getInstance(), true));
        try { cart.setMaxSpeed(1.0D); } catch (Throwable ignored) {}

        progress = 0;
        controllingTeam = null;
        contesting = false;
        returning = false;
        controllingName = null;
        controlTimes.clear();
        startTime = System.currentTimeMillis();
        controlStart = startTime;
        stoppedAt = 0L;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        task.runTaskTimer(getInstance(), 0L, 1L);

        // Spawn hologram after cart
        if (holoEnabled) spawnOrUpdateHologram();
    }


    /**
     * Stops the payload event.
     */
    public PayloadStats stop(String finisher) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (cart != null) {
            cart.remove();
            cart = null;
        }
        removeHologram();
        updateController(null);
        progress = 0;
        controllingTeam = null;
        contesting = false;
        returning = false;
        stoppedAt = 0L;
        String msg = finisher != null
                ? getLanguageConfig().getString("PAYLOAD.EVENT_FINISHED_BY").replace("%controller%", finisher)
                : getLanguageConfig().getString("PAYLOAD.EVENT_STOPPED_BROADCAST");
        Bukkit.broadcastMessage(msg);
        long duration = System.currentTimeMillis() - startTime;
        return new PayloadStats(new LinkedHashMap<>(controlTimes), finisher, duration);
    }

    /**
     * Teleports a player to the current payload location.
     */
    public void teleport(Player player) {
        if (cart == null) return;
        player.teleport(cart.getLocation());
    }

    private void updateController(String newController) {
        long now = System.currentTimeMillis();
        if (!Objects.equals(controllingName, newController)) {
            if (controllingName != null) {
                controlTimes.merge(controllingName, now - controlStart, Long::sum);
            }
            controllingName = newController;
            controlStart = now;
        }
    }

    /**
     * Moves the cart either forwards (dir = 1) or backwards (dir = -1).
     */
    private void move(int dir) {
        if (path == null || path.isEmpty()) return;

        progress += dir * speed;
        if (progress < 0) progress = 0;

        double maxIndex = Math.max(0, path.size() - 1);
        if (progress >= maxIndex) {
            progress = maxIndex;
            getManager().endPayload(controllingName);
            return;
        }

        int idx = (int) Math.floor(progress);
        double frac = progress - idx;

        Location a = path.get(idx);
        Location b = idx + 1 < path.size() ? path.get(idx + 1) : a;
        if (a == null || b == null || cart == null) return;

        if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
            // Fallback to node snapping across worlds
            cart.teleport(a.clone().add(0.5, 0, 0.5));
            return;
        }

        double x = a.getX() + (b.getX() - a.getX()) * frac + 0.5;
        double y = a.getY() + (b.getY() - a.getY()) * frac;
        double z = a.getZ() + (b.getZ() - a.getZ()) * frac + 0.5;
        Location interp = new Location(a.getWorld(), x, y, z, cart.getLocation().getYaw(), cart.getLocation().getPitch());
        cart.teleport(interp);
    }

    /**
     * Repeated task to handle movement logic.
     */
    private void tick() {
        if (cart == null) return;

        // Visual circle showing the aura around the cart
        showAura();

        // Update hologram position/text
        if (holoEnabled) spawnOrUpdateHologram();

        List<Player> nearby = cart.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .collect(Collectors.toList());

        nearby = nearby.stream()
                .filter(p -> {
                    boolean isStaff = getInstance().getStaffManager().isStaffEnabled(p);
                    boolean isVanish = getInstance().getStaffManager().isVanished(p);
                    return !isStaff && !isVanish;
                })
                .collect(Collectors.toList());

        if (nearby.isEmpty()) {
            updateController(null);
            controllingTeam = null;
            contesting = false;
            long now = System.currentTimeMillis();
            if (progress > 0) {
                if (stoppedAt == 0L) {
                    stoppedAt = now;
                    Bukkit.broadcastMessage(getLanguageConfig().getString("PAYLOAD.STOPPED_NO_PLAYERS"));
                } else if (now - stoppedAt >= stopDelayMillis) {
                    move(-1);
                    if (!returning) {
                        Bukkit.broadcastMessage(getLanguageConfig().getString("PAYLOAD.RETURNING_NO_PLAYERS"));
                        returning = true;
                    }
                }
            } else {
                stoppedAt = 0L;
            }
            return;
        }

        stoppedAt = 0L;
        returning = false;

        TeamManager teamManager = getInstance().getTeamManager();
        Set<PlayerTeam> teams = new HashSet<>();
        boolean nonTeamPresent = false;
        for (Player p : nearby) {
            PlayerTeam team = teamManager.getByPlayer(p.getUniqueId());
            if (team != null) {
                teams.add(team);
            } else {
                nonTeamPresent = true;
            }
        }

        if (nearby.size() == 1) {
            Player only = nearby.get(0);
            controllingTeam = teamManager.getByPlayer(only.getUniqueId());
            String name = controllingTeam != null ? controllingTeam.getName() : only.getName();
            updateController(name);
            contesting = false;
            move(1);
            if (controllingTeam != null) {
            }
            return;
        }

        if (teams.size() == 1 && !nonTeamPresent) {
            controllingTeam = teams.iterator().next();
            updateController(controllingTeam.getName());
            contesting = false;
            move(1);
        } else {
            updateController(null);
            controllingTeam = null;
            if (!contesting) {
                move(-1);
                Bukkit.broadcastMessage(getLanguageConfig().getString("PAYLOAD.BACKWARDS_MULTIPLE"));
                contesting = true;
            } else {
                Bukkit.broadcastMessage(getLanguageConfig().getString("PAYLOAD.STOPPED_WAITING"));
            }
        }
    }

    private void spawnOrUpdateHologram() {
        if (!holoEnabled || cart == null) return;

        Location base = cart.getLocation().clone().add(0, holoOffsetY, 0);
        List<String> lines = buildHologramLines();

        // Ensure plugin present
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("HolographicDisplays") == null) {
            return; // HD not available; skip silently
        }

        if (hologram == null || hologram.isDeleted()) {
            hologram = HologramsAPI.createHologram(getInstance(), base);
            for (String line : lines) {
                hologram.appendTextLine(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            }
        } else {
            // Move to new base
            hologram.teleport(base);
            // Sync lines size
            if (hologram.size() != lines.size()) {
                hologram.clearLines();
                for (String line : lines) {
                    hologram.appendTextLine(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
                }
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    String text = org.bukkit.ChatColor.translateAlternateColorCodes('&', lines.get(i));
                    if (text == null || text.isEmpty()) text = " ";
                    if (hologram.getLine(i) instanceof TextLine) {
                        ((TextLine) hologram.getLine(i)).setText(text);
                    } else {
                        // Non-text line present; reset all to text
                        hologram.clearLines();
                        for (String l : lines) {
                            hologram.appendTextLine(org.bukkit.ChatColor.translateAlternateColorCodes('&', l));
                        }
                        break;
                    }
                }
            }
        }
    }

    private void removeHologram() {
        if (hologram != null && !hologram.isDeleted()) {
            hologram.delete();
        }
        hologram = null;
    }

    private List<String> buildHologramLines() {
        List<String> out = new ArrayList<>(holoLines.size());
        String controller = controllingName != null ? controllingName : "Nadie";
        String direction = getManager().getPayloadDirection();
        String distance = getManager().getRemainingDistance();
        String speedStr = getManager().getPayloadSpeed();
        String remainingStr = getManager().getRemainingString();
        String status = contesting ? "Disputado" : ("Moving".equalsIgnoreCase(direction) ? "Avanzando" : ("Backwards".equalsIgnoreCase(direction) ? "Retrocediendo" : "Detenido"));
        String progressPct = getProgressPercent();

        for (String raw : holoLines) {
            String line = raw
                    .replace("%controller%", controller)
                    .replace("%direction%", direction)
                    .replace("%distance%", distance)
                    .replace("%speed%", speedStr)
                    .replace("%rem%", remainingStr)
                    .replace("%status%", status)
                    .replace("%progress%", progressPct);
            out.add(line);
        }
        return out;
    }

    private String getProgressPercent() {
        if (path == null || path.size() <= 1) return "0.0%";
        double pct = (Math.max(0D, Math.min(progress, path.size() - 1)) / (double) (path.size() - 1)) * 100.0D;
        return String.format(java.util.Locale.US, "%.1f%%", pct);
    }

    private void showAura() {
        if (cart == null || !auraEnabled) return;

        Location center = cart.getLocation();
        World world = center.getWorld();
        if (world == null) return;

        double stepRad = Math.toRadians(auraStepDegrees);

        float r = auraColor.getRed() / 255.0f;
        float g = auraColor.getGreen() / 255.0f;
        float b = auraColor.getBlue() / 255.0f;

        for (double angle = 0; angle < Math.PI * 2; angle += stepRad) {
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location loc = center.clone().add(x, auraYOffset, z);

            try {
                world.spigot().playEffect(
                    loc,
                    Effect.COLOURED_DUST,
                    0,      // data
                    0,      // data2
                    r, g, b,// offsets -> color
                    1.0f,   // speed
                    0,      // count
                    32      // view distance
                );
            } catch (Throwable t) {
                world.playEffect(loc, Effect.COLOURED_DUST, 0);
            }
        }
    }

    private Color parseColor(String input, Color fallback) {
        if (input == null || input.trim().isEmpty()) return fallback;
        String s = input.trim();

        try {
            if (s.startsWith("#")) s = s.substring(1);
            if (s.matches("(?i)^[0-9A-F]{6}$")) {
                int rgb = Integer.parseInt(s, 16);
                return Color.fromRGB(rgb);
            }
        } catch (Exception ignored) {
        }

        switch (s.toUpperCase(Locale.ROOT)) {
            case "WHITE":
                return Color.WHITE;
            case "BLACK":
                return Color.BLACK;
            case "RED":
                return Color.RED;
            case "GREEN":
                return Color.GREEN;
            case "BLUE":
                return Color.BLUE;
            case "YELLOW":
                return Color.YELLOW;
            case "ORANGE":
                return Color.ORANGE;
            case "PURPLE":
                return Color.PURPLE;
            case "FUCHSIA":
            case "MAGENTA":
            case "PINK":
                return Color.FUCHSIA;
            case "AQUA":
            case "CYAN":
                return Color.AQUA;
            case "LIME":
                return Color.LIME;
            case "GRAY":
            case "GREY":
                return Color.GRAY;
            case "SILVER":
                return Color.SILVER;
            case "MAROON":
                return Color.MAROON;
            case "NAVY":
                return Color.NAVY;
            case "TEAL":
                return Color.TEAL;
            case "OLIVE":
                return Color.OLIVE;
            default:
                return fallback;
        }
    }

    /**
     * Scan the rail network from start to end and store the path the cart should follow.
     */
    public void scanPath() {
        path.clear();
        if (start == null) return;

        Block startBlock = nearestRail(start);
        if (startBlock == null) return;

        Block endBlock = end != null ? nearestRail(end) : null;

        // Build the rail network reachable from start (BFS)
        Map<Block, List<Block>> graph = buildRailGraph(startBlock, 20000); // hard cap to avoid insane loops

        // If end not set or unreachable, pick automatic endpoints by graph diameter heuristic
        if (endBlock == null || !graph.containsKey(endBlock)) {
            Block a = pickAnyEndpoint(graph);
            if (a == null) a = startBlock;
            Block far = bfsFarthest(a, graph).getKey();
            Map.Entry<Block, Integer> far2 = bfsFarthest(far, graph);
            Block b = far2.getKey();
            startBlock = far;
            endBlock = b;
            this.start = startBlock.getLocation();
            this.end = endBlock.getLocation();
        }

        // Shortest path from start to end within the rail graph
        List<Block> route = shortestPath(startBlock, endBlock, graph);
        if (route == null || route.isEmpty()) return;
        for (Block b : route) path.add(b.getLocation());
    }

    private Block nearestRail(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Block base = loc.getBlock();
        if (isRail(base.getType())) return base;
        // small cubic search up to radius 3
        int r = 3;
        double bestDist = Double.MAX_VALUE;
        Block best = null;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block b = base.getRelative(dx, dy, dz);
                    if (isRail(b.getType())) {
                        double d = b.getLocation().distanceSquared(loc);
                        if (d < bestDist) { bestDist = d; best = b; }
                    }
                }
            }
        }
        return best;
    }

    private Map<Block, List<Block>> buildRailGraph(Block start, int maxNodes) {
        Map<Block, List<Block>> graph = new LinkedHashMap<>();
        Deque<Block> q = new ArrayDeque<>();
        Set<Block> seen = new HashSet<>();
        q.add(start);
        seen.add(start);
        graph.put(start, new ArrayList<>());
        while (!q.isEmpty() && graph.size() < maxNodes) {
            Block cur = q.poll();
            List<Block> neighbors = getRailNeighbors(cur);
            graph.computeIfAbsent(cur, k -> new ArrayList<>());
            for (Block nb : neighbors) {
                graph.get(cur).add(nb);
                graph.computeIfAbsent(nb, k -> new ArrayList<>());
                if (!graph.get(nb).contains(cur)) graph.get(nb).add(cur);
                if (seen.add(nb)) q.add(nb);
            }
        }
        return graph;
    }

    private List<Block> getRailNeighbors(Block block) {
        List<Block> out = new ArrayList<>();
        List<BlockFace> cardinals = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
        for (BlockFace face : cardinals) {
            Block same = block.getRelative(face);
            if (isRail(same.getType())) out.add(same);
        }
        for (BlockFace face : cardinals) {
            Block up = block.getRelative(face).getRelative(BlockFace.UP);
            if (isRail(up.getType())) out.add(up);
            Block down = block.getRelative(face).getRelative(BlockFace.DOWN);
            if (isRail(down.getType())) out.add(down);
        }
        Block vUp = block.getRelative(BlockFace.UP);
        if (isRail(vUp.getType())) out.add(vUp);
        Block vDown = block.getRelative(BlockFace.DOWN);
        if (isRail(vDown.getType())) out.add(vDown);
        return out;
    }

    private Map.Entry<Block, Integer> bfsFarthest(Block src, Map<Block, List<Block>> graph) {
        Map<Block, Integer> dist = new HashMap<>();
        Deque<Block> q = new ArrayDeque<>();
        q.add(src);
        dist.put(src, 0);
        Block far = src;
        while (!q.isEmpty()) {
            Block cur = q.poll();
            int cd = dist.get(cur);
            if (cd > dist.get(far)) far = cur;
            for (Block nb : graph.getOrDefault(cur, Collections.emptyList())) {
                if (!dist.containsKey(nb)) {
                    dist.put(nb, cd + 1);
                    q.add(nb);
                }
            }
        }
        return new AbstractMap.SimpleEntry<>(far, dist.getOrDefault(far, 0));
    }

    private Block pickAnyEndpoint(Map<Block, List<Block>> graph) {
        for (Map.Entry<Block, List<Block>> e : graph.entrySet()) {
            if (e.getValue().size() <= 1) return e.getKey();
        }
        // no endpoint (loop), just return first
        return graph.keySet().stream().findFirst().orElse(null);
    }

    private List<Block> shortestPath(Block start, Block end, Map<Block, List<Block>> graph) {
        Map<Block, Block> prev = new HashMap<>();
        Set<Block> vis = new HashSet<>();
        Deque<Block> q = new ArrayDeque<>();
        q.add(start);
        vis.add(start);
        boolean found = false;
        while (!q.isEmpty()) {
            Block cur = q.poll();
            if (cur.equals(end)) { found = true; break; }
            for (Block nb : graph.getOrDefault(cur, Collections.emptyList())) {
                if (vis.add(nb)) { prev.put(nb, cur); q.add(nb); }
            }
        }
        if (!found) return null;
        List<Block> route = new ArrayList<>();
        Block at = end;
        while (at != null) {
            route.add(at);
            at = prev.get(at);
        }
        Collections.reverse(route);
        return route;
    }

    private Block nextRail(Block block, Set<Block> visited) {
        // Prefer cardinal connections on same level, then up/down slopes, then vertical
        List<BlockFace> cardinals = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);

        // 1) Same-level neighbors
        for (BlockFace face : cardinals) {
            Block rel = block.getRelative(face);
            if (!visited.contains(rel) && isRail(rel.getType())) return rel;
        }

        // 2) Slopes (neighbor up/down)
        for (BlockFace face : cardinals) {
            Block relUp = block.getRelative(face).getRelative(BlockFace.UP);
            if (!visited.contains(relUp) && isRail(relUp.getType())) return relUp;
            Block relDown = block.getRelative(face).getRelative(BlockFace.DOWN);
            if (!visited.contains(relDown) && isRail(relDown.getType())) return relDown;
        }

        // 3) Vertical continuation (rare)
        Block up = block.getRelative(BlockFace.UP);
        if (!visited.contains(up) && isRail(up.getType())) return up;
        Block down = block.getRelative(BlockFace.DOWN);
        if (!visited.contains(down) && isRail(down.getType())) return down;

        return null;
    }

    private boolean isRail(Material type) {
        if (type == null) return false;
        // Support legacy and modern material names
        if (type == Material.RAILS) return true;
        try { if (type.name().equalsIgnoreCase("RAIL")) return true; } catch (Throwable ignored) {}
        if (type == Material.POWERED_RAIL) return true;
        if (type == Material.DETECTOR_RAIL) return true;
        try { if (type.name().equalsIgnoreCase("ACTIVATOR_RAIL")) return true; } catch (Throwable ignored) {}
        return false;
    }

    /**
     * Returns the type of this event for storage/identification.
     */
    public EventType getType() {
        return EventType.PAYLOAD;
    }

    /**
     * Loads payload settings from a map stored in configuration.
     */
    public void loadFromMap(Map<String, Object> map) {
        if (map == null) return;

        this.start = Serializer.fetchLocation((String) map.getOrDefault("start", "null"));
        this.end = Serializer.fetchLocation((String) map.getOrDefault("end", "null"));
        this.path.clear();
        this.path.addAll(Serializer.fetchLocations(map.get("path")));
        try {
            this.speed = Double.parseDouble(String.valueOf(map.getOrDefault("speed", this.speed)));
            this.radius = Double.parseDouble(String.valueOf(map.getOrDefault("radius", this.radius)));
            this.stopDelayMillis = Long.parseLong(String.valueOf(map.getOrDefault("stopdelay", this.stopDelayMillis)));
        } catch (NumberFormatException ignored) {
        }
    }

    /**
     * Serializes payload data for saving in configuration.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("start", Serializer.serializeLoc(start));
        map.put("end", Serializer.serializeLoc(end));
        map.put("path", Serializer.serializeLocations(path));
        map.put("speed", String.valueOf(speed));
        map.put("radius", String.valueOf(radius));
        map.put("stopdelay", String.valueOf(stopDelayMillis));
        map.put("type", getType().toString());
        return map;
    }
}
