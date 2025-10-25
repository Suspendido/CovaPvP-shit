package me.keano.azurite.modules.events.boost.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.boost.BoostManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoostCommand extends Command {

    public BoostCommand(CommandManager manager) {
        super(manager, "boost");
        this.setPermissible("zeus.boost.admin");

        this.completions.add(new TabCompletion(Arrays.asList("start", "end", "extend"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("boost");
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("BOOST_COMMAND.USAGE");
    }

    @Override
    public void sendUsage(CommandSender sender) {
        for (String line : getLanguageConfig().getStringList("BOOST_COMMAND.USAGE")) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        BoostManager boostManager = getInstance().getBoostManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 3) {
                    sendUsage(sender);
                    return;
                }

                Long duration = Formatter.parse(args[1]);
                int multiplier;

                try {
                    multiplier = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%number%", args[2]));
                    return;
                }

                if (duration == null || duration <= 0 || multiplier <= 0) {
                    sendMessage(sender, getLanguageConfig().getString("BOOST_COMMAND.BOOST_START.INVALID"));
                    return;
                }

                if (boostManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("BOOST_COMMAND.BOOST_START.ALREADY_ACTIVE"));
                    return;
                }

                boostManager.startBoost(duration, multiplier);
                break;

            case "end":
                if (!boostManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("BOOST_COMMAND.BOOST_END.NOT_ACTIVE"));
                    return;
                }

                boostManager.endBoost();
                break;

            case "extend":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long extraTime = Formatter.parse(args[1]);

                if (extraTime == null || extraTime <= 0) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%number%", args[1]));
                    return;
                }

                if (!boostManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("BOOST_COMMAND.BOOST_EXTEND.NOT_ACTIVE"));
                    return;
                }

                boostManager.extendBoost(extraTime * 1000);
                sendMessage(sender, getLanguageConfig().getString("BOOST_COMMAND.BOOST_EXTEND.SUCCESS"));
                break;

            default:
                sendUsage(sender);
                break;
        }
    }
}
