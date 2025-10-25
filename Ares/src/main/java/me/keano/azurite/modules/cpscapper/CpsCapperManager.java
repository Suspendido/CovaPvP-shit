package me.keano.azurite.modules.cpscapper;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Simple CPS limiter that tracks attack packets and reduces damage
 * when players click faster than allowed.
 */
public class CpsCapperManager extends Manager {

    private final Map<UUID, Deque<Long>> clicks = new HashMap<>();
    private final Map<UUID, Long> flagged = new HashMap<>();
    private final int maxCps;
    private final double reduction;

    public CpsCapperManager(HCF instance) {
        super(instance);
        this.maxCps = getConfig().getInt("CPS_CAPPER.MAX_CPS");
        this.reduction = getConfig().getDouble("CPS_CAPPER.DAMAGE_REDUCTION") / 100.0D;
        registerListener(new CpsCapperListener(this));
    }

    public void handleAttack(Player player) {
        if (maxCps <= 0) return;
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Deque<Long> deque = clicks.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        deque.addLast(now);
        while (!deque.isEmpty() && now - deque.peekFirst() > 1000L) {
            deque.removeFirst();
        }
        if (deque.size() > maxCps) {
            flagged.put(uuid, now + 1000L);
        }
    }

    public boolean isFlagged(Player player) {
        Long time = flagged.get(player.getUniqueId());
        return time != null && time > System.currentTimeMillis();
    }

    public double getReduction() {
        return reduction;
    }

    public void clear(Player player) {
        UUID uuid = player.getUniqueId();
        clicks.remove(uuid);
        flagged.remove(uuid);
    }
}
