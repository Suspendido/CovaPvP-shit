package me.keano.azurite.modules.deathban.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeathbanCommand extends Command {

    public DeathbanCommand(CommandManager manager) {
        super(manager, "deathban");
        this.setPermissible("azurite.deathban");
        this.completions.add(new TabCompletion(Arrays.asList("info", "remove", "set"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("DEATHBAN_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(permissible)) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                if (args.length < 2) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.DEATHBAN_INFO.USAGE"));
                    return;
                }

                OfflinePlayer targetInfo = Bukkit.getOfflinePlayer(args[1]);

                if (!targetInfo.hasPlayedBefore() && !targetInfo.isOnline()) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                Deathban deathbanInfo = getInstance().getDeathbanManager().getDeathban(targetInfo);

                if (deathbanInfo == null) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.NOT_DEATHBANNED")
                            .replace("%player%", targetInfo.getName())
                    );
                    return;
                }

                List<String> format = getLanguageConfig().getStringList("DEATHBAN_COMMAND.DEATHBAN_INFO.FORMAT");

                format.replaceAll(s1 -> s1
                        .replace("%player%", targetInfo.getName())
                        .replace("%date%", deathbanInfo.getDateFormatted())
                        .replace("%reason%", deathbanInfo.getReason())
                        .replace("%location%", Utils.formatLocation(deathbanInfo.getLocation()))
                );

                for (String form : format) {
                    sendMessage(sender, form);
                }
                return;

            case "remove":
                if (args.length < 2) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.REMOVE.USAGE"));
                    return;
                }

                OfflinePlayer targetRemove = Bukkit.getOfflinePlayer(args[1]);

                if (!targetRemove.hasPlayedBefore() && !targetRemove.isOnline()) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (!getInstance().getDeathbanManager().isDeathbanned(targetRemove)) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.NOT_DEATHBANNED")
                            .replace("%player%", targetRemove.getName())
                    );
                    return;
                }

                getInstance().getDeathbanManager().removeDeathban(targetRemove);
                sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.REMOVED_DEATHBAN")
                        .replace("%player%", targetRemove.getName())
                );
                return;
            case "set":
                if (args.length < 3) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.SET.USAGE"));
                    return;
                }

                OfflinePlayer targetSet = Bukkit.getOfflinePlayer(args[1]);

                if (!targetSet.hasPlayedBefore() && !targetSet.isOnline()) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND.replace("%player%", args[1]));
                    return;
                }

                long time;
                try {
                    time = Long.parseLong(args[2]) * 60 * 1000L;
                } catch (NumberFormatException ex) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.SET.INVALID_TIME"));
                    return;
                }

                String reason = (args.length > 3) ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "Admin Deathban";

                getInstance().getDeathbanManager().applyDeathban(targetSet, time, reason);
                sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.SET.SUCCESS")
                        .replace("%player%", targetSet.getName())
                        .replace("%time%", Utils.formatTime(time))
                );
                return;
        }

        sendUsage(sender);
    }
}
