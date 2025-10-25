package me.keano.azurite.modules.events.chaos;

/*
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 16/01/2025
 */

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
@Setter
public class ChaosManager extends Manager {

    private boolean active;
    private Long remaining;

    public ChaosManager(HCF instance) {
        super(instance);
        this.active = false;
        this.remaining = 0L;
    }

    public void startChaos(long time) {
        this.active = true;
        this.remaining = System.currentTimeMillis() + time;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for(String message : getLanguageConfig().getStringList("CHAOS_COMMAND.STARTED")) {
                player.sendMessage(message);
            }
        }
    }

    public void endChaos() {
        this.active = false;
        this.remaining = 0L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : getLanguageConfig().getStringList("CHAOS_COMMAND.ENDED")) {
                player.sendMessage(message);
            }
        }
    }

    public String getRemainingString() {
        long rem = remaining - System.currentTimeMillis();

        if (rem <= 0 && active) {
            this.active = false;
            Tasks.execute(this, this::endChaos);
            return "00:00";
        }

        return getRemaining(rem);
    }

    private static String getRemaining(long millis) {
        if (millis < 0) {
            return "00:00";
        }

        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long days = millis / (1000 * 60 * 60 * 24);

        if (days > 0) {
            return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

}
