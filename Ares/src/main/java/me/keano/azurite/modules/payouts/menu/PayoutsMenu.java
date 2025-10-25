package me.keano.azurite.modules.payouts.menu;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.payouts.ItemUtils;
import me.keano.azurite.utils.CC;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 26/02/2025
 * Project: Zeus
 */

public class PayoutsMenu extends Module<ListenerManager> {

    private final HCF hcf;
    private final Inventory inventory;

    public PayoutsMenu(ListenerManager manager, HCF hcf) {
        super(manager);
        String title = CC.t(getMenusConfig().getString("PAYOUTS_MENU.TITLE"));
        int size = getMenusConfig().getInt("PAYOUTS_MENU.SIZE");
        this.inventory = getInstance().getServer().createInventory(null, size, title);

        this.hcf = hcf;
        loadItemsFromConfig(hcf);
    }

    public void reloadItems() {
        inventory.clear();
        loadItemsFromConfig(hcf);
        updateMenuForViewers();
    }

    private void loadItemsFromConfig(HCF plugin) {
        ConfigurationSection itemsSection = getMenusConfig().getConfigurationSection("PAYOUTS_MENU.ITEMS");
        if (itemsSection == null) {
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }

            int slot = itemSection.getInt("slot");
            Material material = Material.valueOf(itemSection.getString("item"));
            int metadata = itemSection.getInt("metadata", 0);
            String displayName = CC.t(itemSection.getString("displayname"));
            List<String> lore = CC.t(itemSection.getStringList("lore"));

            ItemStack item = new ItemStack(material, metadata > 0 ? metadata : 1);

            if (material == Material.SKULL_ITEM && itemSection.contains("skull-owner")) {
                String skullOwner = itemSection.getString("skull-owner");
                item = ItemUtils.getCustomHead(skullOwner);
            }

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
        }
    }

    public void updateMenuForViewers() {
        for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
            viewer.closeInventory();
            openMenu((Player) viewer);
        }
    }

    public void openMenu(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(inventory.getTitle())) {
            event.setCancelled(true);
        }
    }
}