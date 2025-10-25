package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/11/2025
 * Project: Ares
 */

public class TeamMark extends Argument {

    public TeamMark(CommandManager manager) {
        super(  manager,
                Collections.singletonList(
                        "mark"
                ));

    }

    @Override
    public String usage() {
        return null;
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

        int range = getInstance().getWaypointManager().getLunarConfig().getInt("WAYPOINTS.MARK_POINT.RANGE", 40);
        long durationSec = getInstance().getWaypointManager().getLunarConfig().getInt("WAYPOINTS.MARK_POINT.DURATION_SECONDS", 60);

        List<Block> sight = player.getLastTwoTargetBlocks((HashSet<Byte>) null, range);
        Location markLocation;
        if (sight == null || sight.size() < 2) {
            // Fallback: mark at the limit of the radius along look direction
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection();
            if (dir == null || dir.lengthSquared() == 0) dir = player.getLocation().getDirection();
            if (dir == null || dir.lengthSquared() == 0) dir = new Vector(0, 0, 1);
            dir.normalize();
            Location end = eye.clone().add(dir.multiply(range));
            markLocation = new Location(player.getWorld(), end.getBlockX() + 0.5, end.getBlockY() + 0.5, end.getBlockZ() + 0.5);
        } else {
            Block target = sight.get(1);
            if (target == null || target.getType() == Material.AIR) {
                // Fallback when at the limit of range: use end-of-range block center
                Location eye = player.getEyeLocation();
                Vector dir = eye.getDirection();
                if (dir == null || dir.lengthSquared() == 0) dir = player.getLocation().getDirection();
                if (dir == null || dir.lengthSquared() == 0) dir = new Vector(0, 0, 1);
                dir.normalize();
                Location end = eye.clone().add(dir.multiply(range));
                markLocation = new Location(player.getWorld(), end.getBlockX() + 0.5, end.getBlockY() + 0.5, end.getBlockZ() + 0.5);
            } else {
                markLocation = target.getLocation().add(0.5, 0.5, 0.5);
            }
        }

        WaypointAzurite markWaypoint = getInstance().getWaypointManager().getMarkWaypoint();

        String customName = markWaypoint.getName().replace("%player%", player.getName());

        for (Player member : pt.getOnlinePlayers(true)) {
            WaypointAzurite temp = new WaypointAzurite(
                    getInstance().getWaypointManager(),
                    customName,
                    markWaypoint.getWaypointType(),
                    markWaypoint.getColor(),
                    markWaypoint.isEnabled()
            );

            temp.remove(member, markLocation, UnaryOperator.identity());
            temp.send(member, markLocation, UnaryOperator.identity());
        }



        String locString = markLocation.getBlockX() + ", " + markLocation.getBlockY() + ", " + markLocation.getBlockZ();
        String worldName = Utils.getWorldName(player.getWorld());
        String m = getLanguageConfig().getString("TEAM_COMMAND.TEAM_MARK.MARKED");

        pt.broadcast(m
                .replace("%player%", player.getName())
                .replace("%location%", locString)
                .replace("%duration%", String.valueOf(durationSec)));

        long ticksDelay = TimeUnit.SECONDS.toSeconds(durationSec) * 20L;
        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
            for (Player member : pt.getOnlinePlayers(true)) {
                markWaypoint.remove(member, markLocation, UnaryOperator.identity());
            }
        }, ticksDelay);
    }
}
