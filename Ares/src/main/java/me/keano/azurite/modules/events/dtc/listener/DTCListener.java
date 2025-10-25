package me.keano.azurite.modules.events.dtc.listener;

import me.keano.azurite.modules.events.dtc.DTCManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.type.DTCTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 27/01/2025
 * Project: ZeusHCF
 */

public class DTCListener extends Module<DTCManager> implements Listener {

    public DTCListener(DTCManager manager) {
        super(manager);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        DTCManager dtcManager = getInstance().getDtcManager();

        if (!dtcManager.isActive()) {
            if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof DTCTeam) {
                if (block.getType().toString().equals(dtcManager.getDTCBlock())) {
                    block.setType(Material.AIR);
                    player.sendMessage(getLanguageConfig().getString("DTC_COMMAND.NOT_ACTIVE_BLOCK"));
                }
                return;
            }
        }

        if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof DTCTeam) {
            if (block.getType().toString().equals(dtcManager.getDTCBlock())) {
                int remainingHealth = dtcManager.getBlockHealth();
                remainingHealth -= 1;

                if (remainingHealth > 0) {
                    dtcManager.setBlockHealth(remainingHealth);

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(getLanguageConfig()
                                .getString("DTC_COMMAND.BREAK")
                                .replace("%health%", String.valueOf(remainingHealth))
                        );
                    }

                    Location blockLocation = block.getLocation();

                    block.setType(Material.AIR);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            blockLocation.getBlock().setType(Material.OBSIDIAN);
                        }
                    }.runTaskLater(getInstance(), 1L);

                } else {
                    player.sendMessage(getLanguageConfig().getString("DTC_COMMAND.BROKEN"));
                    dtcManager.setBlockHealth(0);
                    block.setType(Material.AIR);

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        List<String> broadcastMessages = getLanguageConfig().getStringList("DTC_COMMAND.BROKEN_BROADCAST");
                        for (String message : broadcastMessages) {
                            p.sendMessage(message.replace("%player%", player.getName()));
                        }
                    }

                    getInstance().getTimerManager().getCustomTimers().remove("DTC");
                }
            }
        }
    }
}
