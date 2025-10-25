package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamForceJoinArg extends Argument {

    public TeamForceJoinArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "forcejoin",
                        "staffjoin"
                )
        );
        this.setPermissible("azurite.team.forcejoin");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEJOIN.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam inTeam = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        PlayerTeam pt = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);

        if (inTeam != null) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEJOIN.ALREADY_IN_TEAM"));
            return;
        }

        if (pt == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        sendAlertStaff(player, pt);
        pt.joinMember(player, Role.MEMBER);
        pt.broadcast(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEJOIN.BROADCAST_FORCEJOIN")
                .replace("%player%", player.getName())
        );
    }

    private void sendAlertStaff(Player player, PlayerTeam team) {
        String[] messages = getLanguageConfig().getStringList("STAFF_LOGS.FORCEJOIN").toArray(new String[0]);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("zeus.headstaff")) {
                for (String message : messages) {
                    String formattedMessage = message
                            .replace("%player%", player.getName())
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(player)))
                            .replace("%team%", team.getName())
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(player)));

                    online.sendMessage(formattedMessage);
                }
            }
        }
    }

}