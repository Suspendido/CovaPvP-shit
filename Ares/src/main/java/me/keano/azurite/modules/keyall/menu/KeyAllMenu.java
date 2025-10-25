package me.keano.azurite.modules.keyall.menu;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.keyall.KeyAllManager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 15/02/2025
 * Project: Zeus
 */

public class KeyAllMenu implements Listener {

    private final Inventory inventory;
    private final KeyAllManager keyAllManager;
    private final Map<Player, ItemStack[]> savedLoot;

    public KeyAllMenu(HCF plugin, KeyAllManager keyAllManager) {
        this.inventory = Bukkit.createInventory(null, 27, "KeyAll Loot");
        this.keyAllManager = keyAllManager;
        this.savedLoot = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        ItemStack[] loot = keyAllManager.getLoot().toArray(new ItemStack[0]);
        inventory.setContents(loot);

        inventory.setItem(22, new ItemBuilder(Material.EMERALD)
                .setName("§aSave Loot")
                .setLore("§7Click Q to save loot.")
                .toItemStack());

        player.openInventory(inventory);
    }

    private void saveLoot(Player player) {
        ItemStack[] contents = new ItemStack[inventory.getSize()];
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 22) {
                contents[i] = new ItemStack(Material.AIR);
                continue;
            }
            ItemStack item = inventory.getItem(i);
            contents[i] = (item != null && item.getType() != Material.AIR) ? item : new ItemStack(Material.AIR);
        }
        keyAllManager.setLoot(Arrays.asList(contents));
        player.sendMessage(CC.t("&aSuccessfully saved loot."));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("KeyAll Loot")) {
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            if (!player.hasPermission("zeus.keyall.admin")) {
                event.setCancelled(true);
                player.sendMessage(Config.INSUFFICIENT_PERM);
                return;
            }

            if (slot == 22) {
                event.setCancelled(true);
                saveLoot(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("KeyAll Loot")) {
            Player player = (Player) event.getPlayer();
            saveLoot(player);
        }
    }

    public void initialize() {
    }
}