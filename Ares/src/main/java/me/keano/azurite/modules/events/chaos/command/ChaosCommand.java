package me.keano.azurite.modules.events.chaos.command;

/*
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 18/01/2025
 */

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.chaos.ChaosManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChaosCommand extends Command {

    public ChaosCommand(CommandManager manager) {
        super(manager, "chaos");
        this.setPermissible("zeus.chaos.admin");

        this.completions.add(new TabCompletion(Arrays.asList("start", "end"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("chaos");
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("CHAOS_COMMAND.USAGE");
    }

    @Override
    public void sendUsage(CommandSender sender) {
        for (String line : getLanguageConfig().getStringList("CHAOS_COMMAND.USAGE")) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        ChaosManager chaosManager = getInstance().getChaosManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (chaosManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("CHAOS_COMMAND.ALREADY_ACTIVE"));
                    return;
                }

                long duration = 0;

                if (args.length > 1) {
                    duration = Formatter.parse(args[1]);
                    if (duration <= 0) {
                        sendMessage(sender, getLanguageConfig().getString("CHAOS_COMMAND.INVALID_DURATION"));
                        return;
                    }
                }

                chaosManager.startChaos(duration);

                sendMessage(sender, getLanguageConfig().getString("CHAOS_COMMAND.START"));
                break;

            case "end":
                if (!chaosManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("CHAOS_COMMAND.NOT_ACTIVE"));
                    return;
                }

                chaosManager.endChaos();
                sendMessage(sender, getLanguageConfig().getString("CHAOS_COMMAND.STOP"));
                break;

            default:
                sendUsage(sender);
                break;
        }
    }
}
