package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TeamAutoFocusArg extends Argument implements Listener {

    private final Set<Player> autoFocusEnabled;
    private final TeamManager teamManager;

    public TeamAutoFocusArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "autofocus"
                )
        );
        this.autoFocusEnabled = new HashSet<>();
        this.teamManager = getInstance().getTeamManager();
        Bukkit.getPluginManager().registerEvents(this, getInstance());
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_AUTOFOCUS.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        if (autoFocusEnabled.contains(player)) {
            autoFocusEnabled.remove(player);
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_AUTOFOCUS.DISABLED"));
        } else {
            autoFocusEnabled.add(player);
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_AUTOFOCUS.ENABLED"));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        if (!autoFocusEnabled.contains(damager)) {
            return;
        }

        PlayerTeam damagerTeam = teamManager.getByPlayer(damager.getUniqueId());
        PlayerTeam targetTeam = teamManager.getByPlayer(target.getUniqueId());

        if (damagerTeam == null || targetTeam == null || damagerTeam == targetTeam || damagerTeam.getAllies().contains(targetTeam.getUniqueID())) {
            return;
        }

        if (damagerTeam.getFocus() != null && damagerTeam.getFocus().equals(targetTeam.getUniqueID())) {
            return; // Already focused
        }

        damagerTeam.setFocus(targetTeam.getUniqueID());
        damagerTeam.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_FOCUS.AUTOFOCUS_APPLIED")
                .replace("%team%", targetTeam.getName())
                .replace("%player%", damager.getName()));

        // Update waypoints (Azurite - Lunar Integration)
        for (Player member : damagerTeam.getOnlinePlayers(true)) {
            getInstance().getWaypointManager().getFocusWaypoint().send(member, targetTeam.getHq(), s -> s
                    .replace("%team%", targetTeam.getName()));
        }
    }
}
