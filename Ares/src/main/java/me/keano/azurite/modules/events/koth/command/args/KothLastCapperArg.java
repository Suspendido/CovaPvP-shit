package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.events.koth.Koth.CaptureData;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 26/01/2025
 */

public class KothLastCapperArg extends Argument {

    public KothLastCapperArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList("lastcapper")
        );
        this.setPermissible("zeus.koth.lastcapper");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_LASTCAPPER.USAGE");
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

        String kothName = args[0];
        Koth koth = getInstance().getKothManager().getKoth(kothName);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_LASTCAPPER.KOTH_NOT_FOUND")
                    .replace("%koth%", kothName));
            return;
        }

        CaptureData captureData = koth.getLastCapture(kothName);

        if (captureData == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_LASTCAPPER.NO_LAST_CAP")
                    .replace("%koth%", kothName));
            return;
        }

        String lastCapper = captureData.getPlayerName();
        String factionName = captureData.getFactionName();

        List<String> messages = getLanguageConfig().getStringList("KOTH_COMMAND.KOTH_LASTCAPPER.SUCCESS");
        for (String message : messages) {
            sendMessage(sender, message
                    .replace("%koth%", kothName)
                    .replace("%capper%", lastCapper)
                    .replace("%faction%", factionName != null ? factionName : "No Faction"));
        }

    }
}
