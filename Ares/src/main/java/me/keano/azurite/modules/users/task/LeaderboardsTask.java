package me.keano.azurite.modules.users.task;

import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.UserManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LeaderboardsTask extends BukkitRunnable {

    private final UserManager manager;

    public LeaderboardsTask(UserManager manager) {
        this.manager = manager;
        this.start();
    }

    @Override
    public void run() {
        List<User> sorted = new ArrayList<>(manager.getUsers().values());

        sorted.sort(Comparator.comparingInt(User::getKills).reversed());
        manager.getTopKills().clear();
        manager.getTopKills().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingInt(User::getDeaths).reversed());
        manager.getTopDeaths().clear();
        manager.getTopDeaths().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingInt(User::getKillstreak).reversed());
        manager.getTopKillStreaks().clear();
        manager.getTopKillStreaks().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingDouble(User::getKDR).reversed());
        manager.getTopKDR().clear();
        manager.getTopKDR().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingDouble(User::getBalance).reversed());
        manager.getTopbalance().clear();
        manager.getTopbalance().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.clear();
    }

    public void start() {
        this.runTaskTimerAsynchronously(manager.getInstance(), 0L, 20 * 30); // 30s
    }
}