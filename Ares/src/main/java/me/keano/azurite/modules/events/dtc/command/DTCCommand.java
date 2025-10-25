package me.keano.azurite.modules.events.dtc.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.dtc.DTCManager;
import me.keano.azurite.modules.events.dtc.listener.DTCListener;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 27/01/2025
 * Project: ZeusHCF
 */

public class DTCCommand extends Command {

    public DTCCommand(CommandManager manager) {
        super(manager, "dtc");
        this.setPermissible("zeus.command.dtc");
        this.completions.add(new TabCompletion(Arrays.asList("start", "end", "coords", "cancel", "extend", "sethealth"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("DTC_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        DTCManager dtcManager = getInstance().getDtcManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long time = Formatter.parse(args[1]);

                if (time == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%number%", args[1]));
                    return;
                }

                dtcManager.start(time);
                sendMessage(sender, getLanguageConfig().getString("DTC_COMMAND.STARTED").replace("%time%", args[1]));
                Bukkit.getServer().getPluginManager().registerEvents(new DTCListener(dtcManager), getInstance());
                return;


            case "sethealth":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                try {
                    int health = Integer.parseInt(args[1]);
                    if (health < 0) {
                        sendMessage(sender, "&cHealth must be a positive number.");
                        return;
                    }

                    dtcManager.setBlockHealth(health);
                    sendMessage(sender, getLanguageConfig().getString("DTC_COMMAND.HEALTH_SET").replace("%health%", String.valueOf(health)));
                } catch (NumberFormatException e) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%number%", args[1]));
                }
                return;

            case "end":
            case "cancel":
                if (!dtcManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("DTC_COMMAND.NOT_ACTIVE"));
                    return;
                }

                getInstance().getTimerManager().getCustomTimers().remove("DTC");
                sendMessage(sender, getLanguageConfig().getString("DTC_COMMAND.CANCELLED"));
                return;

            case "extend":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                if (!dtcManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("DTC_COMMAND.NOT_ACTIVE"));
                    return;
                }

                Long extend = Formatter.parse(args[1]);

                if (extend == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%number%", args[1]));
                    return;
                }

                CustomTimer dtc = getInstance().getTimerManager().getCustomTimer("DTC");
                dtc.setRemaining(dtc.getRemaining() + extend);
                sendMessage(sender, getLanguageConfig().getString("DTC_COMMAND.EXTENDED").replace("%time%", dtc.getRemainingString()));
                return;

            case "coords":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                Player player = (Player) sender;
                Block block = player.getTargetBlock((HashSet<Byte>) null, 5);

                if (block == null || block.getType() == Material.AIR) {
                    sendMessage(sender, "&cYou must look at a block!");
                    return;
                }

                dtcManager.setDtcCoords(block.getLocation());
                sendMessage(sender, "&eSuccessfully saved DTC Block coordinates");
                return;
        }

        sendUsage(sender);
    }
}
