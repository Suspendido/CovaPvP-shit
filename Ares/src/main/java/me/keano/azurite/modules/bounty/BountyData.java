package me.keano.azurite.modules.bounty;

import lombok.Getter;

import java.util.*;

@Getter
public class BountyData {

    private UUID target;
    private String targetName;
    private Map<UUID, Integer> appliers;
    private Set<UUID> players;

    public BountyData(UUID target, String targetName, UUID applier, Integer amount) {
        this.target = target;
        this.targetName = targetName;
        this.appliers = new HashMap<>();
        this.appliers.put(applier, amount);
        this.players = new HashSet<>();
        this.players.add(applier);
    }

    public BountyData(UUID target, String targetName, Map<UUID, Integer> appliers, Set<UUID> players) {
        this.target = target;
        this.targetName = targetName;
        this.appliers = appliers;
        this.players = players;
    }

    public boolean containsApplier(UUID applier) {
        return this.appliers.containsKey(applier);
    }

    public int getAmount() {
        return this.appliers.values().stream().reduce(0, Integer::sum);
    }

    public void addApplier(UUID applier, Integer amount) {
        this.appliers.putIfAbsent(applier, 0);
        this.appliers.put(applier, this.appliers.get(applier) + amount);

        addPlayer(applier);
    }

    public int removeApplier(UUID appliers) {
        return this.appliers.remove(appliers);
    }

    public boolean hasPlayer(UUID player) {
        return this.players.contains(player);
    }

    public void addPlayer(UUID player) {
        this.players.add(player);
    }

    public void removePlayer(UUID player) {
        this.players.remove(player);
    }
}