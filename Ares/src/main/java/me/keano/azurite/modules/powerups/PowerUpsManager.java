package me.keano.azurite.modules.powerups;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 7/16/2025
 * Project: Ares
 *
 */

@Getter
@Setter
public class PowerUpsManager {

    private final HCF plugin;
    private BukkitTask spawnTask;

    private final Map<Location, Integer> activePowerups = new HashMap<>();
    private final Map<Location, BukkitTask> auraTasks   = new HashMap<>();
    private final Map<Location, BukkitTask> pickupTasks = new HashMap<>();

    private String lastPicker;
    private int lastType;

    private List<World> spawnWorlds;
    private int maxActive;

    public PowerUpsManager(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean isRunning() {
        if (spawnTask == null) return false;
        int id = spawnTask.getTaskId();
        return Bukkit.getScheduler().isQueued(id) ||
                Bukkit.getScheduler().isCurrentlyRunning(id);
    }

    public void start(long minutes) {
        stop();

        ConfigurationSection spawnCfg = plugin.getConfig()
                .getConfigurationSection("POWERUPS.SPAWN");

        spawnWorlds = new ArrayList<>();
        for (String name : spawnCfg.getStringList("WORLDS")) {
            World w = Bukkit.getWorld(name);
            if (w != null) spawnWorlds.add(w);
        }

        maxActive = spawnCfg.getInt("MAX_ACTIVE", 3);

        long ticks = minutes * 60L * 20L;
        spawnTask = Bukkit.getScheduler()
                .runTaskTimer(plugin, this::spawnPowerup, ticks, ticks);
    }

    public void stop() {
        if (spawnTask != null) spawnTask.cancel();
        auraTasks.values().forEach(BukkitTask::cancel);
        pickupTasks.values().forEach(BukkitTask::cancel);
        activePowerups.clear();
        auraTasks.clear();
        pickupTasks.clear();
    }

    public void spawn(int typeId, Location loc) {
        schedulePowerupAt(loc, typeId);
    }

    private void spawnPowerup() {
        if (activePowerups.size() >= maxActive) {
            String warn = plugin.getConfig()
                    .getString("POWERUPS.MESSAGES.LIMIT_REACHED")
                    .replace("%limit%", String.valueOf(maxActive));
            Bukkit.broadcastMessage(warn);
            return;
        }

        ConfigurationSection spawnCfg = plugin.getConfig()
                .getConfigurationSection("POWERUPS.SPAWN");
        ConfigurationSection typesCfg = plugin.getConfig()
                .getConfigurationSection("POWERUPS.TYPES");

        int attemptsPerWorld = spawnCfg.getInt("SPAWN_ATTEMPTS", 10);

        for (World world : spawnWorlds) {
            if (activePowerups.size() >= maxActive) break;

            Location chosenLoc = null;
            int tries = attemptsPerWorld;
            while (tries-- > 0) {
                int x = ThreadLocalRandom.current().nextInt(
                        spawnCfg.getInt("MIN_X"), spawnCfg.getInt("MAX_X") + 1);
                int z = ThreadLocalRandom.current().nextInt(
                        spawnCfg.getInt("MIN_Z"), spawnCfg.getInt("MAX_Z") + 1);

                int groundY = world.getHighestBlockYAt(x, z);
                if (groundY <= 0) continue;
                Material below = world.getBlockAt(x, groundY, z).getType();
                if (!below.isSolid()) continue;

                chosenLoc = new Location(world, x + 0.5, groundY + 1, z + 0.5);
                break;
            }

            if (chosenLoc == null) {
                continue;
            }

            List<String> keys = new ArrayList<>(typesCfg.getKeys(false));
            String sel = keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
            int typeId = Integer.parseInt(sel);

            schedulePowerupAt(chosenLoc, typeId);
        }
    }

    private void schedulePowerupAt(Location loc, int typeId) {
        activePowerups.put(loc, typeId);

        String msg = plugin.getConfig()
                .getString("POWERUPS.MESSAGES.SPAWN")
                .replace("%type%", String.valueOf(typeId))
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        Bukkit.broadcastMessage(msg);

        double baseRadius = plugin.getConfig().getDouble("POWERUPS.RADIUS.OUTER", 2.0);

        BukkitTask aura = Bukkit.getScheduler().runTaskTimer(
                plugin, () -> drawAura(loc, baseRadius), 0L, 5L
        );
        auraTasks.put(loc, aura);

        BukkitTask pickup = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distance(loc) <= baseRadius * 1.5) {
                    applyPowerup(p, loc, typeId);
                    break;
                }
            }
        }, 0L, 5L);
        pickupTasks.put(loc, pickup);
    }

    private void drawAura(Location loc, double baseRadius) {
        int typeId = activePowerups.get(loc);
        ConfigurationSection cfg = plugin.getConfig()
                .getConfigurationSection("POWERUPS.TYPES." + typeId);

        String[] rgb = cfg.getString("COLOR").split(",");
        float r = Integer.parseInt(rgb[0]) / 255F;
        float g = Integer.parseInt(rgb[1]) / 255F;
        float b = Integer.parseInt(rgb[2]) / 255F;

        double auraRadius = baseRadius * 1.5;
        double yCenter    = loc.getY();

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 48) {
            double px = loc.getX() + Math.cos(angle) * auraRadius;
            double py = yCenter      + Math.sin(angle) * auraRadius;
            double pz = loc.getZ();
            sendRedstonePacket(new Location(loc.getWorld(), px, py, pz), r, g, b);
        }
    }

    /** Send NMS Packet (Minecraft 1.8). */
    private void sendRedstonePacket(Location loc, float r, float g, float b) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                EnumParticle.REDSTONE, false,
                (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
                r, g, b, 1.0F, 0
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }


    public void sendActionBar(Player player, String message) {
        String safeMsg = message.replace("\"", "\\\"");
        net.minecraft.server.v1_8_R3.IChatBaseComponent icbc =
                net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + safeMsg + "\"}");
        net.minecraft.server.v1_8_R3.PacketPlayOutChat actionbarPacket =
                new net.minecraft.server.v1_8_R3.PacketPlayOutChat(icbc, (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(actionbarPacket);
    }

    private void applyPowerup(Player player, Location loc, int typeId) {
        this.lastPicker = player.getName();
        this.lastType   = typeId;

        ConfigurationSection cfg = plugin.getConfig()
                .getConfigurationSection("POWERUPS.TYPES." + typeId);
        PotionEffectType effectType = PotionEffectType
                .getByName(cfg.getString("EFFECT"));
        int durationTicks = cfg.getInt("DURATION") * 20;
        int level        = Math.max(1, cfg.getInt("LEVEL", 1));
        int amp          = level - 1;

        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                effectType, durationTicks, amp, true, true
        ));

        String actionbarMsg = plugin.getConfig()
                .getString("POWERUPS.MESSAGES.ACTIONBAR_PICKUP", "&a¡Recogiste el powerup %type%!")
                .replace("%type%", String.valueOf(typeId));
        sendActionBar(player, actionbarMsg);

        String msg = plugin.getConfig()
                .getString("POWERUPS.MESSAGES.PICKUP")
                .replace("%player%", player.getName())
                .replace("%type%",   String.valueOf(typeId))
                .replace("%x%",      String.valueOf(loc.getBlockX()))
                .replace("%y%",      String.valueOf(loc.getBlockY()))
                .replace("%z%",      String.valueOf(loc.getBlockZ()));
        Bukkit.broadcastMessage(msg);

        auraTasks.remove(loc).cancel();
        pickupTasks.remove(loc).cancel();
        activePowerups.remove(loc);
    }

    public String getLastPicker() { return lastPicker; }
    public int    getLastType()   { return lastType; }
}
