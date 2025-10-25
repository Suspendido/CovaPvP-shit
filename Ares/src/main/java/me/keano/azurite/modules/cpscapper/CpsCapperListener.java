package me.keano.azurite.modules.cpscapper;

import me.keano.azurite.modules.versions.VersionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 18/8/2025
 * Project: Zeus
 */

public class CpsCapperListener implements Listener {

    private final CpsCapperManager manager;
    private final Set<UUID> injected = ConcurrentHashMap.newKeySet();

    public CpsCapperListener(CpsCapperManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!injected.add(player.getUniqueId())) return;
        VersionManager versionManager = manager.getInstance().getVersionManager();
        versionManager.getVersion().handleNettyListener(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        injected.remove(event.getPlayer().getUniqueId());
        manager.clear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (manager.isFlagged(player)) {
            event.setDamage(event.getDamage() * manager.getReduction());
        }
    }
}
