package me.keano.azurite.modules.events.payload;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Tasks;
import java.util.LinkedHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Effect;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

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
public class PayloadManager extends Manager {

    private final Payload payload;

    private boolean active;
    private long remaining;

    private double lastProgressSample = Double.NaN;
    private long lastProgressTime = 0L;
    private String lastDirection = "Stopped";

    private final Map<UUID, List<Location>> routeEditors = new HashMap<>();

    private PayloadStats lastStats;

    private final List<String> msgStart;
    private final List<String> msgEnd;
    private final List<String> msgEditWhileActive;
    private final List<String> msgAlreadyEditing;
    private final List<String> msgRouteStart;
    private final List<String> msgRoutePointAdded;
    private final List<String> msgRoutePointAlready;
    private final List<String> msgRouteNoPoints;
    private final List<String> msgRouteScanned;
    private final List<String> msgRoutePath;
    private final List<String> msgNotEditing;
    private final List<String> msgMoving;

    public PayloadManager(HCF instance) {
        super(instance);

        Map<String, Object> map = (Map<String, Object>) getEventsData().getValues().get("Payload");
        this.payload = new Payload(this);
        if (map != null) {
            this.payload.loadFromMap(map);
        }
        this.active = false;
        this.remaining = 0L;

        this.msgStart = loadMsg("PAYLOAD.EVENT_START");
        this.msgEnd = loadMsg("PAYLOAD.EVENT_END");
        this.msgEditWhileActive = loadMsg("PAYLOAD.ROUTE_ACTIVE_EDIT");
        this.msgAlreadyEditing = loadMsg("PAYLOAD.ROUTE_ALREADY_EDITING");
        this.msgRouteStart = loadMsg("PAYLOAD.ROUTE_START");
        this.msgRoutePointAdded = loadMsg("PAYLOAD.ROUTE_POINT_ADDED");
        this.msgRoutePointAlready = loadMsg("PAYLOAD.ROUTE_POINT_ALREADY");
        this.msgRouteNoPoints = loadMsg("PAYLOAD.ROUTE_NO_POINTS");
        this.msgRouteScanned = loadMsg("PAYLOAD.ROUTE_SAVED");
        this.msgRoutePath = loadMsg("PAYLOAD.ROUTE_PATH");
        this.msgNotEditing = loadMsg("PAYLOAD.NOT_EDITING");
        this.msgMoving = loadMsg("PAYLOAD.EVENT_MOVING");

        new me.keano.azurite.modules.events.payload.listener.PayloadListener(this);
    }

    /**
     * Saves current payload data to configuration.
     */
    public void saveData() {
        Map<String, Object> data = new LinkedHashMap<>(payload.serialize());
        getEventsData().getValues().put("Payload", data);
        getEventsData().save();
    }

    /**
     * Starts the payload event for a specific time (seconds).
     */
    public boolean startPayload() {
        if (active) return false;

        this.active = true;
        this.lastProgressSample = Double.NaN;
        this.lastStats = null;

        this.remaining = Long.MAX_VALUE;
        payload.start();

        broadcastMessageList(msgStart, null);
        return true;
    }

    public boolean endPayload() {
        return endPayload(null);
    }

    public boolean endPayload(String finisher) {
        if (!active) {
            return false;
        }
        this.active = false;
        this.remaining = 0L;
        this.lastProgressSample = Double.NaN;
        PayloadStats stats = payload.stop(finisher);
        this.lastStats = stats;

        broadcastMessageList(msgEnd, null);
        return true;
    }

    @Override
    public void disable() {
        endPayload();
        saveData();
    }

    public void checkMovement() {
        if (!active || payload == null) return;

        String dir = getPayloadDirection();
        if ("Moving".equalsIgnoreCase(dir)) {
                Map<String, String> ph = new HashMap<>();
                broadcastMessageList(msgMoving, ph);
            }
        }

    public String getRemainingDistance() {
        if (payload == null || payload.getPath() == null || payload.getPath().isEmpty()) {
            return "0.0";
        }
        List<Location> path = payload.getPath();
        double progress = Math.max(0.0, Math.min(payload.getProgress(), path.size() - 1));

        int idx = (int) Math.floor(progress);
        double frac = progress - idx;

        double dist = 0.0;

        if (idx >= path.size() - 1) {
            return "0.0";
        }

        Location a = path.get(idx);
        Location b = path.get(idx + 1);
        if (a.getWorld() != null && a.getWorld().equals(b.getWorld())) {
            double seg = a.distance(b);
            dist += (1.0 - frac) * seg;
        } else {
            dist += (1.0 - frac);
        }

        for (int i = idx + 1; i < path.size() - 1; i++) {
            Location p1 = path.get(i);
            Location p2 = path.get(i + 1);
            if (p1.getWorld() != null && p1.getWorld().equals(p2.getWorld())) {
                dist += p1.distance(p2);
            } else {
                dist += 1.0;
            }
        }

        return String.format(Locale.US, "%.1f", dist);
    }

    /**
     * speed from payload bps (2 de).
     * speed by tick; 20 ticks = 1 seg.
     */
    public String getPayloadSpeed() {
        if (payload == null) return "0.00";
        double bps = payload.getSpeed() * 20.0;
        return String.format(Locale.US, "%.2f", bps);
    }

    /**
     * Direction of payload:
     * - "moving" +
     * - "backward" -
     * - "stopped" =
     */
    public String getPayloadDirection() {
        if (!active || payload == null) {
            lastProgressSample = Double.NaN;
            lastDirection = "Stopped";
            return lastDirection;
        }

        double current = payload.getProgress();
        long now = System.currentTimeMillis();

        if (Double.isNaN(lastProgressSample)) {
            lastDirection = "Stopped";
        } else if (current > lastProgressSample + 1e-6) {
            lastDirection = "Moving";
            lastProgressTime = now;
        } else if (current < lastProgressSample - 1e-6) {
            lastDirection = "Backwards";
            lastProgressTime = now;
        } else if (now - lastProgressTime > 500) {
            lastDirection = "Stopped";
        }

        lastProgressSample = current;
        return lastDirection;
    }

    /**
     * Remaining formatted time string.
     */
    public String getRemainingString() {
        if (!active) return "00:00";
        if (remaining == Long.MAX_VALUE) return "∞";

        long rem = remaining - System.currentTimeMillis();
        if (rem <= 0) {
            this.active = false;
            Tasks.execute(this, this::endPayload);
            return "00:00";
        }
        return formatTime(rem);
    }

    private static String formatTime(long millis) {
        if (millis < 0) return "00:00";
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long days = millis / (1000 * 60 * 60 * 24);

        if (days > 0) {
            return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    // -------------------------------------------------
    // Route editing logic
    // -------------------------------------------------

    public void startRouteEdit(Player player) {
        if (active) {
            sendMessageList(player, msgEditWhileActive, null);
            return;
        }
        if (isEditing(player)) {
            sendMessageList(player, msgAlreadyEditing, null);
            return;
        }

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Payload Route Wand");
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        routeEditors.put(player.getUniqueId(), new ArrayList<>());
        sendMessageList(player, msgRouteStart, null);
    }

    public void addRoutePoint(Player player, Location loc) {
        List<Location> list = routeEditors.get(player.getUniqueId());
        if (list == null) return;

        for (Location existing : list) {
            if (existing.equals(loc)) {
                sendMessageList(player, msgRoutePointAlready, null);
                return;
            }
        }

        list.add(loc);

        Map<String, String> ph = new HashMap<>();
        ph.put("%index%", String.valueOf(list.size()));
        ph.put("%x%", String.valueOf(loc.getBlockX()));
        ph.put("%y%", String.valueOf(loc.getBlockY()));
        ph.put("%z%", String.valueOf(loc.getBlockZ()));

        sendMessageList(player, msgRoutePointAdded, ph);
        loc.getWorld().playEffect(loc.clone().add(0.5, 1, 0.5), Effect.HAPPY_VILLAGER, 0);
    }

    public void finishRouteEdit(Player player) {
        List<Location> list = routeEditors.remove(player.getUniqueId());
        if (list == null || list.isEmpty()) {
            sendMessageList(player, msgRouteNoPoints, null);
            removeWand(player);
            return;
        }
        payload.getPath().clear();
        payload.getPath().addAll(list);
        payload.setStart(list.get(0));
        payload.setEnd(list.get(list.size() - 1));

        String coords = list.stream()
                .map(l -> "(" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")")
                .collect(Collectors.joining(" -> "));

        Map<String, String> ph1 = new HashMap<>();
        ph1.put("%points%", String.valueOf(list.size()));
        sendMessageList(player, msgRouteScanned, ph1);

        Map<String, String> ph2 = new HashMap<>();
        ph2.put("%coords%", coords);
        sendMessageList(player, msgRoutePath, ph2);

        list.forEach(l -> l.getWorld().playEffect(l.clone().add(0.5, 1, 0.5), Effect.CLOUD, 0));

        removeWand(player);
        saveData();
    }

    private void removeWand(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta()
                    && "§6Payload Route Wand".equals(item.getItemMeta().getDisplayName())) {
                player.getInventory().setItem(i, null);
                break;
            }
        }
        player.updateInventory();
    }

    public boolean isEditing(Player player) {
        return routeEditors.containsKey(player.getUniqueId());
    }

    private List<String> loadMsg(String path) {
        List<String> list;
        try {
            list = getLanguageConfig().getStringList(path);
        } catch (Throwable t) {
            list = Collections.emptyList();
        }
        if (list != null && !list.isEmpty()) return list;

        // Try as single string (tolerant: use untranslated to avoid ClassCast issues)
        String s;
        try {
            s = getLanguageConfig().getUntranslatedString(path);
        } catch (Throwable t) {
            s = null;
        }
        if (s != null && !s.trim().isEmpty()) {
            return Arrays.stream(s.split("\\\n|\n"))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .map(me.keano.azurite.utils.CC::t)
                    .collect(Collectors.toList());
        }

        // Try as a section (flatten values)
        try {
            org.bukkit.configuration.ConfigurationSection sec = getLanguageConfig().getConfigurationSection(path);
            if (sec != null) {
                List<String> out = new java.util.ArrayList<>();
                for (String key : sec.getKeys(false)) {
                    String val = getLanguageConfig().getUntranslatedString(path + "." + key);
                    if (val != null && !val.trim().isEmpty()) out.add(me.keano.azurite.utils.CC.t(val));
                }
                if (!out.isEmpty()) return out;
            }
        } catch (Throwable ignored) { }

        return Collections.singletonList("&c[LANG MISSING] " + path);
    }


    private void broadcastMessageList(List<String> lines, Map<String, String> placeholders) {
        if (lines == null || lines.isEmpty()) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendMessageList(p, lines, placeholders);
        }
    }

    private void sendMessageList(CommandSender sender, List<String> lines, Map<String, String> placeholders) {
        if (sender == null || lines == null || lines.isEmpty()) return;
        for (String raw : lines) {
            String msg = applyPlaceholders(raw, placeholders);
            if (msg == null || msg.isEmpty()) continue;
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    private String applyPlaceholders(String input, Map<String, String> placeholders) {
        if (input == null) return null;
        if (placeholders == null || placeholders.isEmpty()) return input;
        String out = input;
        for (Map.Entry<String, String> e : placeholders.entrySet()) out = out.replace(e.getKey(), e.getValue());
        return out;
    }

}
