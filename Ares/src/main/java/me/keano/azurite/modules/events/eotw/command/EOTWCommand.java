package me.keano.azurite.modules.events.eotw.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.eotw.EOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.deco.utils.FormatterTime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EOTWCommand extends Command {

    public EOTWCommand(CommandManager manager) {
        super(
                manager,
                "EOTW"
        );
        this.setPermissible("azurite.eotw");
        this.completions.add(new TabCompletion(Arrays.asList("start", "cancel", "extend", "whitelist"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("EOTW_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        EOTWManager eotwManager = getInstance().getEotwManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long time = Formatter.parse(args[1]);

                if (time == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                eotwManager.startPreEOTW(time);
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.STARTED")
                        .replace("%time%", args[1])
                );

// Send title to all online players when event starts
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location location = player.getLocation();

                    String titleTemplate = getLanguageConfig().getString("EOTW_COMMAND.STARTED_TITLE");
                    String subtitleTemplate = getLanguageConfig().getString("EOTW_COMMAND.STARTED_SUBTITLE");

                    String soundName = getLanguageConfig().getString("EOTW_COMMAND.STARTED_SOUND", "NOTE_PLING");
                   // Sound eventSound = Sound.valueOf(soundName.toUpperCase()); // Convert to uppercase for enum

                    if (titleTemplate != null && subtitleTemplate != null) {
                        String formattedTime = FormatterTime.format(time);
                        String title = titleTemplate.replace("%time%", formattedTime);
                        String subtitle = subtitleTemplate.replace("%time%", formattedTime);

                        player.sendTitle(title, subtitle);
                        player.playSound(location, Sound.WITHER_DEATH, 1.0f, 1.0f);
                    }else {
                        // Handle the case where title or subtitle templates are missing
                        Bukkit.getLogger().warning("[EOTW] Title or subtitle templates missing in language.");
                    }
                }

                return;

            case "cancel":
                if (eotwManager.isNotPreEOTW()) {
                    sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.NOT_PRE_EOTW"));
                    return;
                }

                for (String s : getLanguageConfig().getStringList("EOTW_TIMER.CANCELLED_EOTW")) {
                    Bukkit.broadcastMessage(s);
                }

                getInstance().getTimerManager().getCustomTimers().remove("EOTW");
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.CANCELLED"));
                return;

            case "extend":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                if (eotwManager.isNotPreEOTW()) {
                    sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.NOT_PRE_EOTW"));
                    return;
                }

                Long extend = Formatter.parse(args[1]);

                if (extend == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                CustomTimer timer = getInstance().getTimerManager().getCustomTimer("EOTW");
                timer.setRemaining(timer.getRemaining() + extend);
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.EXTENDED")
                        .replace("%time%", timer.getRemainingString())
                );
                return;

            case "whitelist":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                switch (args[1].toLowerCase()) {
                    case "on":
                        eotwManager.setActive(true);
                        sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.WHITELIST_ON"));
                        break;

                    case "off":
                        eotwManager.setActive(false);
                        sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.WHITELIST_OFF"));
                        break;

                    case "add":
                        if (args.length < 3) {
                            sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.SPECIFY_PLAYER"));
                            return;
                        }

                        String playerName = args[2];
                        User target = getInstance().getUserManager().getByName(playerName);

                        if (target == null) {
                            sendMessage(sender, Config.PLAYER_NOT_FOUND
                                    .replace("%player%", playerName));
                            return;
                        }

                        if (eotwManager.getWhitelisted().add(target.getUniqueID())) {
                            sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.WHITELISTED")
                                    .replace("%player%", target.getName()));
                        } else {
                            sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.ALREADY_WHITELISTED")
                                    .replace("%player%", target.getName()));
                        }
                        break;

                    default:
                        sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.INVALID_WHITELIST_OPTION")
                                .replace("%option%", args[1]));
                        break;
                }
                return;
        }

        sendUsage(sender);
    }
}