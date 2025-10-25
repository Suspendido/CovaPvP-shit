package me.keano.azurite.modules.events.boost;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.boost.listener.BoostListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
@Setter
public class BoostManager extends Manager {

    private boolean active;
    private int multiplier;
    private Long remaining;


    public BoostManager(HCF instance) {
        super(instance);
        this.active = false;
        this.multiplier = 1;
        this.remaining = 0L;

        new BoostListener(this, instance);
    }

    public void startBoost(long time) {
        this.active = true;
        this.remaining = System.currentTimeMillis() + time;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : getLanguageConfig().getStringList("BOOST_COMMAND.BOOST_START.STARTED")) {
                player.sendMessage(message.replace("%multiplier%", String.valueOf(this.multiplier)));
            }
        }
    }

    public void startBoost(long time, int multiplier) {
        this.multiplier = multiplier;
        startBoost(time);
    }

    public void endBoost() {
        this.active = false;
        this.remaining = 0L;
        this.multiplier = 1;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : getLanguageConfig().getStringList("BOOST_COMMAND.BOOST_END.ENDED")) {
                player.sendMessage(message);
            }
        }
    }

    public void extendBoost(long extraTime) {
        if (!active) {
            return;
        }

        this.remaining += extraTime;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String message : getLanguageConfig().getStringList("BOOST_COMMAND.BOOST_START.EXTENDED")) {
                player.sendMessage(message.replace("%time%", String.valueOf(extraTime / 1000)));
            }
        }
    }

    public void checkBoostStatus() {
        long timeLeft = remaining - System.currentTimeMillis();

        if (timeLeft <= 0 && active) {
            this.active = false;
            Tasks.execute(this, this::endBoost);
        }
    }

    public String getRemainingString() {
        long rem = remaining - System.currentTimeMillis();

        if (rem <= 0 && active) {
            this.active = false;
            Tasks.execute(this, this::endBoost);
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
