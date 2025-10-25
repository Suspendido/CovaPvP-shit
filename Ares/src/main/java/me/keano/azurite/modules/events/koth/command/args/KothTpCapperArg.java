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
public class KothTpCapperArg extends Argument {

    public KothTpCapperArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "tpcapper",
                        "tpc"
                )
        );
        this.setPermissible("azurite.koth.tpcapper");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_TP_CAPPER.USAGE");
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
        Koth koth = getInstance().getKothManager().getKoth(args[0]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        if (koth.getCapturing() == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_TP_CAPPER.NO_CAPPER")
                    .replace("%koth%", koth.getName())
            );
            return;
        }

        Player capper = koth.getCapturing().getPlayer();
        long timeCapturing = koth.getRemaining(); // Time left
        long timeElapsed = koth.getMinutes() - timeCapturing; // Time transcurred

        player.teleport(capper.getLocation());

        // Convert time
        long minutes = (timeElapsed / 1000) / 60;
        long seconds = (timeElapsed / 1000) % 60;

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_TP_CAPPER.SUCCESS")
                .replace("%capper%", capper.getName())
                .replace("%player%", capper.getName())
                .replace("%koth%", koth.getName())
                .replace("%time%", String.format("%02d:%02d", minutes, seconds))
        );
    }
}
