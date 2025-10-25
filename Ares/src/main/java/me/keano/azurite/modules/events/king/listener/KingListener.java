package me.keano.azurite.modules.events.king.listener;

import me.keano.azurite.modules.events.king.KingManager;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KingListener extends Module<KingManager> {

    private final Map<Player, Long> laserCooldown = new HashMap<>();
    private final Map<Player, Long> dashCooldown = new HashMap<>();

    public KingListener(KingManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (getManager().getKing() == player) {
            e.getDrops().clear(); // we make sure we clear the drops.
            e.setDroppedExp(0);
            e.setDeathMessage(null);
            getManager().stopKing(false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (getManager().getKing() == player) {
            getManager().stopKing(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!getManager().isActive()) return;

        if (!player.equals(getManager().getKing())) {
            if (getManager().isLaser(e.getItem()) || getManager().isDash(e.getItem())) {
                e.setCancelled(true);
            }
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (getManager().isLaser(e.getItem())) {
            long last = laserCooldown.getOrDefault(player, 0L);
            if (System.currentTimeMillis() - last < 5000) {
                return;
            }
            laserCooldown.put(player, System.currentTimeMillis());

            Vector dir = player.getLocation().getDirection().normalize();
            for (int i = 0; i < 30; i++) {
                Vector step = dir.clone().multiply(i);
                org.bukkit.Location loc = player.getLocation().clone().add(step);
                for (Entity ent : player.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                    if (ent instanceof Player && ent != player) {
                        Player hit = (Player) ent;
                        hit.setVelocity(dir.clone().multiply(1.5));
                        return;
                    }
                }
            }
        } else if (getManager().isDash(e.getItem())) {
            long last = dashCooldown.getOrDefault(player, 0L);
            if (System.currentTimeMillis() - last < 5000) {
                return;
            }
            dashCooldown.put(player, System.currentTimeMillis());

            Vector boost = player.getLocation().getDirection().multiply(2);
            player.setVelocity(boost);
            for (Entity ent : player.getNearbyEntities(3, 3, 3)) {
                if (ent instanceof Player && ent != player) {
                    Player hit = (Player) ent;
                    Vector knock = hit.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.2);
                    hit.setVelocity(knock);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getManager().isActive()) return;
        Player king = getManager().getKing();
        if (!e.getPlayer().equals(king)) return;

        int limit = getManager().getFlightLimit();
        int ground = e.getTo().getWorld().getHighestBlockYAt(e.getTo());
        if (e.getTo().getY() > ground + limit) {
            e.getPlayer().setVelocity(new Vector(0, -0.4, 0));
        }
    }
}