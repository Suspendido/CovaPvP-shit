package me.keano.azurite.modules.scheduler;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 18/02/2025
 * Project: ZeusHCF
 */


@Getter
public class Schedule extends Module<ScheduleManager> {

    private final String name;
    private final List<String> commands;
    private final int minute;
    private final int hour;
    private final long dayTime;

    public Schedule(ScheduleManager manager, String name, long time, String commands) {
        super(manager);
        this.name = name;
        this.commands = Arrays.asList(commands.split(";"));
        this.dayTime = time;

        Calendar calendar = Calendar.getInstance(manager.getTimeZone());
        calendar.setTimeInMillis(time);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }

    public long getScheduledTime() {
        return dayTime;
    }


    public void execute() {
        Bukkit.getLogger().info("[Schedule] starting koth: " + name);
        for (String command : commands) {
            Bukkit.getLogger().info("[Schedule] executed command: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.trim());
        }
    }
}