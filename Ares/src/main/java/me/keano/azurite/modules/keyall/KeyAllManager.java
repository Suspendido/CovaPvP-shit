package me.keano.azurite.modules.keyall;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 15/02/2025
 * Project: Zeus
 */

public class KeyAllManager extends Manager {

    private boolean isActive;
    private List<ItemStack> loot;
    private Set<UUID> claimedPlayers;

    public KeyAllManager(HCF instance) {
        super(instance);
        this.isActive = false;
        this.loot = new ArrayList<>();
        this.claimedPlayers = new HashSet<>();
        loadLootFromConfig();
    }

    public boolean isActive() {
        return isActive;
    }

    public void toggleActive() {
        isActive = !isActive;
        if (!isActive) {
            claimedPlayers.clear();
        }
        saveLootToConfig();
    }

    public List<ItemStack> getLoot() {
        return loot;
    }

    public void setLoot(List<ItemStack> loot) {
        this.loot = loot;
        saveLootToConfig();
    }

    public List<ItemStack> redeemLoot(Player player) {
        UUID playerId = player.getUniqueId();
        if (claimedPlayers.contains(playerId)) {
            return Collections.emptyList();
        }
        claimedPlayers.add(playerId);
        return new ArrayList<>(loot);
    }

    private void loadLootFromConfig() {
        FileConfiguration config = getConfig();
        loot = (List<ItemStack>) config.getList("keyall.loot", new ArrayList<>());
    }

    private void saveLootToConfig() {
        FileConfiguration config = getConfig();
        config.set("keyall.loot", loot);
        getConfig().save();
    }
}