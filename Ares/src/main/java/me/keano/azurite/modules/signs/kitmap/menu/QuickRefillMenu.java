package me.keano.azurite.modules.signs.kitmap.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class QuickRefillMenu extends Menu {

    public QuickRefillMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getConfig().getString("SIGNS_CONFIG.QUICK_REFILL_SIGN.MENU_TITLE"),
                manager.getConfig().getInt("SIGNS_CONFIG.QUICK_REFILL_SIGN.MENU_SIZE"),
                false
        );
        this.setAllowInteract(true);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<ItemStack> items = new ArrayList<>();
        for (String entry : getConfig().getStringList("SIGNS_CONFIG.QUICK_REFILL_SIGN.ITEMS")) {
            String[] parts = entry.split(":");
            Material material = Material.matchMaterial(parts[0]);
            if (material == null) continue;
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            items.add(new ItemStack(material, amount));
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i).clone();
            int slot = i + 1;
            buttons.put(slot, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    // Empty
                }

                @Override
                public ItemStack getItemStack() {
                    return item.clone();
                }
            });
        }

        return buttons;
    }
}