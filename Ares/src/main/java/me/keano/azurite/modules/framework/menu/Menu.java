package me.keano.azurite.modules.framework.menu;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public abstract class Menu extends Module<MenuManager> {

    protected Map<Integer, Button> buttons;
    protected Player player;
    protected Inventory inventory;
    protected BukkitTask updater;
    protected String title;
    protected int size;

    protected ItemStack filler;
    protected boolean fillEnabled;
    protected boolean allowInteract;

    public Menu(MenuManager manager, Player player, String title, int size, boolean update) {
        super(manager);
        this.player = player;
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.updater = (update ? Bukkit.getScheduler().runTaskTimer(getInstance(), this::update, 0L, 10L) : null);
        this.fillEnabled = false;
        this.allowInteract = false;
    }

    public Menu(MenuManager manager, Player player, String title, boolean update, Inventory inventory) {
        super(manager);
        this.player = player;
        this.inventory = (inventory instanceof DoubleChestInventory ?
                Bukkit.createInventory(null, 54, title) :
                Bukkit.createInventory(null, inventory.getType(), title));
        this.updater = (update ? Bukkit.getScheduler().runTaskTimer(getInstance(), this::update, 0L, 10L) : null);
        this.fillEnabled = false;
        this.allowInteract = false;
    }

    public void open() {
        this.buttons = getButtons(player);

        // Set the first items
        for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
            inventory.setItem(entry.getKey() - 1, entry.getValue().getItemStack());
        }

        // Fill null/air spots with our filler
        if (fillEnabled) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);

                if (item == null || item.getType() == Material.AIR) {
                    inventory.setItem(i, filler);
                }
            }
        }

        // Open and add to map
        player.openInventory(inventory);
        getManager().getMenus().put(player.getUniqueId(), this);
    }

    public void update() {
        if (!player.isOnline()) {
            destroy(); // They aren't online so just destroy it.
            return;
        }

        // "Re-Fetch" the buttons and cache them again
        Map<Integer, Button> buttons = (this.buttons = getButtons(player));

        // Update the items in our inventory
        for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
            inventory.setItem(entry.getKey() - 1, entry.getValue().getItemStack());
        }

        // Update filler
        if (fillEnabled) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);

                if (item == null || item.getType() == Material.AIR) {
                    inventory.setItem(i, filler);
                }
            }
        }
    }

    public void destroy() {
        // Lower ram usage
        buttons.clear();
        inventory.clear();

        // Remove from map
        getManager().getMenus().remove(player.getUniqueId());

        // Cancel updater
        if (updater != null) {
            updater.cancel();
        }
    }

    public void onClick(InventoryClickEvent e) {
        if (!allowInteract) {
            e.setCancelled(true);
        }
    }

    public void onClickOwn(InventoryClickEvent e) {
    }

    public void onClose() {
    }

    public abstract Map<Integer, Button> getButtons(Player player);
}