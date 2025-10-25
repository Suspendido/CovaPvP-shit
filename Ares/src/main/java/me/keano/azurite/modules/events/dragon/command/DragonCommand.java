package me.keano.azurite.modules.events.dragon.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.dragon.DragonManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 10/02/2025
 * Project: Zeus
 */

public class DragonCommand extends Command {

    public DragonCommand(CommandManager manager) {
        super(manager,
                "dragon");
        this.setPermissible("zeus.command.enddragon");
        this.completions.add(new TabCompletion(Arrays.asList("start", "cancel", "sethealth", "top", "coords", "tp", "list", "clear"), 0));
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "dragon",
                "dragonevent",
                "enddragon",
                "enderdragon"
        );
    }
    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("DRAGON_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        DragonManager manager = getInstance().getDragonManager();

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

                manager.start(time);
                break;

            case "cancel":
                if (!manager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("DRAGON_COMMAND.NOT_ACTIVE"));
                    return;
                }
                manager.endEvent();
                sendMessage(sender, getLanguageConfig().getString("DRAGON_COMMAND.CANCELLED"));
                break;

            case "sethealth":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                try {
                    int health = Integer.parseInt(args[1]);
                    manager.setHealth(health);
                    sendMessage(sender, getLanguageConfig().getString("DRAGON_COMMAND.HEALTH_SET"));
                } catch (NumberFormatException e) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%health%", args[1]));
                }
                break;

            case "top":
                List<Map.Entry<UUID, Double>> top = manager.getTopDamagers();
                sendMessage(sender, "&5&lTop Damagers:");
                for (int i = 0; i < top.size(); i++) {
                    Map.Entry<UUID, Double> entry = top.get(i);
                    String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    sendMessage(sender, "&d" + (i + 1) + ". &f" + name + " &7- &c" + entry.getValue() + " damage");
                }
                break;

            case "coords":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                Player player = (Player) sender;
                manager.setDragonCoords(player.getLocation());
                sendMessage(sender, "&eDragon spawn location set!");
                break;

            case "tp":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                Player tpPlayer = (Player) sender;
                if (manager.getCurrentDragon() == null) {
                    sendMessage(tpPlayer, "&eThere is not a dragon!.");
                    return;
                }

                tpPlayer.teleport(manager.getCurrentDragon().getLocation());
                sendMessage(tpPlayer, "&eTeleported to dragon.");
                break;

            case "list":
                List<EnderDragon> dragons = manager.getAliveDragons();
                if (dragons.isEmpty()) {
                    sendMessage(sender, "&eThere are no dragons alive.");
                    return;
                }

                sendMessage(sender, "&5&lDragos alive");
                for (int i = 0; i < dragons.size(); i++) {
                    EnderDragon dragon = dragons.get(i);
                    Location loc = dragon.getLocation();
                    sendMessage(sender, "&d" + (i + 1) + ". &fWorld: &7" + loc.getWorld().getName() +
                            " &fX: &7" + loc.getBlockX() + " &fY: &7" + loc.getBlockY() + " &fZ: &7" + loc.getBlockZ());
                }
                break;

            case "clear":

                int dragonsRemoved = manager.clearDragons();
                sendMessage(sender, "&eRemoved &c" + dragonsRemoved + " &edragons.");
                break;

            default:
                sendUsage(sender);
        }
    }
}