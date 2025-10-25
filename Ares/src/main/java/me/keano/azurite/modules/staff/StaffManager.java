package me.keano.azurite.modules.staff;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.staff.extra.StaffItem;
import me.keano.azurite.modules.staff.extra.StaffItemAction;
import me.keano.azurite.modules.staff.extra.StaffReport;
import me.keano.azurite.modules.staff.extra.StaffRequest;
import me.keano.azurite.modules.staff.listener.StaffListener;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Pair;
import me.keano.azurite.modules.users.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StaffManager extends Manager {

    private final Map<UUID, Staff> staffMembers;
    private final Map<UUID, Staff> headstaffMembers;
    private final Map<Pair<String, List<String>>, StaffItem> staffItems;

    private final Set<UUID> vanished;
    private final Set<UUID> hvanished;
    private final Set<UUID> frozen;
    private final Set<UUID> staffBuild;
    private final Set<UUID> headstaffbuild;
    private final Set<UUID> hideStaff;

    private final List<StaffReport> reports;
    private final List<StaffRequest> requests;

    public StaffManager(HCF instance) {
        super(instance);

        this.staffMembers = new ConcurrentHashMap<>();
        this.headstaffMembers = new ConcurrentHashMap<>();
        this.staffItems = new HashMap<>();

        this.vanished = new HashSet<>();
        this.hvanished = new HashSet<>();
        this.frozen = new HashSet<>();
        this.staffBuild = new HashSet<>();
        this.headstaffbuild = new HashSet<>();
        this.hideStaff = new HashSet<>();

        this.reports = new ArrayList<>();
        this.requests = new ArrayList<>();

        new StaffListener(this);

        this.load();
    }

    @Override
    public void reload() {
        staffItems.clear();
        this.load();
    }

    @Override
    public void disable() {
        for (Staff staff : staffMembers.values()) {
            Player player = Bukkit.getPlayer(staff.getPlayer().getUniqueId());
            PlayerInventory inventory = player.getInventory();

            for (PotionEffect effect : staff.getEffects()) {
                player.addPotionEffect(effect);
            }

            inventory.setContents(staff.getContents());
            inventory.setArmorContents(staff.getArmorContents());
            player.updateInventory();
            player.setGameMode(staff.getGameMode());
        }
        for (Staff staff : headstaffMembers.values()) {
            Player player = Bukkit.getPlayer(staff.getPlayer().getUniqueId());
            PlayerInventory inventory = player.getInventory();

            for (PotionEffect effect : staff.getEffects()) {
                player.addPotionEffect(effect);
            }

            inventory.setContents(staff.getContents());
            inventory.setArmorContents(staff.getArmorContents());
            player.updateInventory();
            player.setGameMode(staff.getGameMode());
        }
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("STAFF_MODE.STAFF_ITEMS").getKeys(false)) {
            String path = "STAFF_MODE.STAFF_ITEMS." + key + ".";
            String action = getConfig().getString(path + "ACTION");
            String replace = getConfig().getString(path + "REPLACE");
            String name = getConfig().getString(path + "NAME");
            List<String> list = getConfig().getStringList(path + "LORE");
            String texture = getConfig().getString(path + "BASE64_TEXTURE", "");

            ItemBuilder builder;
            if (texture != null && !texture.isEmpty()) {
                builder = new ItemBuilder(ItemUtils.getCustomHead(texture));
            } else {
                builder = new ItemBuilder(ItemUtils.getMatItem(getConfig().getString(path + "MATERIAL")))
                        .data(this, getConfig().getInt(path + "DATA"));
            }

            ItemStack item = builder.setName(name).setLore(list).toItemStack();

            staffItems.put(new Pair<>(name, list), new StaffItem(
                    this, key,
                    action.isEmpty() ? null : StaffItemAction.valueOf(action),
                    replace.isEmpty() ? null : replace,
                    getConfig().getString(path + "COMMAND"), item,
                    getConfig().getInt(path + "SLOT"))
            );
        }
    }

    // Helper seguro para colocar ítems según slot de config (1..36, -1 casco)
    private void setItemInConfiguredSlot(Player player, ItemStack item, int cfgSlot) {
        if (player == null || item == null) return;
        PlayerInventory inv = player.getInventory();

        if (cfgSlot == -1) { // casco
            inv.setHelmet(item);
            return;
        }
        if (cfgSlot >= 1 && cfgSlot <= 36) { // 1..9 hotbar, 10..36 inventario
            inv.setItem(cfgSlot - 1, item);   // 0..35
            return;
        }
        // valores inválidos: no colocar
    }

    public void enableStaff(Player player) {
        PlayerInventory inventory = player.getInventory();
        Staff staff = new Staff(this, player, player.getGameMode());

        inventory.clear();
        inventory.setArmorContents(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
            staff.getEffects().add(effect);
        }

        for (StaffItem item : staffItems.values()) {
            if (item.getAction() == StaffItemAction.VANISH_ON
                    || item.getAction() == StaffItemAction.VANISH_ADMIN) {
                continue;
            }
            int slot = getItemSlot(player, item);
            setItemInConfiguredSlot(player, item.getItem(), slot);
        }

        // WorldEdit opcional
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Integer weSlot = user.getModModeSlots().get("WORLDEDIT");
        if (weSlot != null) {
            setItemInConfiguredSlot(player,
                    new ItemBuilder(Material.WOOD_AXE).setName("&bWorldEdit").toItemStack(),
                    weSlot);
        }

        getInstance().getWaypointManager().enableStaffModules(player);
        getInstance().getUserManager().getByUUID(player.getUniqueId()).updatePlaytime();

        player.updateInventory();
        if (player.hasPermission("zeus.headstaff")) {
            player.setGameMode(GameMode.CREATIVE);
        } else {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        enableVanish(player);
        staffMembers.put(player.getUniqueId(), staff);
    }

    public void enableHeadStaff(Player player) {
        PlayerInventory inventory = player.getInventory();
        Staff staff = new Staff(this, player, player.getGameMode());

        inventory.clear();
        inventory.setArmorContents(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
            staff.getEffects().add(effect);
        }

        for (StaffItem item : staffItems.values()) {
            if (item.getAction() == StaffItemAction.VANISH_ON
                    || item.getAction() == StaffItemAction.VANISH_ADMIN) {
                continue;
            }
            int slot = getItemSlot(player, item);
            setItemInConfiguredSlot(player, item.getItem(), slot);
        }

        // WorldEdit opcional
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Integer weSlot = user.getModModeSlots().get("WORLDEDIT");
        if (weSlot != null) {
            setItemInConfiguredSlot(player,
                    new ItemBuilder(Material.WOOD_AXE).setName("&bWorldEdit").toItemStack(),
                    weSlot);
        }

        getInstance().getWaypointManager().enableStaffModules(player);
        getInstance().getUserManager().getByUUID(player.getUniqueId()).updatePlaytime();

        player.updateInventory();
        player.setGameMode(GameMode.CREATIVE);

        headstaffMembers.put(player.getUniqueId(), staff);
        enableHeadVanish(player);
    }

    private int getItemSlot(Player player, StaffItem item) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Map<String, Integer> slots = user.getModModeSlots();
        return slots.getOrDefault(item.getName(), item.getSlot());
    }

    public void disableStaff(Player player) {
        Staff staff = staffMembers.get(player.getUniqueId());

        if (staff != null) {
            PlayerInventory inventory = player.getInventory();

            inventory.setContents(staff.getContents());
            inventory.setArmorContents(staff.getArmorContents());

            for (PotionEffect effect : staff.getEffects()) {
                player.addPotionEffect(effect);
            }

            getInstance().getWaypointManager().disableStaffModules(player);
            if (staff.getActionBarTask() != null) staff.getActionBarTask().destroy();

            player.updateInventory();

            if (player.hasPermission("zeus.headstaff")) {
                player.setGameMode(GameMode.CREATIVE);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.setFlying(false);
            }

            staffMembers.remove(player.getUniqueId());
            staffBuild.remove(player.getUniqueId());
            if (isHeadVanished(player)) {
                disableHeadVanish(player);
            } else {
                disableVanish(player);
            }
        }
    }

    public void disableHeadStaff(Player player) {
        Staff staff = headstaffMembers.get(player.getUniqueId());

        if (staff != null) {
            PlayerInventory inventory = player.getInventory();

            inventory.setContents(staff.getContents());
            inventory.setArmorContents(staff.getArmorContents());

            for (PotionEffect effect : staff.getEffects()) {
                player.addPotionEffect(effect);
            }

            getInstance().getWaypointManager().disableStaffModules(player);
            if (staff.getActionBarTask() != null) staff.getActionBarTask().destroy();

            player.updateInventory();
            player.setGameMode(staff.getGameMode());

            headstaffMembers.remove(player.getUniqueId());
            headstaffbuild.remove(player.getUniqueId());
            if (isHeadVanished(player)) {
                disableHeadVanish(player);
            } else {
                disableVanish(player);
            }
        }
    }

    public void enableVanish(Player player) {
        hvanished.remove(player.getUniqueId());
        vanished.add(player.getUniqueId());
        Utils.setCollidesWithEntities(player, false);

        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        if (isStaffEnabled(player) || isHeadStaffEnabled(player)) {
            for (StaffItem item : staffItems.values()) {
                if (item.getAction() == StaffItemAction.VANISH_OFF) {
                    setItemInConfiguredSlot(player, item.getItem(), getItemSlot(player, item));
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("azurite.vanish") &&
                    !hideStaff.contains(onlinePlayer.getUniqueId())) continue;
            onlinePlayer.hidePlayer(player);
        }
    }

    public void disableVanish(Player player) {
        vanished.remove(player.getUniqueId());
        Utils.setCollidesWithEntities(player, true);

        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        if (isHeadStaffEnabled(player) || isStaffEnabled(player)) {
            for (StaffItem item : staffItems.values()) {
                if (item.getAction() == StaffItemAction.VANISH_ON) {
                    setItemInConfiguredSlot(player, item.getItem(), getItemSlot(player, item));
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(player);
        }
    }

    public void enableHeadVanish(Player player) {
        vanished.remove(player.getUniqueId());
        hvanished.add(player.getUniqueId());
        Utils.setCollidesWithEntities(player, false);

        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        if (isHeadStaffEnabled(player) || isStaffEnabled(player)) {
            for (StaffItem item : staffItems.values()) {
                if (item.getAction() == StaffItemAction.VANISH_ADMIN) {
                    setItemInConfiguredSlot(player, item.getItem(), getItemSlot(player, item));
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("azurite.head.vanish") &&
                    !hideStaff.contains(onlinePlayer.getUniqueId())) continue;
            onlinePlayer.hidePlayer(player);
        }
    }

    public void disableHeadVanish(Player player) {
        hvanished.remove(player.getUniqueId());
        vanished.remove(player.getUniqueId());
        Utils.setCollidesWithEntities(player, true);

        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        if (isHeadStaffEnabled(player) || isStaffEnabled(player)) {
            for (StaffItem item : staffItems.values()) {
                if (item.getAction() == StaffItemAction.VANISH_ON) {
                    setItemInConfiguredSlot(player, item.getItem(), getItemSlot(player, item));
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(player);
        }
    }

    public void freezePlayer(Player player) {
        player.setWalkSpeed(0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
        frozen.add(player.getUniqueId());
    }

    public void unfreezePlayer(Player player) {
        player.setWalkSpeed(0.2F);
        player.setFoodLevel(20);
        player.setSprinting(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.JUMP);
        frozen.remove(player.getUniqueId());
    }

    public StaffItem getItem(ItemStack item) {
        List<String> lore = Collections.emptyList();
        String name = "";

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasLore()) {
                lore = meta.getLore();
            }

            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
        }

        return staffItems.get(new Pair<>(name, lore));
    }

    public StaffItem getItemByName(String name) {
        for (StaffItem item : staffItems.values()) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId()) || hvanished.contains(player.getUniqueId());
    }

    public boolean isHeadVanished(Player player) {
        return hvanished.contains(player.getUniqueId());
    }

    public boolean isStaffEnabled(Player player) {
        return staffMembers.containsKey(player.getUniqueId());
    }

    public boolean isHeadStaffEnabled(Player player) {
        return headstaffMembers.containsKey(player.getUniqueId());
    }

    public boolean isStaffBuild(Player player) {
        return staffBuild.contains(player.getUniqueId());
    }
    public boolean isHeadStaffBuild(Player player) {
        return headstaffbuild.contains(player.getUniqueId());
    }

    public boolean isHideStaff(Player player) {
        return hideStaff.contains(player.getUniqueId());
    }

    public boolean isFrozen(Player player) {
        return frozen.contains(player.getUniqueId());
    }
}
