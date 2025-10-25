package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class StaffCommand extends Command {

    public StaffCommand(CommandManager manager) {
        super(manager, "staff");
        this.setPermissible("azurite.staff");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("mod", "modmode", "sm", "staffmode", "h", "mm");
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

        // Si el jugador tiene permisos de Head Staff
        if (player.hasPermission("azurite.head.staff") || player.isOp()) {
            if (args.length == 1 && player.hasPermission("azurite.staff.other")) {
                toggleStaffMode(player, Bukkit.getPlayer(args[0]), true);
                return;
            }
            toggleStaffMode(player, player, true);
            return;
        }

        // Si el jugador solo tiene permisos de Staff normal
        if (args.length == 1 && player.hasPermission("azurite.staff.other")) {
            toggleStaffMode(player, Bukkit.getPlayer(args[0]), false);
            return;
        }
        toggleStaffMode(player, player, false);
    }

    private void toggleStaffMode(Player sender, Player target, boolean isHeadStaff) {
        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND.replace("%player%", target.getName()));
            return;
        }

        StaffManager manager = getInstance().getStaffManager();
        boolean isEnabled = isHeadStaff ? manager.isHeadStaffEnabled(target) : manager.isStaffEnabled(target);

        if (isEnabled) {
            if (isHeadStaff) manager.disableHeadStaff(target);
            else manager.disableStaff(target);
            sendMessage(target, getLanguageConfig().getString("STAFF_MODE.DISABLED_STAFF"));
            notifyStaffModeChange(sender, target, false);
        } else {
            if (isHeadStaff) manager.enableHeadStaff(target);
            else manager.enableStaff(target);
            sendMessage(target, getLanguageConfig().getString("STAFF_MODE.ENABLED_STAFF"));
            notifyStaffModeChange(sender, target, true);
        }
    }

    private void notifyStaffModeChange(Player sender, Player target, boolean enabled) {
        String messageKey = enabled ? "STAFF_LOGS.MODMODE_ON" : "STAFF_LOGS.MODMODE_OFF";
        String[] messages = getLanguageConfig().getStringList(messageKey).toArray(new String[0]);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("zeus.headstaff")) {
                for (String s : messages) {
                    s = s
                            .replace("%player%", target.getName())
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)));

                    online.sendMessage(s);
                }
            }
        }
    }
}