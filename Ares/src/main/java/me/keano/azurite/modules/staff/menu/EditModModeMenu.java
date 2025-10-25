package me.keano.azurite.modules.staff.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.staff.extra.StaffItem;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 18/8/2025
 * Project: Zeus
 */

public class EditModModeMenu extends Menu {

    public EditModModeMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getConfig().getString("STAFF_MODE.EDIT_MODMODE_MENU.TITLE"),
                normalizeSize(manager.getConfig().getInt("STAFF_MODE.EDIT_MODMODE_MENU.SIZE")),
                false
        );
        setAllowInteract(true);
        player.sendMessage(CC.t("&e."));
        player.sendMessage(CC.t("&eLos items debajo de la linea solo se almacenarán."));
    }

    private static int normalizeSize(int cfg) {
        if (cfg < 27) cfg = 27;
        if (cfg > 54) cfg = 54;
        if (cfg % 9 != 0) cfg = ((cfg / 9) + 1) * 9;
        return cfg;
    }


    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        StaffManager staffManager = getInstance().getStaffManager();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        final int invSize = size;

        ItemStack divider = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 15).setName(" ").toItemStack();
        for (int slot1 = 10; slot1 <= 18 && slot1 <= invSize; slot1++) {
            final ItemStack d = divider;
            buttons.put(slot1, new Button() {
                @Override public void onClick(InventoryClickEvent e) { e.setCancelled(true); }
                @Override public ItemStack getItemStack() { return d; }
            });
        }

        int storageSlot1 = 19;

        for (StaffItem item : staffManager.getStaffItems().values()) {
            String name = item.getName();
            if (name.equalsIgnoreCase("VANISH_OFF") || name.equalsIgnoreCase("VANISH_ADMIN")) continue;

            int savedHuman = user.getModModeSlots().getOrDefault(name, item.getSlot());
            final ItemStack s = item.getItem();

            if (savedHuman >= 1 && savedHuman <= 9 && savedHuman <= invSize) {
                int slot1 = savedHuman;
                buttons.put(slot1, new Button() {
                    @Override public void onClick(InventoryClickEvent e) {}
                    @Override public ItemStack getItemStack() { return s; }
                });
            } else if (storageSlot1 <= invSize) {
                int putAt1 = storageSlot1++;
                buttons.put(putAt1, new Button() {
                    @Override public void onClick(InventoryClickEvent e) {  }
                    @Override public ItemStack getItemStack() { return s; }
                });
            }
        }

        if (player.hasPermission("zeus.headstaff")) {
            ItemStack worldEdit = new ItemBuilder(Material.WOOD_AXE).setName("&bWorldEdit").toItemStack();
            int savedHuman = user.getModModeSlots().getOrDefault("WORLDEDIT", storageSlot1 + 1);
            final ItemStack we = worldEdit;

            if (savedHuman >= 1 && savedHuman <= 9 && savedHuman <= invSize) {
                buttons.put(savedHuman, new Button() {
                    @Override public void onClick(InventoryClickEvent e) { }
                    @Override public ItemStack getItemStack() { return we; }
                });
            } else if (storageSlot1 <= invSize) {
                int putAt1 = storageSlot1++;
                buttons.put(putAt1, new Button() {
                    @Override public void onClick(InventoryClickEvent e) { }
                    @Override public ItemStack getItemStack() { return we; }
                });
            }
        }

        return buttons;
    }


    @Override
    public void onClickOwn(InventoryClickEvent e) {
        e.setCancelled(false);
    }

    @Override
    public void onClose() {
        StaffManager staffManager = getInstance().getStaffManager();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Map<String, Integer> map = user.getModModeSlots();
        map.clear();

        int topRowMax = Math.min(9, inventory.getSize());
        for (StaffItem item : staffManager.getStaffItems().values()) {
            String name = item.getName();
            if (name.equalsIgnoreCase("VANISH_OFF") || name.equalsIgnoreCase("VANISH_ADMIN")) {
                continue;
            }
            ItemStack target = item.getItem();
            boolean found = false;
            for (int i = 0; i < topRowMax; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack == null || stack.getType() == Material.AIR) continue;
                if (stack.isSimilar(target)) {
                    int slotHuman = i + 1;
                    map.put(item.getName(), slotHuman);

                    if (item.getReplacement() != null) {
                        map.put(item.getReplacement(), slotHuman);
                    }
                    if (item.getName().equalsIgnoreCase("VANISH_ON")) {
                        map.put("VANISH_ADMIN", slotHuman);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                map.put(item.getName(), 0);
                if (item.getReplacement() != null) {
                    map.put(item.getReplacement(), 0);
                }
                if (item.getName().equalsIgnoreCase("VANISH_ON")) {
                    map.put("VANISH_ADMIN", 0);
                }
            }
        }

        if (player.hasPermission("zeus.headstaff")) {
            ItemStack worldEdit = new ItemBuilder(Material.WOOD_AXE).setName("&bWorldEdit").toItemStack();
            boolean found = false;
            for (int i = 0; i < topRowMax; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack != null && stack.isSimilar(worldEdit)) {
                    map.put("WORLDEDIT", i + 1);
                    found = true;
                    break;
                }
            }
            if (!found) map.put("WORLDEDIT", 0);
        }

        user.save();
        player.sendMessage(CC.t("&aSuccessfully saved your changes."));
    }
}
