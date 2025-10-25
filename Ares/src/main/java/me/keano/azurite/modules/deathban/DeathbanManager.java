package me.keano.azurite.modules.deathban;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.deathban.listener.DeathbanListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.timers.listeners.playertimers.PvPTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class DeathbanManager extends Manager {

    private final Map<String, Long> deathbanTimes;

    public DeathbanManager(HCF instance) {
        super(instance);
        this.deathbanTimes = new HashMap<>();
        this.load();
        new DeathbanListener(this);
    }
    private void load() {
        for (String s : getConfig().getStringList("DEATHBANS.TIMES")) {
            String[] split = s.split(", ");
            deathbanTimes.put("azurite.deathban." + split[0].toLowerCase(), Integer.parseInt(split[1]) * (60 * 1000L));
        }
    }

    private long getDeathbanTime(Player player) {
        long deathbanTime = getConfig().getInt("DEATHBANS.DEFAULT_TIME") * (60 * 1000L);

        for (Map.Entry<String, Long> entry : deathbanTimes.entrySet()) {
            String perm = entry.getKey();
            Long time = entry.getValue();

            if (player.hasPermission(perm) && time < deathbanTime) {
                deathbanTime = time;
            }
        }

        return deathbanTime;
    }

    public Deathban getDeathban(Player player) {
        return getDeathban(player.getUniqueId());
    }

    public Deathban getDeathban(OfflinePlayer offlinePlayer) {
        return getDeathban(offlinePlayer.getUniqueId());
    }

    public Deathban getDeathban(UUID uuid) {
        User user = getInstance().getUserManager().getByUUID(uuid);
        return user.getDeathban();
    }

    public void removeDeathban(Player player) {
        removeDeathban(player.getUniqueId(), player);
    }

    public void removeDeathban(OfflinePlayer offlinePlayer) {
        if (offlinePlayer == null) return;
        removeDeathban(offlinePlayer.getUniqueId(), null);
    }

    private void removeDeathban(UUID uuid, Player player) {
        User user = getInstance().getUserManager().getByUUID(uuid);
        user.setDeathban(null);
        user.save();

        if (player != null) {
            getInstance().getTimerManager().getCombatTimer().removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("DEATHBAN_LISTENER.REVIVED"));

            Tasks.execute(this, () -> {
                player.teleport(getInstance().getWaypointManager().getWorldSpawn().clone().add(0.5, 0, 0.5));
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.updateInventory();

                PvPTimer pvPTimer = getInstance().getTimerManager().getPvpTimer();
                if (pvPTimer.getSeconds() != 0) pvPTimer.applyTimer(player);

                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
            });
        }
    }

    public boolean isDeathbanned(Player player) {
        return isDeathbanned(player.getUniqueId());
    }

    public boolean isDeathbanned(OfflinePlayer offlinePlayer) {
        return isDeathbanned(offlinePlayer.getUniqueId());
    }

    private boolean isDeathbanned(UUID uuid) {
        return getDeathban(uuid) != null;
    }

    public void applyDeathban(Player player) {
        if (player.hasPermission("azurite.deathban.bypass")) return;

        EntityDamageEvent cause = player.getLastDamageCause();
        String reason = (player.getKiller() != null ? player.getKiller().getName() : (cause == null ? "Unknown" : cause.getCause().toString()));
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        user.setDeathban(new Deathban(this, player.getUniqueId(), getDeathbanTime(player), reason, player.getLocation()));
        user.save();
    }
    public void applyDeathban(OfflinePlayer target, long customTime, String reason) {
        UUID uuid = target.getUniqueId();
        User user = getInstance().getUserManager().getByUUID(uuid);

        if (user == null) return;

        user.setDeathban(new Deathban(this, uuid, customTime, reason, target.getPlayer() != null ? target.getPlayer().getLocation() : null));
        user.save();
    }
}