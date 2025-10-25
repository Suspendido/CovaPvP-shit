package me.keano.azurite.modules.scheduler.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.scheduler.Schedule;
import me.keano.azurite.modules.scheduler.ScheduleManager;
import me.keano.azurite.modules.scheduler.extra.NextSchedule;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */

public class SchedulesCommand extends Command {

    public SchedulesCommand(CommandManager manager) {
        super(manager, "schedule");
        this.setAsync(true);
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("koths", "schedules", "events");
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("SCHEDULE_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showSchedules(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (!sender.hasPermission("zeus.schedule.add")) {
                    sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.NO_PERMISSION"));
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.ADD.USAGE"));
                    return;
                }
                addKoth(sender, args[1], args[2]);
                break;

            case "interval":
                if (!sender.hasPermission("zeus.schedule.interval")) {
                    sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.NO_PERMISSION"));
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.INTERVAL.USAGE"));
                    return;
                }
                setInterval(sender, args[1]);
                break;

            case "remove":
                if (!sender.hasPermission("zeus.schedule.remove")) {
                    sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.NO_PERMISSION"));
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.REMOVE.USAGE"));
                    return;
                }
                removeKoth(sender, args[1]);
                break;

            default:
                sendUsage(sender);
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("add");
            completions.add("interval");
            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            if (sender.hasPermission("azurite.schedule.add")) {
                return Arrays.asList("koth start <nombre>");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("interval")) {
            if (sender.hasPermission("azurite.schedule.interval")) {
                return Arrays.asList("1", "5", "10", "15", "30", "60");
            }
        }
        return new ArrayList<>();
    }

    private void showSchedules(CommandSender sender) {
        ScheduleManager scheduleManager = getInstance().getScheduleManager();
        Calendar calendar = Calendar.getInstance(scheduleManager.getTimeZone());
        NextSchedule nextSchedule = scheduleManager.getNextSchedule();

        sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.NEXT").replace("%next%", nextSchedule.getName()));
        sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.REMAINING").replace("%rem%", nextSchedule.getTimeFormatted()));

        sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.SCHEDULE"));
        for (Map.Entry<Long, List<Schedule>> entry : scheduleManager.getKothSchedules().entrySet()) {
            for (Schedule schedule : entry.getValue()) {
                sender.sendMessage("§7- §e" + schedule.getName() + " §7a las §e" + formatTime(entry.getKey()));
            }
        }
    }

    private void addKoth(CommandSender sender, String name, String displayName) {
        List<String> kothsList = getSchedulesConfig().getStringList("KOTHS_LIST");

        String kothCommand = "koth start " + name;
        String kothEntry = "&9" + displayName + ", " + kothCommand;

        kothsList.add(kothEntry);
        getSchedulesConfig().set("KOTHS_LIST", kothsList);
        getSchedulesConfig().save();

        getInstance().getScheduleManager().reload();

        sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.ADD.SUCCESS").replace("%name%", displayName));
    }

    private void setInterval(CommandSender sender, String interval) {
        try {
            int minutes = Integer.parseInt(interval);
            if (minutes <= 0) {
                sender.sendMessage(CC.t("Invalid interval"));
                return;
            }

            getSchedulesConfig().set("TIME.INTERVAL", minutes);
            getSchedulesConfig().save();

            getInstance().getScheduleManager().reload();

            sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.INTERVAL.SUCCESS").replace("%interval%", interval));
        } catch (NumberFormatException e) {
            sender.sendMessage(CC.t("Invalid interval"));
        }
    }

    private void removeKoth(CommandSender sender, String name) {
        List<String> kothsList = new ArrayList<>(getSchedulesConfig().getStringList("KOTHS_LIST"));
        String kothCommand = "koth start " + name;

        boolean removed = kothsList.removeIf(entry -> entry.toLowerCase().contains(kothCommand.toLowerCase()));

        if (removed) {
            getSchedulesConfig().set("KOTHS_LIST", kothsList);
            getSchedulesConfig().save();
            getInstance().getScheduleManager().reload();

            sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.REMOVE.SUCCESS")
                    .replace("%name%", name));
        } else {
            sender.sendMessage(getLanguageConfig().getString("SCHEDULE_COMMAND.REMOVE.NOT_FOUND")
                    .replace("%name%", name));
        }
    }



    private String formatTime(long time) {
        Calendar calendar = Calendar.getInstance(getInstance().getScheduleManager().getTimeZone());
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }
}