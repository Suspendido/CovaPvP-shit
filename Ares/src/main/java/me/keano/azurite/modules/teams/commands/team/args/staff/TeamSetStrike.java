package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class TeamSetStrike extends Argument {

    public TeamSetStrike(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setstrike"
                )
        );
        this.setPermissible("azurite.team.setstrike");

    }
    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_SETSTRIKE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        PlayerTeam faction = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);
        Integer amount = getInt(args[1]);

        if (faction == null) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_NOT_FOUND")
                    .replace("%team%", args[0])
            );
            return;
        }

        if (amount == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        faction.setStrikes(amount);
        faction.save();

        sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_SETSTRIKE.SETSTRIKES")
                .replace("%team%", args[0])
                .replace("%amount%", args[1])
        );
    }
}

