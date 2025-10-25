package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class StaffBuildCommand extends Command {

    public StaffBuildCommand(CommandManager manager) {
        super(manager, "staffbuild");
        this.setPermissible("azurite.staffbuild");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
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
        StaffManager staffManager = getInstance().getStaffManager();

        // Verificar si es staff normal o headstaff
        boolean isStaff = staffManager.isStaffEnabled(player);
        boolean isHeadStaff = staffManager.isHeadStaffEnabled(player);

        if (!isStaff && !isHeadStaff) {
            sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.NOT_IN_STAFF"));
            return;
        }

        // Si ya está en modo de construcción, desactivarlo
        boolean hasStaffBuild = staffManager.isStaffBuild(player);
        boolean hasHeadStaffBuild = staffManager.isHeadStaffBuild(player);

        if (hasStaffBuild || hasHeadStaffBuild) {
            if (hasStaffBuild) {
                staffManager.getStaffBuild().remove(player.getUniqueId());
            }
            if (hasHeadStaffBuild) {
                staffManager.getHeadstaffbuild().remove(player.getUniqueId());
            }

            sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.BUILD_DISABLED"));
            return;
        }

        // Si no tiene el modo activo, activarlo dependiendo de su rango
        if (isHeadStaff) {
            staffManager.getHeadstaffbuild().add(player.getUniqueId());
            sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.BUILD_ENABLED"));
        } else if (isStaff) {
            staffManager.getStaffBuild().add(player.getUniqueId());
            sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.BUILD_ENABLED"));
        }
    }
}
