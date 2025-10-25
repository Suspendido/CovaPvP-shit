package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeamForcePowerOffArg extends Argument {

    public TeamForcePowerOffArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "forcepower",
                        "undopower",
                        "fp"
                )
        );
        this.setPermissible("azurite.team.forcepower");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_FORCEPOWEROFF.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sendUsage(sender);
            return;
        }

        if (!player.hasPermission("azurite.head.staff")) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        Player targetPlayer = getInstance().getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND.replace("%player%", args[0]));
            return;
        }

        PlayerTeam targetTeam = getInstance().getTeamManager().getByPlayer(targetPlayer.getUniqueId());
        if (targetTeam == null) {
            sendMessage(sender, Config.NOT_IN_TEAM.replace("%player%", targetPlayer.getName()));
            return;
        }

        if (targetTeam.isPower()) {
            targetTeam.setPowerStatus(false);
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FORCEPOWEROFF.SUCCESS")
                    .replace("%team%", targetTeam.getName()));

            sendMessage(targetPlayer, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FORCEPOWEROFF.NOTIFICATION")
                    .replace("%team%", targetTeam.getName())
                    .replace("%player%", player.getName()));
        } else {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FORCEPOWEROFF.NOT_ENABLED"));
        }
    }
}

