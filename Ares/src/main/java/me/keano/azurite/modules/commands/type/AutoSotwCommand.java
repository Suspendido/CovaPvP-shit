package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 12/03/2025
 * Project: Zeus
 */

public class AutoSotwCommand extends Command {

    public AutoSotwCommand(CommandManager manager) {
        super(
                manager,
                "autosotw"
        );
        this.setPermissible("zeus.command.autosotw");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "sotwautomation",
                "autostart",
                "sotwmanager"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("AUTOSOTW_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            System.out.println("No console");
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("zeus.command.autosotw")) {
            player.sendMessage(CC.t(getLanguageConfig().getString("GLOBAL_COMMANDS.INSUFFICIENT_PERMISSION")));
            return;
        }

        if (args.length == 0) {
            for (String line : getLanguageConfig().getStringList("AUTOSOTW_COMMAND.USAGE")) {
                player.sendMessage(CC.t(line));
            }
            return;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "start":
                executeStartCommands(player);
                break;
            case "cancel":
                executeCancelCommands(player);
                break;
            case "list":
                displayCommandList(player);
                break;
            case "add":
                if (args.length < 3) {
                    player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.ADD_USAGE")));
                    return;
                }
                String category = args[1].toLowerCase();
                StringBuilder commandToAdd = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    commandToAdd.append(args[i]).append(" ");
                }
                String finalCommand = commandToAdd.toString().trim();
                addCommand(player, category, finalCommand);
                break;
            case "remove":
                if (args.length != 3) {
                    player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.REMOVE_USAGE")));
                    return;
                }
                String removeCategory = args[1].toLowerCase();
                int commandIndex;
                try {
                    commandIndex = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.INVALID_NUMBER")));
                    return;
                }
                removeCommand(player, removeCategory, commandIndex);
                break;
            default:
                for (String line : getLanguageConfig().getStringList("AUTOSOTW_COMMAND.USAGE")) {
                    player.sendMessage(CC.t(line));
                }
                break;
        }
    }

    private void executeStartCommands(Player player) {
        List<String> startCommands = getConfig().getStringList("autosotw.commands.start");
        if (startCommands.isEmpty()) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.NO_START_COMMANDS")));
            return;
        }

        for (String command : startCommands) {
            player.performCommand(command);
        }

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.START_ADMIN")));
    }

    private void executeCancelCommands(Player player) {
        List<String> cancelCommands = getConfig().getStringList("autosotw.commands.cancel");
        if (cancelCommands.isEmpty()) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.NO_CANCEL_COMMANDS")));
            return;
        }

        for (String command : cancelCommands) {
            player.performCommand(command);
        }

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.CANCEL_ADMIN")));
    }

    private void displayCommandList(Player player) {
        List<String> startCommands = getConfig().getStringList("autosotw.commands.start");
        List<String> cancelCommands = getConfig().getStringList("autosotw.commands.cancel");

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.LIST_HEADER")));

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.START_COMMANDS_HEADER")));
        if (startCommands.isEmpty()) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.NO_START_COMMANDS")));
        } else {
            for (int i = 0; i < startCommands.size(); i++) {
                player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.COMMAND_FORMAT")
                        .replace("{number}", String.valueOf(i + 1))
                        .replace("{command}", startCommands.get(i))));
            }
        }

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.CANCEL_COMMANDS_HEADER")));
        if (cancelCommands.isEmpty()) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.NO_CANCEL_COMMANDS")));
        } else {
            for (int i = 0; i < cancelCommands.size(); i++) {
                player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.COMMAND_FORMAT")
                        .replace("{number}", String.valueOf(i + 1))
                        .replace("{command}", cancelCommands.get(i))));
            }
        }
    }

    private void addCommand(Player player, String category, String command) {
        if (!category.equals("start") && !category.equals("cancel")) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.INVALID_CATEGORY")));
            return;
        }

        String path = "autosotw.commands." + category;
        FileConfiguration config = getConfig();
        List<String> commands = new ArrayList<>(config.getStringList(path));

        if (commands.contains(command)) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.COMMAND_ALREADY_EXISTS")));
            return;
        }

        commands.add(command);
        config.set(path, commands);
        saveConfig();

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.COMMAND_ADDED")
                .replace("{category}", category)
                .replace("{command}", command)));
    }

    private void removeCommand(Player player, String category, int index) {
        if (!category.equals("start") && !category.equals("cancel")) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.INVALID_CATEGORY")));
            return;
        }

        String path = "autosotw.commands." + category;
        FileConfiguration config = getConfig();
        List<String> commands = new ArrayList<>(config.getStringList(path));

        if (index < 1 || index > commands.size()) {
            player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.INVALID_INDEX")));
            return;
        }

        String removedCommand = commands.get(index - 1);

        commands.remove(index - 1);
        config.set(path, commands);
        saveConfig();

        player.sendMessage(CC.t(getLanguageConfig().getString("AUTOSOTW_COMMAND.COMMAND_REMOVED")
                .replace("{category}", category)
                .replace("{command}", removedCommand)));
    }

    private void saveConfig() {
        try {
            getConfig().save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}