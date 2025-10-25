package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.menu.EditModModeMenu;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EditModModeCommand extends Command {

    public EditModModeCommand(CommandManager manager) {
        super(manager, "editmodmode");
        this.setPermissible("azurite.staff");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sendMessage(sender, "&cUsage: /editmodmode reset <player>");
                return;
            }

            User user = getInstance().getUserManager().getByName(args[1]);
            if (user == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND);
                return;
            }

            user.getModModeSlots().clear();
            user.save();
            sendMessage(sender, "&aReset mod mode items for &f" + user.getName() + "&a.");
            return;
        }

        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        new EditModModeMenu(getInstance().getMenuManager(), player).open();
    }
}
