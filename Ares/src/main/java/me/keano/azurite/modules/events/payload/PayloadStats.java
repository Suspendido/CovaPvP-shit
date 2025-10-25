package me.keano.azurite.modules.events.payload;

import lombok.Getter;

import java.util.Map;

@Getter
public class PayloadStats {
    private final Map<String, Long> controlTimes;
    private final String winner;
    private final long duration;

    public PayloadStats(Map<String, Long> controlTimes, String winner, long duration) {
        this.controlTimes = controlTimes;
        this.winner = winner;
        this.duration = duration;
    }

    public String getTopController() {
        return controlTimes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
