package me.keano.azurite.utils;

import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.Bukkit;

public class Tasks {

    public static void executeAsync(Manager manager, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(manager.getInstance(), runnable);
    }

    public static void execute(Manager manager, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(manager.getInstance(), runnable);
    }

    public static void executeLater(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskLater(manager.getInstance(), runnable, ticks);
    }

    public static void executeLaterAsync(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(manager.getInstance(), runnable, ticks);
    }

    public static void executeScheduledAsync(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(manager.getInstance(), runnable, 0L, ticks);
    }

    public static void executeScheduled(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskTimer(manager.getInstance(), runnable, 0L, ticks);
    }

    public static int repeat(AbilityManager manager, long delay, long period, Runnable runnable) {
        return Bukkit.getServer().getScheduler().runTaskTimer(manager.getInstance(), runnable, delay, period).getTaskId();
    }

    public static void cancelTask(int taskId) {
        Bukkit.getServer().getScheduler().cancelTask(taskId);
    }

    public static void executeTimer(AbilityManager manager, long l, long l1, Object o) {
    }
}