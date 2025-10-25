package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AntiTrapHaloAbility extends Ability {

    private final Set<UUID> halo;
    private final List<Material> denied;
    private final int seconds;
    private final int radius;
    private final boolean denyPlace;
    private final boolean denyBreak;
    private final boolean denyInteract;

    private final Map<UUID, Set<Location>> visualBlocks;
    private final Map<UUID, Map<Location, Material>> originalBlocks;
    private final Map<UUID, Integer> visualTasks;

    public AntiTrapHaloAbility(AbilityManager manager) {
        super(manager, AbilityUseType.INTERACT, "AntiTrap Halo");

        this.halo = new HashSet<>();
        this.denied = getAbilitiesConfig().getStringList("ANTITRAP_HALO.DENIED_INTERACT")
                .stream().map(ItemUtils::getMat).collect(Collectors.toList());
        this.seconds = getAbilitiesConfig().getInt("ANTITRAP_HALO.SECONDS_ANTI_BUILD");
        this.radius = getAbilitiesConfig().getInt("ANTITRAP_HALO.RADIUS");
        this.denyPlace = getAbilitiesConfig().getBoolean("ANTITRAP_HALO.DENY_PLACE");
        this.denyBreak = getAbilitiesConfig().getBoolean("ANTITRAP_HALO.DENY_BREAK");
        this.denyInteract = getAbilitiesConfig().getBoolean("ANTITRAP_HALO.DENY_INTERACT");

        this.visualBlocks = new ConcurrentHashMap<>();
        this.originalBlocks = new ConcurrentHashMap<>();
        this.visualTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player) || hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        halo.add(player.getUniqueId());
        startDynamicVisualEffect(player);

        getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_HALO.USED")
                .forEach(player::sendMessage);

        Tasks.executeLater(getManager(), seconds * 20L, () -> {
            halo.remove(player.getUniqueId());
            stopDynamicVisualEffect(player);

            getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_HALO.EXPIRED")
                    .forEach(player::sendMessage);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (denyPlace && isInsideHaloArea(e.getBlock().getLocation(), player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_HALO.DENIED"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (denyBreak && isInsideHaloArea(e.getBlock().getLocation(), player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_HALO.DENIED"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("BLOCK") || e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        if (denyInteract && denied.contains(e.getClickedBlock().getType())
                && isInsideHaloArea(e.getClickedBlock().getLocation(), player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_HALO.DENIED"));
        }
    }

    private boolean isInsideHaloArea(Location location, Player actionPlayer) {
        for (UUID playerId : halo) {
            Player haloPlayer = getManager().getInstance().getServer().getPlayer(playerId);
            if (haloPlayer != null && haloPlayer.isOnline()) {
                Location playerLocation = haloPlayer.getLocation();

                if (!playerLocation.getWorld().equals(location.getWorld())) {
                    continue;
                }

                double distance = playerLocation.distance(location);

                if (distance <= radius && !actionPlayer.getUniqueId().equals(playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startDynamicVisualEffect(Player player) {
        UUID playerId = player.getUniqueId();

        visualBlocks.put(playerId, new HashSet<>());
        originalBlocks.put(playerId, new HashMap<>());

        int taskId = Tasks.repeat((AbilityManager) getManager(), 0L, 5L, () -> {
            if (!player.isOnline() || !halo.contains(playerId)) {
                stopDynamicVisualEffect(player);
                return;
            }
            updateVisualEffect(player);
        });

        visualTasks.put(playerId, taskId);
    }

    private void stopDynamicVisualEffect(Player player) {
        UUID playerId = player.getUniqueId();

        Integer taskId = visualTasks.remove(playerId);
        if (taskId != null) Tasks.cancelTask(taskId);

        Set<Location> blocks = visualBlocks.get(playerId);
        if (blocks != null && !blocks.isEmpty()) {
            for (Player nearbyPlayer : getNearbyPlayers(player, 20)) {
                for (Location blockLoc : blocks) {
                    Block actualBlock = blockLoc.getBlock();
                    nearbyPlayer.sendBlockChange(blockLoc, actualBlock.getType(), actualBlock.getData());
                }
            }
        }

        visualBlocks.remove(playerId);
        originalBlocks.remove(playerId);
    }

    private void updateVisualEffect(Player player) {
        cleanupDisconnectedPlayers();

        UUID playerId = player.getUniqueId();
        Set<Location> currentBlocks = visualBlocks.get(playerId);
        Map<Location, Material> currentOriginals = originalBlocks.get(playerId);

        if (currentBlocks == null || currentOriginals == null) return;

        Location center = player.getLocation();
        Set<Location> newBlocks = new HashSet<>();
        int radiusSquared = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radiusSquared) {
                        Location blockLoc = center.clone().add(x, y, z);
                        Block block = blockLoc.getBlock();

                        if (block.getType().isSolid() && block.getType() != Material.GOLD_BLOCK &&
                                block.getType() != Material.GOLD_ORE && block.getType() != Material.AIR) {
                            newBlocks.add(blockLoc.clone());
                        }
                    }
                }
            }
        }

        List<Player> nearbyPlayers = getNearbyPlayers(player, 20);

        for (Location oldBlock : new HashSet<>(currentBlocks)) {
            if (!newBlocks.contains(oldBlock)) {
                Block actualBlock = oldBlock.getBlock();
                for (Player nearbyPlayer : nearbyPlayers) {
                    nearbyPlayer.sendBlockChange(oldBlock, actualBlock.getType(), actualBlock.getData());
                }
                currentBlocks.remove(oldBlock);
                currentOriginals.remove(oldBlock);
            }
        }

        for (Location newBlock : newBlocks) {
            if (!currentBlocks.contains(newBlock)) {
                Block block = newBlock.getBlock();
                currentOriginals.put(newBlock.clone(), block.getType());
                currentBlocks.add(newBlock.clone());

                for (Player nearbyPlayer : nearbyPlayers) {
                    nearbyPlayer.sendBlockChange(newBlock, Material.GOLD_BLOCK, (byte) 0);
                }
            }
        }
    }

    private void cleanupDisconnectedPlayers() {
        halo.removeIf(playerId -> {
            Player player = getManager().getInstance().getServer().getPlayer(playerId);
            return player == null || !player.isOnline();
        });
    }

    private List<Player> getNearbyPlayers(Player center, int radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Entity entity : center.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                nearbyPlayers.add((Player) entity);
            }
        }
        nearbyPlayers.add(center);
        return nearbyPlayers;
    }
}