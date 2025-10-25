package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothSetPointsArg extends Argument {

    public KothSetPointsArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setpoints"
                )
        );
        this.setPermissible("azurite.koth.setpoints");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETPOINTS.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Koth koth = getInstance().getKothManager().getKoth(args[0]);
        Integer points = Formatter.parseInt(args[1]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        if (points == null || points < 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        koth.setPointsReward(points);
        koth.save();

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETPOINTS.UPDATED_POINTS")
                .replace("%koth%", koth.getName())
                .replace("%points%", String.valueOf(points))
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getKothManager().getKoths().values()
                    .stream()
                    .map(Koth::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}
