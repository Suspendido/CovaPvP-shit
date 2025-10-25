package me.keano.azurite.modules.users.menu;

import me.keano.azurite.modules.discord.type.RestoreWebhook;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class InventoryMenu extends Menu {

    private final StoredInventory inventory;
    private final Player target;
    private final String targetName;
    private final ItemStack pane;
    private final boolean restore;
    private final Runnable backAction;

    private InventoryMenu(MenuManager manager, Player player, Player target, String targetName, StoredInventory inventory, boolean restore, Runnable backAction) {
        super(
                manager,
                player,
                manager.getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.TITLE")
                        .replace("%date%", Formatter.formatDate(inventory.getDate())),
                manager.getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.SIZE"),
                false
        );
        this.inventory = inventory;
        this.target = target;
        this.targetName = targetName;
        this.restore = restore;
        this.backAction = backAction;
        this.pane = new ItemBuilder(ItemUtils.getMat(getConfig()
                .getString("INVENTORY_RESTORE.INVENTORY_MENU.FILLER.MATERIAL")))
                .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.FILLER.NAME"))
                .data(manager, getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.FILLER.DATA"))
                .toItemStack();
    }

    public InventoryMenu(MenuManager manager, Player player, Player target, StoredInventory inventory, Runnable backAction) {
        this(manager, player, target, target.getName(), inventory, true, backAction);
    }

    public InventoryMenu(MenuManager manager, Player player, String targetName, StoredInventory inventory, Runnable backAction) {
        this(manager, player, null, targetName, inventory, false, backAction);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Normal items
        for (int i = 0; i < inventory.getContents().length; i++) {
            int copy = i;

            buttons.put(i + 1, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return inventory.getContents()[copy];
                }
            });
        }

        if (restore && target != null) {
            // Confirm inventory
            buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.SLOT"), new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    PlayerInventory toRestore = target.getInventory();
                    toRestore.setContents(inventory.getContents());
                    toRestore.setArmorContents(inventory.getArmor());
                    player.sendMessage(getLanguageConfig().getString("RESTORE_COMMAND.RESTORED")
                            .replace("%player%", target.getName())
                    );
                    target.sendMessage(getLanguageConfig().getString("RESTORE_COMMAND.TARGET")
                            .replace("%player%", player.getName())
                    );
                    player.closeInventory();
                    target.updateInventory();

                    new RestoreWebhook(getManager(), player, target, inventory).executeAsync();
                }

                @Override
                public ItemStack getItemStack() {
                    ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                            .getString("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.MATERIAL")))
                            .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.NAME"))
                            .setLore(getConfig().getStringList("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.LORE"))
                            .data(getManager(), getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.DATA"));

                    return builder.toItemStack();
                }
            });
        }

        // Back Button
        buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                player.closeInventory();
                if (backAction != null) backAction.run();
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.MATERIAL")))
                        .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.NAME"))
                        .setLore(getConfig().getStringList("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.LORE"))
                        .data(getManager(), getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.DATA"));

                return builder.toItemStack();
            }
        });

        // Armor
        for (int i = 0; i < 4; i++) {
            String part = (i == 0 ? "HELMET" : i == 1 ? "CHESTPLATE" : i == 2 ? "LEGGINGS" : "BOOTS");

            buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU." + part + "_SLOT"), new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return part.equals("HELMET") ?
                            inventory.getArmor()[3] : part.equals("CHESTPLATE") ?
                            inventory.getArmor()[2] : part.equals("LEGGINGS") ?
                            inventory.getArmor()[1] : inventory.getArmor()[0];
                }
            });
        }

        // Potion Effects
        buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.POTIONS_VIEWER.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("INVENTORY_RESTORE.INVENTORY_MENU.POTIONS_VIEWER.MATERIAL")))
                        .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.POTIONS_VIEWER.NAME"))
                        .data(getManager(), getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.POTIONS_VIEWER.DATA"));

                for (PotionEffect effect : inventory.getEffects()) {
                    int duration = effect.getDuration();
                    int amplifier = effect.getAmplifier() + 1;
                    long durationLong = (duration / 20L) * 1000L;
                    builder.addLoreLine(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.POTIONS_VIEWER.FORMAT")
                            .replace("%effect%", Utils.convertName(effect.getType()))
                            .replace("%time%", (duration > 1000000 ? "Permanent" : Formatter.getRemaining(durationLong, false)))
                            .replace("%amplifier%", String.valueOf(amplifier))
                    );
                }

                return builder.toItemStack();
            }
        });

        // Player Info
        buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.PLAYER_INFO.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("INVENTORY_RESTORE.INVENTORY_MENU.PLAYER_INFO.MATERIAL")))
                        .setSkullOwner(targetName)
                        .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.PLAYER_INFO.NAME")
                                .replace("%player%", targetName))
                        .data(getManager(), getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.PLAYER_INFO.DATA"));

                for (String s : getConfig().getStringList("INVENTORY_RESTORE.INVENTORY_MENU.PLAYER_INFO.LORE")) {
                    builder.addLoreLine(s.replace("%health%", Formatter.formatHealth(inventory.getHealth())));
                }

                return builder.toItemStack();
            }
        });

        // Some fillers
        for (int i = 37; i <= 45; i++) {
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return pane;
                }
            });
        }

        return buttons;
    }
}