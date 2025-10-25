package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PortalLegacyListener extends Module<ListenerManager> {

    public PortalLegacyListener(ListenerManager manager) {
        super(manager);
    }

    @EventHandler // the bedrock thing
    public void onPortalCreate(EntityCreatePortalEvent e) {
        if (e.getEntity().getType() == EntityType.ENDER_DRAGON) {
            e.setCancelled(true);
        }
    }

    @EventHandler // deny entities from using portals
    public void onEntity(EntityPortalEvent e) {
        if (e.getEntity() instanceof Player) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) // call first
    public void onPortal(PlayerPortalEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return;

        World from = e.getFrom().getWorld();

        if (from.getEnvironment() == World.Environment.NORMAL) {
            Location cloned = e.getFrom().clone();
            cloned.setWorld(e.getTo().getWorld());
            cloned.setX(cloned.getX() / Config.NETHER_MULTIPLIER);
            cloned.setZ(cloned.getZ() / Config.NETHER_MULTIPLIER);
            e.setTo(cloned);
            return;
        }

        if (from.getEnvironment() == World.Environment.NETHER) {
            Location cloned = e.getFrom().clone();
            cloned.setWorld(e.getTo().getWorld());
            cloned.setX(cloned.getX() * Config.NETHER_MULTIPLIER);
            cloned.setZ(cloned.getZ() * Config.NETHER_MULTIPLIER);
            e.setTo(cloned);
        }
    }

    @EventHandler
    public void onTeleport(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        Team from = getInstance().getTeamManager().getClaimManager().getTeam(e.getFrom());

        e.setCancelled(true);
        e.useTravelAgent(false);

        switch (e.getTo().getWorld().getEnvironment()) {
            case NORMAL:
                player.teleport(getInstance().getWaypointManager().getWorldSpawn().clone().add(0.5, 0, 0.5));
                break;

            case NETHER:
                player.teleport(getInstance().getWaypointManager().getNetherSpawn());
                break;

            case THE_END:
                player.teleport(getInstance().getWaypointManager().getEndSpawn().clone().add(0.5, 0, 0.5));
                break;


        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeaveEnd(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        Block blockMovingTo = e.getTo().getBlock();
        World world = player.getWorld();

        if(world == null) return;

        if (world.getEnvironment() == World.Environment.THE_END && blockMovingTo.isLiquid()) {
            Location endExitLocation = getInstance().getWaypointManager().getEndWorldExit();
            player.teleport(endExitLocation);
        }
    }

}