package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothRenameArg extends Argument {

    public KothRenameArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList("rename")
        );
        this.setPermissible("azurite.koth.rename");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_RENAME.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        String oldName = args[0];
        Koth koth = getInstance().getKothManager().getKoth(oldName);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", oldName));
            return;
        }

        String newName = args[1];

        // Check if a Koth with the new name already exists
        Koth existingKoth = getInstance().getKothManager().getKoth(newName);
        if (existingKoth != null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_ALREADY_EXISTS")
                    .replace("%koth%", newName));
            return;
        }

        // Remove the Koth from the manager using the old name
        getInstance().getKothManager().getKoths().remove(oldName);

        // Set the new name and save the Koth
        koth.setName(newName);
        koth.save();

        // Add the Koth back into the manager with the new name
        getInstance().getKothManager().getKoths().put(newName, koth);

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_RENAME.SUCCESS")
                .replace("%old_name%", oldName)
                .replace("%new_name%", newName));
    }
}
