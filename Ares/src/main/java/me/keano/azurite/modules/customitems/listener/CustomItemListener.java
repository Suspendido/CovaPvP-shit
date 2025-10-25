package me.keano.azurite.modules.customitems.listener;

import me.keano.azurite.modules.customitems.CustomItem;
import me.keano.azurite.modules.customitems.CustomItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 22/01/2025
 */

public class CustomItemListener implements Listener {

    private final CustomItemManager customItemManager;

    public CustomItemListener(CustomItemManager customItemManager) {
        this.customItemManager = customItemManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        for (CustomItem customItem : customItemManager.getCustomItems().values()) {
            if (customItem.matches(item)) {
                customItem.onPlayerInteract(event);
                return;
            }
        }
    }

}
