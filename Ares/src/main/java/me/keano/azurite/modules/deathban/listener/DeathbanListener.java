package me.keano.azurite.modules.deathban.listener;

import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.deathban.DeathbanManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.timers.listeners.playertimers.PvPTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeathbanListener extends Module<DeathbanManager> implements Listener {

    private static final Method RESPAWN_FLAGS = (Utils.isModernVer() ?
            ReflectionUtils.accessMethod(PlayerRespawnEvent.class, "getRespawnFlags") : null);

    public DeathbanListener(DeathbanManager manager) {
        super(manager);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (player.hasPermission("azurite.deathban.bypass")) return;
        if (!getManager().isDeathbanned(player) && !getInstance().isKits() && !getInstance().isSoup()) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0);

            getManager().applyDeathban(player);

            String kickMessage = ChatColor.translateAlternateColorCodes('&',
                    getLanguageConfig().getString("DEATHBAN_LISTENER.KICK_MESSAGE")
                            .replace("%time%", Utils.formatTime(getManager().getDeathban(player).getTime())));
            Tasks.executeLater(getManager(), 2L, () -> player.kickPlayer(kickMessage));
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        User user = getInstance().getUserManager().getByUUID(e.getUniqueId());
        Deathban deathban = user.getDeathban();
        Deathban deathbanInfo = getInstance().getDeathbanManager().getDeathban(e.getUniqueId());

        if (deathban == null) return;

        if (!deathban.isExpired()) {
            String denyMessage = ChatColor.translateAlternateColorCodes('&',
                    getLanguageConfig().getString("DEATHBAN_LISTENER.DENY_JOIN_MESSAGE")
                            .replace("%reason%", deathbanInfo.getReason())
                            .replace("%time%", Utils.formatTime(deathban.getTime()))
            );
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, denyMessage);
        } else {
            getManager().removeDeathban(user.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        boolean bypass = player.hasPermission("azurite.deathban.bypass");

        if (Utils.isModernVer()) {
            List<String> flags = ((Set<?>) ReflectionUtils.fetch(RESPAWN_FLAGS, e))
                    .stream().map(Object::toString).collect(Collectors.toList());

            if (flags.contains("END_PORTAL")) {
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    e.setRespawnLocation(getInstance().getWaypointManager().getEndWorldExit().clone().add(0.5, 0, 0.5));
                    player.sendMessage(getLanguageConfig().getString("END_LISTENER.ENTERED"));
                }
                return;
            }
        }

        if (bypass && !getInstance().isKits() && !getInstance().isSoup()) {
            player.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.BYPASSED_DEATHBAN"));
        }

        if (!getInstance().isKits() && !getInstance().isSoup()) {
            PvPTimer pvPTimer = getInstance().getTimerManager().getPvpTimer();
            if (pvPTimer.getSeconds() != 0) {
                Tasks.executeLater(getManager(), 20L, () -> pvPTimer.applyTimer(player));
            }
        }
        e.setRespawnLocation(getInstance().getWaypointManager().getWorldSpawn().clone().add(0.5, 0, 0.5));
    }
}
