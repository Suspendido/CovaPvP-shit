package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 26/01/2025
 */

public class KothFocusArg extends Argument {

    private final Map<UUID, Boolean> focusMap = new HashMap<>();

    public KothFocusArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList("focus")
        );
        this.setPermissible("azurite.koth.focus");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_FOCUS.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length < 1) {
            sendMessage(sender, usage());
            return;
        }

        Player player = (Player) sender;
        String kothName = args[0];

        Koth koth = getInstance().getKothManager().getKoth(kothName);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_FOCUS.KOTH_NOT_FOUND")
                    .replace("%koth%", kothName));
            return;
        }

        if (!koth.isActive() || koth.getCapturing() == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_FOCUS.NOT_ACTIVE")
                    .replace("%koth%", kothName));
            return;
        }

        boolean isFocused = focusMap.getOrDefault(player.getUniqueId(), false);
        focusMap.put(player.getUniqueId(), !isFocused);

        if (isFocused) {
            Bukkit.getOnlinePlayers().forEach(p -> player.showPlayer(p));
            sendMessage(player, getLanguageConfig().getString("KOTH_COMMAND.KOTH_FOCUS.DISABLED"));
        } else {
            Player capturer = koth.getCapturing();
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.equals(capturer)) {
                    player.hidePlayer(p);
                }
            });

            List<String> messages = getLanguageConfig().getStringList("KOTH_COMMAND.KOTH_FOCUS.ENABLED");
            for (String message : messages) {
                sendMessage(sender, message
                        .replace("%koth%", kothName)
                        .replace("%capper%", capturer.getName()));
            }
        }
    }
}
