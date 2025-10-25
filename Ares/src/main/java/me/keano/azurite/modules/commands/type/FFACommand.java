package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2024
 * Date: 01/12/2024
 */

public class FFACommand extends Command {

    public FFACommand(CommandManager manager) {
        super(
                manager,
                "ffa"
        );
        this.setPermissible("zeus.command.ffaevent");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "freeforall",
                "ffaeotw",
                "ffaevent"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("FFA_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("zeus.command.ffaevent")) {
                if (args.length > 0) {
                    String subcommand = args[0].toLowerCase();
                    switch (subcommand) {
                        case "start":
                            player.performCommand("customtimer create ffastart &4&lFFA 7m");
                            player.performCommand("sotw start 10m");
                            player.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.START_ADMIN")));
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                String title = getLanguageConfig().getString("FFA_COMMAND.TITLE");
                                String subtitle = getLanguageConfig().getString("FFA_COMMAND.SUBTITLE");
                                onlinePlayer.sendTitle(title, subtitle);
                            }
                            break;
                        case "confirm":
                            player.performCommand("azurite deleteteams");
                            player.performCommand("whitelist on");
                            player.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.CONFIRM_ADMIN")));
                            break;
                        case "cancel":
                            player.performCommand("customtimer delete ffastart");
                            player.performCommand("sotw end");
                            player.performCommand("whitelist off");
                            player.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.CANCEL_ADMIN")));
                            for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                                onlinePlayers.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.CANCEL")));
                                onlinePlayers.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                                onlinePlayers.removePotionEffect(PotionEffectType.SPEED);
                                onlinePlayers.removePotionEffect(PotionEffectType.INVISIBILITY);
                            }
                            break;
                        case "effects":
                            for (Player onlinePlayers : Bukkit.getOnlinePlayers()){
                                onlinePlayers.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE , 1));
                                onlinePlayers.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
                                onlinePlayers.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
                                onlinePlayers.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.EFFECTS")));
                            }
                            player.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.EFFECTS_ADMIN")));
                            break;
                        default:
                            player.sendMessage(CC.t(getLanguageConfig().getString("FFA_COMMAND.USAGE")));
                            break;
                    }
                } else {
                    player.sendMessage(CC.t(getLanguageConfig().getStringList("FFA_COMMAND.USAGE")).toString());
                }
            } else {
                player.sendMessage(CC.t(getLanguageConfig().getString("GLOBAL_COMMANDS.INSUFFICIENT_PERMISSION")));
            }
        } else {
            System.out.println("No console");
        }

    }


}

