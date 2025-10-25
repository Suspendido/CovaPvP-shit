package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class HeadVanishCommand extends Command {

    public HeadVanishCommand(CommandManager manager) {
        super(
                manager,
                "headvanish"
        );
        this.setPermissible("azurite.head.vanish");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "hv",
                "hvanished"
        );
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        StaffManager manager = getInstance().getStaffManager();


        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            if (manager.isHeadVanished(target)) {
                manager.disableHeadVanish(target);
                sendMessage(target, getLanguageConfig().getString("STAFF_MODE.DISABLED_HVANISH"));
                sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.DISABLED_HVANISH_TARGET")
                        .replace("%player%", target.getName())
                );
                return;
            }

            manager.enableHeadVanish(target);
            sendMessage(target, getLanguageConfig().getString("STAFF_MODE.ENABLED_HVANISH"));
            sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.ENABLED_HVANISH_TARGET")
                    .replace("%player%", target.getName())
            );
            return;
        }

        if (manager.isHeadVanished(player)) {
            manager.disableHeadVanish(player);
            sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.DISABLED_HVANISH"));
            return;
        }

        manager.enableHeadVanish(player);
        sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.ENABLED_HVANISH"));
    }
}