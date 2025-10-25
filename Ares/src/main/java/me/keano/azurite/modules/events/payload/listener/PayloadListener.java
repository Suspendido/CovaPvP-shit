package me.keano.azurite.modules.events.payload.listener;

import me.keano.azurite.modules.events.payload.PayloadManager;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for the payload event. Handles invulnerability of the minecart
 * and assists with manual route creation using the route wand.
 */
public class PayloadListener extends Module<PayloadManager> {

    public PayloadListener(PayloadManager manager) {
        super(manager);
    }

    // Prevent the payload cart from taking any kind of damage
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("payloadCart")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getVehicle().hasMetadata("payloadCart")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getVehicle().hasMetadata("payloadCart")) {
            event.setCancelled(true);
        }
    }

    // Route building using the wand
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("LEFT")) return;

        Player player = event.getPlayer();
        ItemStack item = getManager().getItemInHand(player);
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();
        if (!"§6Payload Route Wand".equals(name)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        if (type == Material.RAILS || type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL) {
            getManager().addRoutePoint(player, block.getLocation());
            event.setCancelled(true);
        }
    }
}

