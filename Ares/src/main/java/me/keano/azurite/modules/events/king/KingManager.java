package me.keano.azurite.modules.events.king;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.king.listener.KingListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class KingManager extends Manager {

    private Player king;
    private String reward;
    private long startedTime;
    private int flightLimit;

    public KingManager(HCF instance) {
        super(instance);

        this.king = null;
        this.reward = null;
        this.startedTime = 0L;
        this.flightLimit = 5;

        new KingListener(this);
    }

    public boolean isActive() {
        return king != null;
    }

    public void startKing(Player player, String reward) {
        this.king = player;
        this.reward = reward;
        this.startedTime = System.currentTimeMillis();

        getInstance().getKitManager().getKit("ktk").equip(player);

        // Allow flight with small limit
        player.setAllowFlight(true);
        player.setFlying(true);

        // Give special king items
        player.getInventory().addItem(createLaser());
        player.getInventory().addItem(createDash());

        for (String s : getLanguageConfig().getStringList("KING_EVENTS.BROADCAST_START")) {
            Bukkit.broadcastMessage(s
                    .replace("%player%", king.getName())
                    .replace("%reward%", reward)
            );
        }
    }

    public void stopKing(boolean forced) {
        String killer = (king.getKiller() == null ? "Unknown" : king.getKiller().getName());
        long death = System.currentTimeMillis() - startedTime;

        for (String s : getLanguageConfig().getStringList("KING_EVENTS.BROADCAST_" + (forced ? "END" : "KILL"))) {
            king.getInventory().clear();
            king.getInventory().setArmorContents(null);
            king.setAllowFlight(false);
            king.setFlying(false);
            Bukkit.broadcastMessage(s
                    .replace("%player%", king.getName())
                    .replace("%reward%", reward)
                    .replace("%killer%", killer)
                    .replace("%time%", Formatter.formatDetailed(death))
            );
        }

        this.king = null;
        this.reward = null;
        this.startedTime = 0L;
    }

    private ItemStack createLaser() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "King Laser");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Dispara un rayo");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDash() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "King Dash");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Impulsa hacia adelante");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isLaser(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return (ChatColor.GOLD + "King Laser").equals(meta.getDisplayName());
    }

    public boolean isDash(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return (ChatColor.GOLD + "King Dash").equals(meta.getDisplayName());
    }
}