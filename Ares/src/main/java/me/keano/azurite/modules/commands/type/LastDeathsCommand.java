package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.menu.LastDeathsMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LastDeathsCommand extends Command {

    public LastDeathsCommand(CommandManager manager) {
        super(
                manager,
                "lastdeaths"
        );
        this.setPermissible("azurite.lastdeaths");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("LAST_DEATHS_COMMAND.USAGE");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        User target = getInstance().getUserManager().getByName(args[0]);

        if (target == null) {
            sendMessage(player, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        new LastDeathsMenu(getInstance().getMenuManager(), player, target).open();
    }
}