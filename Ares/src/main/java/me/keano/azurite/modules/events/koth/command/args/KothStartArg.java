package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothStartArg extends Argument {

    public KothStartArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "start"
                )
        );
        this.setPermissible("azurite.koth.start");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_START.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (getInstance().getKothManager().getActiveKoths().size() >= getConfig().getInt("KOTHS_CONFIG.MAX_KOTHS_ACTIVE")) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_START.MAX_KOTHS_REACHED"));
            return;
        }

        Koth koth = getInstance().getKothManager().getKoth(args[0]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        if (koth.isActive()) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_START.ALREADY_ACTIVE"));
            return;
        }

        if (getInstance().getSotwManager().isActive() ||
            getInstance().getConquestManager().isActive() ||
            getInstance().getEotwManager().isActive()) {
            Bukkit.getConsoleSender().sendMessage("Canceled KOTH due SOTW is currently running");

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("zeus.headstaff")) {

                    String m = getLanguageConfig().getString("KOTH_COMMAND.KOTH_SOTW");
                    staff.sendMessage(m.replace("%koth%", koth.getName()));

                }
            }
            return;
        }

// Broadcast messages when event starts
            for (String messageTemplate : getLanguageConfig().getStringList("KOTH_EVENTS.BROADCAST_START")) {
                String message = messageTemplate
                        .replace("%koth%", koth.getName())
                        .replace("%color%", koth.getColor())
                        .replace("%time%", Formatter.getRemaining(koth.getMinutes(), false));
                Bukkit.broadcastMessage(message);
            }

// Send title when Event starts
            //Sound eventSound = Sound.valueOf(getLanguageConfig().getString("KOTH_EVENTS.SOUND_START", "PORTAL_TRAVEL"));

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location location = player.getLocation();

                String titleTemplate = getLanguageConfig().getString("KOTH_EVENTS.BROADCAST_START_TITLE");
                String subtitleTemplate = getLanguageConfig().getString("KOTH_EVENTS.BROADCAST_START_SUBTITLE");

                if (titleTemplate != null && subtitleTemplate != null) {
                    String title = titleTemplate
                            .replace("%koth%", koth.getName())
                            .replace("%color%", koth.getColor());
                    String subtitle = subtitleTemplate
                            .replace("%koth%", koth.getName())
                            .replace("%color%", koth.getColor());

                    player.sendTitle(title, subtitle);
                    player.playSound(location, Sound.NOTE_PLING, 1.0f, 1.0f);
                } else {
                    // Handle the case where title or subtitle templates are missing
                    Bukkit.getLogger().warning("Title or subtitle templates are missing in the language config.");
                }
            }


            koth.start();
            koth.save(); // save the active:true.

            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_START.STARTED")
                    .replace("%koth%", koth.getName())
            );
        }

        @Override
        public List<String> tabComplete (CommandSender sender, String[]args) throws IllegalArgumentException {
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