package me.keano.azurite.modules.teams.commands.team.args.leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamPowerArg extends Argument {

    private static final Set<UUID> confirming = new HashSet<>();

    public TeamPowerArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList("power", "pw")
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_POWER.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (!pt.checkRole(player, Role.LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.LEADER.getName())
            );
            return;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("confirm")) {
            if (!confirming.contains(player.getUniqueId())) {
                sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_POWER.NO_PENDING_CONFIRM"));
                return;
            }

            confirming.remove(player.getUniqueId());
            pt.setPowerStatus(true);

            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_POWER.SETPOWER")
                    .replace("%team%", pt.getName()));
            return;
        }

        if (pt.isPower()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_POWER.ALREADY_ENABLED"));
            return;
        }

        confirming.add(player.getUniqueId());

        List<String> confirmLines = getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_POWER.CONFIRMATION");
        for (String line : confirmLines) {
            sendMessage(sender, line);
        }
    }

}
