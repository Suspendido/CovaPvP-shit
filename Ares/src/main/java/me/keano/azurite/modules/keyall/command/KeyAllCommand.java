package me.keano.azurite.modules.keyall.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.keyall.menu.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 15/02/2025
 * Project: Zeus
 */

public class KeyAllCommand extends Command {



    public KeyAllCommand(CommandManager manager) {
        super(manager, "keyall");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("keyall", "keys", "ka");
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KEYALL_COMMAND.USAGE");
    }

    @Override
    public void sendUsage(CommandSender sender) {
        for (String line : getLanguageConfig().getStringList("KEYALL_COMMAND.USAGE")) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (args.length == 1) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "redeem":
                    handleRedeem(player);
                    break;

                case "open":
                    if (player.hasPermission("zeus.keyall.admin")) {
                        handleOpen(player);
                    } else {
                        player.sendMessage(Config.INSUFFICIENT_PERM);
                    }
                    break;

                case "toggle":
                    if (player.hasPermission("zeus.keyall.admin")) {
                        handleToggle(player);
                    } else {
                        player.sendMessage(Config.INSUFFICIENT_PERM);
                    }
                    break;

                default:
                    player.sendMessage(getLanguageConfig().getString("KEYALL_COMMAND.INVALID"));
                    break;
            }
        } else {
            player.sendMessage(getLanguageConfig().getString("KEYALL_COMMAND.INVALID"));
        }
    }

    private void handleRedeem(Player player) {
        if (!getInstance().getKeyAllManager().isActive()) {
            sendMessage(player, getLanguageConfig().getString("KEYALL_COMMAND.NOT_ACTIVE"));
            return;
        }

        List<ItemStack> loot = getInstance().getKeyAllManager().redeemLoot(player);
        if (loot.isEmpty()) {
            player.sendMessage(getLanguageConfig().getString("KEYALL_COMMAND.ALREADY_REDEEMED"));
            return;
        }

        loot.forEach(item -> {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.sendMessage(getLanguageConfig().getString("KEYALL_COMMAND.NOT_SPACE"));
            }
        });

        player.sendMessage(getLanguageConfig().getString("KEYALL_COMMAND.SUCCESS"));
    }

    private void handleOpen(Player player) {
        KeyAllMenu menu = new KeyAllMenu(getInstance(), getInstance().getKeyAllManager());
        menu.initialize();
        menu.openMenu(player);
    }

    private void handleToggle(Player player) {
        getInstance().getKeyAllManager().toggleActive();
        String status = getInstance().getKeyAllManager().isActive() ? "active" : "inactive";
        player.sendMessage(getLanguageConfig().getString("KEYALL_COMMAND.STATUS_CHANGE").replace("%status%", status));
    }
}