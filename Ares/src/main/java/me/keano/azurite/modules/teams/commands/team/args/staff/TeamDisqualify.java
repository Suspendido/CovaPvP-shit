package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class TeamDisqualify extends Argument {

    public TeamDisqualify(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "disqualify",
                        "disq",
                        "dq"
                )
        );
        this.setPermissible("azurite.team.disqualify");

    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_DISQUALIFY.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);

        if (pt == null) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_NOT_FOUND")
                    .replace("%team%", args[0])
            );
            return;
        }

        int originalPoints = pt.getPoints();
        pt.setDisqualified(!pt.isDisqualified());

        if (pt.isDisqualified()) {
            pt.broadcast(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_DISQUALIFY.SUCCESS"));
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_DISQUALIFY.SETDISQUALIFY")
                    .replace("%team%", args[0]));
        } else {
            pt.setPoints(originalPoints);
            pt.broadcast(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_DISQUALIFY.UNDO"));
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_DISQUALIFY.UNDODISQUALIFY")
                    .replace("%team%", args[0]));
        }
    }
}