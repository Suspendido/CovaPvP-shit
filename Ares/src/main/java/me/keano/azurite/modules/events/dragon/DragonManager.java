// src/main/java/me/keano/azurite/modules/events/dragon/DragonManager.java
package me.keano.azurite.modules.events.dragon;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.dragon.listener.DragonListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 10/02/2025
 * Project: Zeus
 */

@Getter
@Setter
public class DragonManager extends Manager {

    private EnderDragon currentDragon;
    private final Map<UUID, Double> damageMap = new HashMap<>();
    private int maxHealth;

    private int dragonX, dragonY, dragonZ;
    private String dragonWorld;

    private Long remaining;
    private boolean active;

    private double attackDamage;
    private long attackInterval;
    private int attackRadius;
    private double knockbackStrength;
    private List<PotionEffect> attackEffects = new ArrayList<>();
    private BukkitTask attackTask;

    public DragonManager(HCF instance) {
        super(instance);
        loadConfig();
        new DragonListener(this);
        this.active = false;
        this.remaining = 0L;
    }

    private void loadConfig() {
        this.maxHealth        = getConfig().getInt("DRAGON_EVENT.HEALTH", 200);
        this.dragonX          = getConfig().getInt("DRAGON_EVENT.LOCATION.X");
        this.dragonY          = getConfig().getInt("DRAGON_EVENT.LOCATION.Y");
        this.dragonZ          = getConfig().getInt("DRAGON_EVENT.LOCATION.Z");
        this.dragonWorld      = getConfig().getString("DRAGON_EVENT.LOCATION.world");

        this.attackDamage     = getConfig().getDouble("DRAGON_EVENT.ATTACK.DAMAGE", 4.0);
        this.attackInterval   = getConfig().getLong  ("DRAGON_EVENT.ATTACK.INTERVAL", 100L);
        this.attackRadius     = getConfig().getInt   ("DRAGON_EVENT.ATTACK.RADIUS", 30);
        this.knockbackStrength= getConfig().getDouble("DRAGON_EVENT.ATTACK.KNOCKBACK", 1.5);

        attackEffects.clear();
        for (String entry : getConfig().getStringList("DRAGON_EVENT.ATTACK.EFFECTS")) {
            String[] parts = entry.split(":");
            PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
            int duration   = Integer.parseInt(parts[1]);
            int amplifier  = Integer.parseInt(parts[2]);
            if (type != null) {
                attackEffects.add(new PotionEffect(type, duration, amplifier));
            }
        }
    }

    public void start(long time) {
        spawnDragon();
        this.active    = true;
        this.remaining = System.currentTimeMillis() + time;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String msg : getLanguageConfig().getStringList("DRAGON_COMMAND.STARTED")) {
                player.sendMessage(msg);
            }
        }

        scheduleAttackTask();
    }

    private void scheduleAttackTask() {
        if (attackTask != null) {
            attackTask.cancel();
            attackTask = null;
        }

        attackTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentDragon == null) {
                    cancel();
                    return;
                }
                Location eye = currentDragon.getEyeLocation();
                World w = eye.getWorld();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().equals(w)) continue;
                    if (player.getLocation().distance(eye) > attackRadius) continue;
                    Fireball fb = w.spawn(eye, Fireball.class);
                    fb.setShooter(currentDragon);
                    Vector dir = player.getEyeLocation().toVector()
                            .subtract(eye.toVector())
                            .normalize();
                    fb.setDirection(dir);
                    fb.setYield(0f);
                }
            }
        }.runTaskTimer(getInstance(), 20L, attackInterval);
    }

    private void spawnDragon() {
        clearDragons();
        World world = Bukkit.getWorld(dragonWorld);
        if (world == null) return;
        currentDragon = world.spawn(
                new Location(world, dragonX, dragonY, dragonZ),
                EnderDragon.class
        );
        currentDragon.setMaxHealth(maxHealth);
        currentDragon.setHealth(maxHealth);
    }

    public void endDragon() {
        this.active    = false;
        this.remaining = 0L;
        if (attackTask != null) {
            attackTask.cancel();
            attackTask = null;
        }
        if (currentDragon != null) {
            currentDragon.remove();
            currentDragon = null;
        }
        clearDragons();
        damageMap.clear();
        getInstance().getTimerManager().getCustomTimers().remove("EnderDragon");
    }

    public void endEvent() { endDragon(); }

    public int clearDragons() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof EnderDragon) {
                    e.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    public void addDamage(Player player, double damage) {
        damageMap.merge(player.getUniqueId(), damage, Double::sum);
        broadcastHealth();
    }

    private void broadcastHealth() {
        if (currentDragon == null) return;
        int hp = (int) currentDragon.getHealth();
        String msg = getLanguageConfig()
                .getString("DRAGON_COMMAND.HEALTH_UPDATE")
                .replace("%health%", String.valueOf(hp))
                .replace("%max%", String.valueOf(maxHealth));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    public List<Map.Entry<UUID, Double>> getTopDamagers() {
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(damageMap.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return list.subList(0, Math.min(list.size(), 10));
    }

    public void setHealth(int health) {
        this.maxHealth = health;
        getConfig().set("DRAGON_EVENT.HEALTH", health);
        getInstance().saveConfig();
    }

    public void setDragonCoords(Location loc) {
        this.dragonX     = loc.getBlockX();
        this.dragonY     = loc.getBlockY();
        this.dragonZ     = loc.getBlockZ();
        this.dragonWorld = loc.getWorld().getName();
        getConfig().set("DRAGON_EVENT.LOCATION.X", dragonX);
        getConfig().set("DRAGON_EVENT.LOCATION.Y", dragonY);
        getConfig().set("DRAGON_EVENT.LOCATION.Z", dragonZ);
        getConfig().set("DRAGON_EVENT.LOCATION.world", dragonWorld);
        getInstance().saveConfig();
    }

    public EnderDragon getCurrentDragon() {
        return currentDragon;
    }

    public int getCurrentHealth() {
        return (currentDragon != null) ? (int) currentDragon.getHealth() : 0;
    }

    public List<EnderDragon> getAliveDragons() {
        List<EnderDragon> list = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof EnderDragon) list.add((EnderDragon) e);
            }
        }
        return list;
    }

    public String getRemainingString() {
        long rem = remaining - System.currentTimeMillis();
        if (rem <= 0 && active) {
            this.active = false;
            Tasks.execute(this, this::endDragon);
            return "00:00";
        }
        return getRemaining(rem);
    }

    private static String getRemaining(long millis) {
        if (millis < 0) return "00:00";
        long secs = (millis/1000)%60, mins = (millis/60000)%60,
                hrs = (millis/3600000)%24, days = millis/86400000;
        if (days>0)   return String.format("%d:%02d:%02d:%02d",days,hrs,mins,secs);
        if (hrs>0)    return String.format("%02d:%02d:%02d",hrs,mins,secs);
        return String.format("%02d:%02d",mins,secs);
    }

    public double getAttackDamage()              { return attackDamage; }
    public double getKnockbackStrength()         { return knockbackStrength; }
    public List<PotionEffect> getAttackEffects() { return attackEffects; }
}
