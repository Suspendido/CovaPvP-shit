package me.keano.azurite.modules.users.menu;

import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LastKillsMenu extends PaginatedMenu {

    private final User target;

    public LastKillsMenu(MenuManager manager, Player player, User target) {
        super(
                manager,
                player,
                manager.getConfig().getString("LAST_KILLS.MENU.TITLE").replace("%player%", target.getName()),
                manager.getConfig().getInt("LAST_KILLS.MENU.SIZE"),
                false,
                20
        );
        this.target = target;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<String> kills = target.getLastKills();
        int number = 1;
        for (int idx = kills.size() - 1; idx >= 0; idx--) {
            String kill = kills.get(idx);
            String[] split = kill.split(";");
            if (split.length < 10) continue; // legacy format
            String victim = split[0];
            String world = split[1];
            double x = Double.parseDouble(split[2]);
            double y = Double.parseDouble(split[3]);
            double z = Double.parseDouble(split[4]);
            long time = Long.parseLong(split[5]);
            String deathMsg = split[6];
            String team = split[7];
            double dtrBefore = Double.parseDouble(split[8]);
            double dtrAfter = Double.parseDouble(split[9]);
            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            StoredInventory inv = split.length >= 11 ? StoredInventory.fromString(split[10]) : null;
            final int num = number;

            buttons.put(number, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    player.closeInventory();
                    if (e.isRightClick()) {
                        player.teleport(loc);
                    } else {
                        if (inv != null) {
                            new InventoryMenu(getManager(), player, target.getName(), inv, () -> new LastKillsMenu(getManager(), player, target).open()).open();
                        } else {
                            player.sendMessage(deathMsg);
                        }
                    }
                }

                @Override
                public ItemStack getItemStack() {
                    List<String> lore = getConfig().getStringList("LAST_KILLS.MENU.ITEM.LORE");
                    lore.replaceAll(s -> s
                            .replace("%player%", victim)
                            .replace("%kill%", victim)

                            .replace("%deathmessage%", deathMsg)
                            .replace("%date%", Formatter.formatDate(new Date(time)))
                            .replace("%x%", String.valueOf((int) x))
                            .replace("%y%", String.valueOf((int) y))
                            .replace("%z%", String.valueOf((int) z))
                            .replace("%team%", team)
                            .replace("%dtr_before%", Formatter.formatDtr(dtrBefore))
                            .replace("%dtr_after%", Formatter.formatDtr(dtrAfter))
                            .replace("%number%", String.valueOf(num))
                    );
                    return new ItemBuilder(ItemUtils.getMatItem("head_" + victim))
                            .setName(getConfig().getString("LAST_KILLS.MENU.ITEM.NAME")
                                    .replace("%number%", String.valueOf(num))
                                    .replace("%player%", victim)
                                    .replace("%kill%", victim))
                            .setLore(lore)
                            .toItemStack();
                }
            });
            number++;
        }

        return buttons;
    }
}
