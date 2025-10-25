package me.keano.azurite.modules.staff.listener;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.staff.extra.StaffItem;
import me.keano.azurite.modules.staff.extra.StaffItemAction;
import me.keano.azurite.modules.staff.menu.InspectionMenu;
import me.keano.azurite.modules.staff.menu.SilentViewMenu;
import me.keano.azurite.modules.timers.listeners.playertimers.CombatTimer;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StaffListener extends Module<StaffManager> {

    private final Cooldown interactCooldown;
    private final List<String> disabledFrozenCommands;

    public StaffListener(StaffManager manager) {
        super(manager);
        this.interactCooldown = new Cooldown(manager);
        this.disabledFrozenCommands = getConfig().getStringList("STAFF_MODE.DISABLED_COMMANDS_FROZEN");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (getManager().isFrozen(player)) {
            for (String disabledFrozenCommand : disabledFrozenCommands) {
                if (!e.getMessage().contains(disabledFrozenCommand)) continue;
                e.setCancelled(true);
                player.sendMessage(getLanguageConfig().getString("STAFF_MODE.NOT_ALLOWED_COMMAND"));
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTab(PlayerChatTabCompleteEvent e) {
        for (UUID uuid : getManager().getStaffMembers().keySet()) {
            Player staff = Bukkit.getPlayer(uuid);

            if (staff == null) continue;

            e.getTabCompletions().remove(staff.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeadTab(PlayerChatTabCompleteEvent e) {
        for (UUID uuid : getManager().getHeadstaffMembers().keySet()) {
            Player staff = Bukkit.getPlayer(uuid);

            if (staff == null) continue;

            e.getTabCompletions().remove(staff.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageFrozen(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (getManager().isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (getManager().isFrozen(player)) {
            e.setTo(e.getFrom());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropFrozen(PlayerDropItemEvent e) {
        Player player = e.getPlayer();

        if (getManager().isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupFrozen(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();

        if (getManager().isFrozen(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickFrozen(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (getManager().isFrozen(player)) {
            e.setCancelled(true);
        }
    }

//    @EventHandler(ignoreCancelled = true)
//    public void onQuitFrozen(PlayerQuitEvent e) {
//        Player player = e.getPlayer();
//
//        if (getManager().isFrozen(player)) {
//            getManager().unfreezePlayer(player);
//        }
//    }

    @EventHandler
    public void onQuitFrozenMessage(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!getManager().isFrozen(player)) {
            return;
        }

        System.out.println("[ALERT] " + player.getName() + " is frozen.");

        List<String> messages = getLanguageConfig().getStringList("STAFF_MODE.LOGOUT_FROZEN");
        if (messages == null || messages.isEmpty()) {
            return;
        }

        String rankPrefix = CC.t(getInstance().getRankHook().getRankPrefix(player));
        String rankColor = CC.t(getInstance().getRankHook().getRankColor(player));

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("zeus.staff")) {
                for (String message : messages) {
                    String formattedMessage = formatMessage(message, player.getName(), rankPrefix, rankColor);

                    System.out.println("[ALERT] Alert sent to " + staff.getName() + ": " + formattedMessage);

                    staff.sendMessage(formattedMessage);
                }

                staff.playSound(staff.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
            }
        }
    }
    private String formatMessage(String message, String playerName, String rankPrefix, String rankColor) {
        return message
                .replace("%player%", playerName)
                .replace("%rank-prefix%", rankPrefix)
                .replace("%rank-color%", rankColor);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block != null && block.getType().name().contains("SIGN") &&
                (getManager().isStaffEnabled(player) || getManager().isHeadStaffEnabled(player))) {
            e.setCancelled(true);
        }

        if (getManager().isStaffEnabled(player) || getManager().isHeadStaffEnabled(player)) {
            if (!getManager().isStaffBuild(player)) e.setCancelled(true);
            if (interactCooldown.hasCooldown(player)) return;

            interactCooldown.applyCooldownTicks(player, 100);
            handleClick(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        if (getManager().isStaffEnabled(damager) || getManager().isVanished(damager) ||
                getManager().isHeadStaffEnabled(damager) || getManager().isHeadVanished(damager)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInspect(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Player)) return;

        Player player = e.getPlayer();
        Player clicked = (Player) e.getRightClicked();

        if (getManager().isStaffEnabled(player) || getManager().isHeadStaffEnabled(player)) {
            ItemStack hand = getManager().getItemInHand(player);
            if (hand == null) return;

            StaffItem staffItem = getManager().getItem(hand);
            if (staffItem == null || staffItem.getAction() == null || interactCooldown.hasCooldown(player)) return;

            interactCooldown.applyCooldownTicks(player, 100);

            if (staffItem.getAction() == StaffItemAction.INSPECTION) {
                new InspectionMenu(getInstance().getMenuManager(), player, clicked).open();
            } else if (staffItem.getAction() == StaffItemAction.FREEZE) {
                player.chat("/freeze " + clicked.getName());
            } else if (!staffItem.getCommand().isEmpty() && staffItem.getAction() == StaffItemAction.INTERACT_PLAYER) {
                player.chat(staffItem.getCommand().replace("%player%", clicked.getName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageStaff(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if (getManager().isStaffEnabled(player) || getManager().isVanished(player) ||
                getManager().isHeadStaffEnabled(player) || getManager().isHeadVanished(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventory(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        if ((!getManager().isStaffBuild(player) && getManager().isStaffEnabled(player)) ||
                (!getManager().isHeadStaffBuild(player) && getManager().isHeadStaffEnabled(player))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        boolean isStaff = getManager().isStaffEnabled(player) && !getManager().isStaffBuild(player);
        boolean isHeadStaff = getManager().isHeadStaffEnabled(player) && !getManager().isHeadStaffBuild(player);

        if (isStaff || isHeadStaff) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("STAFF_MODE.DENY_BREAK"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        boolean isStaff = getManager().isStaffEnabled(player) && !getManager().isStaffBuild(player);
        boolean isHeadStaff = getManager().isHeadStaffEnabled(player) && !getManager().isHeadStaffBuild(player);

        if (isStaff || isHeadStaff) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("STAFF_MODE.DENY_PLACE"));
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (!getManager().isStaffBuild(player) && !getManager().isHeadStaffBuild(player) &&
                (getManager().isStaffEnabled(player) || getManager().isVanished(player) ||
                        getManager().isHeadStaffEnabled(player) || getManager().isHeadVanished(player) && getManager().isVanished(player))) {
            player.sendMessage(getLanguageConfig().getString("STAFF_MODE.DENY_DROP"));
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if (getManager().isStaffEnabled(player) || getManager().isVanished(player) ||
                getManager().isHeadStaffEnabled(player) || getManager().isHeadVanished(player) && getManager().isVanished(player)) {
            e.setCancelled(true);
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (getManager().isStaffEnabled(player) || getManager().isVanished(player) ||
                getManager().isHeadStaffEnabled(player) || getManager().isHeadVanished(player) && getManager().isVanished(player)) {
            e.getDrops().clear();
            e.setDroppedExp(0);
            player.chat("/staff");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if ((player.hasPermission("azurite.staff") || player.hasPermission("azurite.head.staff")) &&
                getConfig().getBoolean("STAFF_MODE.STAFF_MODE_ON_JOIN")) {
            Tasks.execute(getManager(), () -> player.chat("/staff"));
        }
    }

    @EventHandler
    public void onKickStaff(PlayerKickEvent e) {
        Player player = e.getPlayer();
        if (getManager().isStaffEnabled(player) || getManager().isHeadStaffEnabled(player)) {
            player.chat("/staff");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (getManager().isStaffEnabled(player) || getManager().isHeadStaffEnabled(player)) {
            player.chat("/staff");
        }
    }

    @EventHandler
    public void onPick(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        if (getManager().isStaffEnabled(player) || getManager().isHeadStaffEnabled(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInspect(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();

        if (clicked.getState() instanceof InventoryHolder) {
            boolean isStaff = getManager().isStaffEnabled(player) && getManager().isVanished(player) && !getManager().isStaffBuild(player);
            boolean isHeadStaff = (getManager().isHeadStaffEnabled(player) && getManager().isVanished(player) && !getManager().isHeadStaffBuild(player)) || getManager().isHeadVanished(player);

            if (isStaff || isHeadStaff) {
                new SilentViewMenu(getInstance().getMenuManager(), player, ((InventoryHolder) clicked.getState()).getInventory()).open();
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlate(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getAction() != Action.PHYSICAL) return;

        Player player = e.getPlayer();
        if (getManager().isStaffEnabled(player) || getManager().isVanished(player) ||
                getManager().isHeadStaffEnabled(player) || getManager().isHeadVanished(player) && getManager().isVanished(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        onKickStaff(e);
    }


    @EventHandler
    public void onJoinVanish(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("azurite.vanish")) return;

        for (UUID uuid : getManager().getVanished()) {
            Player vanished = Bukkit.getPlayer(uuid);

            if (vanished != null) {
                player.hidePlayer(vanished);
            }
        }
    }
    @EventHandler
    public void onJoinHeadVanish(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("azurite.head.vanish")) return;

        for (UUID uuid : getManager().getHvanished()) {
            Player vanished = Bukkit.getPlayer(uuid);

            if (vanished != null) {
                player.hidePlayer(vanished);
            }
        }
    }

    private void handleClick(Player player) {
        ItemStack hand = getManager().getItemInHand(player);

        if (hand != null) {
            StaffItem staffItem = getManager().getItem(hand);

            if (staffItem == null) return;

            StaffItemAction action = staffItem.getAction();

            if (action == StaffItemAction.VANISH_ON || action == StaffItemAction.VANISH_OFF || action == StaffItemAction.VANISH_ADMIN) {
                handleVanishClick(player, action);
                return;
            }

            if (staffItem.getReplacement() != null) {
                StaffItem replacement = getManager().getItemByName(staffItem.getReplacement());
                if (replacement != null) {
                    getManager().setItemInHand(player, replacement.getItem());
                }
            }

            if (!staffItem.getCommand().isEmpty()) {
                player.chat(staffItem.getCommand());
            }

            if (action != null) {
                switch (action) {
                    case COMBAT_TP:
                        CombatTimer combatTimer = getInstance().getTimerManager().getCombatTimer();
                        List<Player> combatTagged = Bukkit.getOnlinePlayers().stream()
                                .filter(combatTimer::hasTimer)
                                .collect(Collectors.toList());

                        if (combatTagged.isEmpty()) {
                            player.sendMessage(getLanguageConfig().getString("STAFF_MODE.COMBAT_TP_EMPTY"));
                            return;
                        }

                        Player target = combatTagged.get(ThreadLocalRandom.current().nextInt(combatTagged.size()));
                        player.teleport(target);
                        break;

                    case FREEZE:
                        player.chat(staffItem.getCommand());
                        break;

                    case INSPECTION:
                    case INTERACT_PLAYER:
                    default:
                        break;
                }
            }
        }
    }

    private void handleVanishClick(Player player, StaffItemAction action) {
        switch (action) {
            case VANISH_ON:
                getManager().enableVanish(player);
                StaffItem offItem = getManager().getItemByName("VANISH_OFF");
                if (offItem != null) {
                    getManager().setItemInHand(player, offItem.getItem());
                }
                sendVanishLog(player, "VANISH_ON");
                break;

            case VANISH_OFF:
                getManager().disableVanish(player);
                StaffItem nextItem = getManager().isHeadStaffEnabled(player)
                        ? getManager().getItemByName("VANISH_ADMIN")
                        : getManager().getItemByName("VANISH_ON");
                if (nextItem != null) {
                    getManager().setItemInHand(player, nextItem.getItem());
                }
                sendVanishLog(player, "VANISH_OFF");
                break;

            case VANISH_ADMIN:
                if (getManager().isHeadVanished(player)) {
                    getManager().disableHeadVanish(player);
                    StaffItem onItem = getManager().getItemByName("VANISH_ON");
                    if (onItem != null) {
                        getManager().setItemInHand(player, onItem.getItem());
                    }
                    sendVanishLog(player, "VANISH_OFF");
                } else {
                    getManager().enableHeadVanish(player);
                    StaffItem adminItem = getManager().getItemByName("VANISH_ADMIN");
                    if (adminItem != null) {
                        getManager().setItemInHand(player, adminItem.getItem());
                    }
                    sendVanishLog(player, "VANISH_ON");
                }
                break;
        }
    }

    private void sendVanishLog(Player player, String path) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("azurite.head")) continue;
            String[] messages = getLanguageConfig().getStringList("STAFF_LOGS." + path).toArray(new String[0]);
            for (String s : messages) {
                s = s
                        .replace("%player%", player.getName())
                        .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(player)))
                        .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(player)));
                online.sendMessage(s);
            }
        }
    }
}