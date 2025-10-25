package me.keano.azurite.modules.events.purge.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.purge.PurgeManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.timers.type.CustomTimer;
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
public class PurgeCommand extends Command {

    public PurgeCommand(CommandManager manager) {
        super(
                manager,
                "purge"
        );
        this.setPermissible("azurite.purge");
        this.completions.add(new TabCompletion(Arrays.asList("start", "end", "create", "cancel", "extend"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("PURGE_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        PurgeManager purgeManager = getInstance().getPurgeManager();

        switch (args[0].toLowerCase()) {
            case "create":
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

                for (String s : getLanguageConfig().getStringList("PURGE_TIMER.STARTED_PURGE")) {
                    Bukkit.broadcastMessage(s);
                }

                //Send title to all online players when event starts

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location location = player.getLocation();

                    String titleTemplate = getLanguageConfig().getString("PURGE_TIMER.STARTED_TITLE");
                    String subtitleTemplate = getLanguageConfig().getString("PURGE_TIMER.STARTED_SUBTITLE");
                    //Sound eventSound = Sound.valueOf(getLanguageConfig().getString("PURGE_TIMER.STARTED_SOUND", "NOTE_PLING"));

                    if (titleTemplate != null && subtitleTemplate != null) {
                        String formattedTime = FormatterTime.format(time);
                        String title = titleTemplate.replace("%time%", formattedTime);
                        String subtitle = subtitleTemplate.replace("%time%", formattedTime);

                        player.sendTitle(title, subtitle);
                        player.playSound(location, Sound.WITHER_SPAWN, 1.0f, 1.0f);
                    }else {
                        // Manejo del caso en que faltan título o subtítulo
                        Bukkit.getLogger().warning("[PURGE] Title or subtitle templates missing in language.");
                    }
                }




            purgeManager.start(time);
                sendMessage(sender, getLanguageConfig().getString("PURGE_COMMAND.STARTED")
                        .replace("%time%", args[1])
                );
                return;

            case "end":
            case "cancel":
                if (!purgeManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("PURGE_COMMAND.NOT_ACTIVE"));
                    return;
                }

                for (String s : getLanguageConfig().getStringList("PURGE_TIMER.CANCELLED_PURGE")) {
                    Bukkit.broadcastMessage(s);
                }

                getInstance().getTimerManager().getCustomTimers().remove("Purge");
                sendMessage(sender, getLanguageConfig().getString("PURGE_COMMAND.CANCELLED"));
                return;

            case "extend":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                if (!purgeManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("PURGE_COMMAND.NOT_ACTIVE"));
                    return;
                }

                Long extend = Formatter.parse(args[1]);

                if (extend == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                CustomTimer purge = getInstance().getTimerManager().getCustomTimer("Purge");
                purge.setRemaining(purge.getRemaining() + extend);
                sendMessage(sender, getLanguageConfig().getString("PURGE_COMMAND.EXTENDED")
                        .replace("%time%", purge.getRemainingString())
                );
                return;
        }

        sendUsage(sender);
    }
}