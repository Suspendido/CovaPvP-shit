package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothEndArg extends Argument {

    public KothEndArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "end",
                        "stop"
                )
        );
        this.setPermissible("azurite.koth.end");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_END.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        // all arg to stop all
        if (args[0].equalsIgnoreCase("all")) {
            int stoppedCount = 0;

            for (Koth koth : getInstance().getKothManager().getKoths().values()) {
                if (koth.isActive()) {
                    koth.end();
                    koth.save();
                    stoppedCount++;
                }
            }

            if (stoppedCount > 0) {
                sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_END.STOPPED_ALL")
                        .replace("%count%", String.valueOf(stoppedCount))
                );
            } else {
                sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_END.NO_ACTIVE_EVENTS"));
            }
            return;
        }

        // Original code for stopping a specific KoTH
        Koth koth = getInstance().getKothManager().getKoth(args[0]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        if (!koth.isActive()) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_END.NOT_ACTIVE"));
            return;
        }

        koth.end();
        koth.save(); // save the active:false.

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_END.STOPPED")
                .replace("%koth%", koth.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];

            // add all to tab
            List<String> completions = getInstance().getKothManager().getKoths().values()
                    .stream()
                    .map(Koth::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());

            if ("all".regionMatches(true, 0, string, 0, string.length())) {
                completions.add("all");
            }

            return completions;
        }

        return super.tabComplete(sender, args);
    }
}