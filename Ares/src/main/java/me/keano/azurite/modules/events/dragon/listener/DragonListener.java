package me.keano.azurite.modules.events.dragon.listener;

import me.keano.azurite.modules.events.dragon.DragonManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 10/02/2025
 * Project: Zeus
 */

public class DragonListener implements Listener {

    private final DragonManager dragonManager;
    private final Map<UUID, Long> wingKnockTimestamps = new HashMap<>();
    private static final long WING_COOLDOWN_MS = 500; // 0.5s cooldown for wing knockback

    public DragonListener(DragonManager dragonManager) {
        this.dragonManager = dragonManager;
        Bukkit.getPluginManager().registerEvents(this, dragonManager.getInstance());
    }

    /**
     * Detect when the dragon is hit by a player or their projectile
     * and track damage + broadcast its health.
     */
    @EventHandler
    public void onDragonTakeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!dragon.equals(dragonManager.getCurrentDragon())) return;

        Player damagerPlayer = null;
        // Direct melee
        if (event.getDamager() instanceof Player) {
            damagerPlayer = (Player) event.getDamager();
        }
        // Projectile
        else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                damagerPlayer = (Player) proj.getShooter();
            }
        }

        if (damagerPlayer != null) {
            // Record damage and broadcast health
            dragonManager.addDamage(damagerPlayer, event.getDamage());
        }
    }

    @EventHandler
    public void onDragonFireballHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Fireball)) return;
        Fireball fb = (Fireball) event.getDamager();
        if (!(fb.getShooter() instanceof EnderDragon)) return;
        EnderDragon shooter = (EnderDragon) fb.getShooter();
        if (!shooter.equals(dragonManager.getCurrentDragon())) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Custom damage + effects
        event.setDamage(dragonManager.getAttackDamage());
        player.sendMessage(ChatColor.RED + "The dragon has hit you");
        for (PotionEffect eff : dragonManager.getAttackEffects()) {
            player.addPotionEffect(eff);
        }

        // Knockback from actual fireball location
        applyKnockback(player, fb.getLocation());
    }

    @EventHandler
    public void onDragonWingHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof EnderDragon)) return;
        EnderDragon dr = (EnderDragon) event.getDamager();
        if (!dr.equals(dragonManager.getCurrentDragon())) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = wingKnockTimestamps.get(id);
        if (last != null && now - last < WING_COOLDOWN_MS) return;
        wingKnockTimestamps.put(id, now);

        applyKnockback(player, dr.getLocation());
    }

    private void applyKnockback(Player player, Location source) {
        Vector dir = player.getLocation().toVector()
                .subtract(source.toVector())
                .normalize()
                .multiply(dragonManager.getKnockbackStrength());
        dir.setY(0.5);
        player.setVelocity(dir);
    }
}
