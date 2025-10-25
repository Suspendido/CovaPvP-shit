package me.keano.azurite.modules.scheduler;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.scheduler.extra.NextSchedule;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;

import java.util.*;

@Getter
public class ScheduleManager extends Manager {

    private final Map<Long, List<Schedule>> kothSchedules;
    private final List<Schedule> allSchedules;
    private final List<Schedule> regularKothSchedules;
    private final List<Schedule> weeklyEventSchedules;
    private Calendar calendar;
    private TimeZone timeZone;
    private NextSchedule nextSchedule;
    private int oldMin;
    private boolean hasReloaded;
    private long lastDailyReset;

    public ScheduleManager(HCF instance) {
        super(instance);

        this.kothSchedules = new LinkedHashMap<>();
        this.allSchedules = new ArrayList<>();
        this.regularKothSchedules = new ArrayList<>();
        this.weeklyEventSchedules = new ArrayList<>();
        this.timeZone = TimeZone.getTimeZone(Config.SCHEDULE_TIMEZONE);
        this.calendar = Calendar.getInstance(timeZone);
        this.hasReloaded = false;
        this.lastDailyReset = 0;

        this.load();
        Tasks.executeScheduled(this, 20, this::tick);
    }

    @Override
    public void reload() {
        allSchedules.clear();
        regularKothSchedules.clear();
        weeklyEventSchedules.clear();
        kothSchedules.clear();

        this.timeZone = TimeZone.getTimeZone(Config.SCHEDULE_TIMEZONE);
        this.calendar = Calendar.getInstance(timeZone);
        this.load();
    }

    private void load() {
        allSchedules.clear();

        String timeZoneId = getSchedulesConfig().getString("SCHEDULE_CONFIG.TIME_ZONE", "America/New_York");
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        String hourStart = getSchedulesConfig().getString("TIME.HOUR_START");
        String hourEnd = getSchedulesConfig().getString("TIME.HOUR_END");
        int interval = getSchedulesConfig().getInt("TIME.INTERVAL");

        int startHour = Integer.parseInt(hourStart.split(":")[0]);
        int startMinute = Integer.parseInt(hourStart.split(":")[1]);
        int endHour = Integer.parseInt(hourEnd.split(":")[0]);
        int endMinute = Integer.parseInt(hourEnd.split(":")[1]);

        List<String> kothsList = getSchedulesConfig().getStringList("KOTHS_LIST");
        List<String> eventList = getSchedulesConfig().getStringList("EVENT_LIST");

        Calendar baseCalendar = Calendar.getInstance(timeZone);

        boolean scheduleForToday = isTimeBeforeHourEnd(baseCalendar, endHour, endMinute);

        if (!scheduleForToday) {
            baseCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        baseCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        baseCalendar.set(Calendar.MINUTE, startMinute);
        baseCalendar.set(Calendar.SECOND, 0);
        baseCalendar.set(Calendar.MILLISECOND, 0);

        long startTime = baseCalendar.getTimeInMillis();
        Bukkit.getLogger().info("[Schedule] Daily schedule starting at: " + new Date(startTime));

        int totalMinutes = (endHour - startHour) * 60 + (endMinute - startMinute);
        int kothIndex = 0;

        for (int minute = 0; minute <= totalMinutes; minute += interval) {
            if (kothIndex >= kothsList.size()) kothIndex = 0;

            String kothEntry = kothsList.get(kothIndex);
            String[] parts = kothEntry.split(", ");

            if (parts.length < 2) {
                Bukkit.getLogger().warning("Invalid KOTH entry: " + kothEntry);
                continue;
            }

            String kothName = parts[0].replace("&", "§");
            String commands = parts[1];

            long scheduleTime = startTime + (minute * 60_000L);
            Schedule kothSchedule = new Schedule(this, kothName, scheduleTime, commands);

            regularKothSchedules.add(kothSchedule);
            allSchedules.add(kothSchedule);

            kothIndex++;
        }

        for (String eventEntry : eventList) {
            String[] parts = eventEntry.split(", ");

            if (parts.length < 4) {
                Bukkit.getLogger().warning("Invalid EVENT entry: " + eventEntry);
                continue;
            }

            String eventName = parts[0].replace("&", "§");
            String dayOfWeek = parts[1].toUpperCase();
            String time = parts[2];
            String command = parts[3];

            int eventHour = Integer.parseInt(time.split(":")[0]);
            int eventMinute = Integer.parseInt(time.split(":")[1]);

            Calendar eventCalendar = Calendar.getInstance(timeZone);
            int targetDay = getDayOfWeek(dayOfWeek);

            eventCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
            eventCalendar.set(Calendar.MINUTE, eventMinute);
            eventCalendar.set(Calendar.SECOND, 0);
            eventCalendar.set(Calendar.MILLISECOND, 0);

            int currentDay = eventCalendar.get(Calendar.DAY_OF_WEEK);
            if (currentDay > targetDay) {
                eventCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            }
            eventCalendar.set(Calendar.DAY_OF_WEEK, targetDay);

            if (currentDay == targetDay && System.currentTimeMillis() > eventCalendar.getTimeInMillis()) {
                eventCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            long eventTime = eventCalendar.getTimeInMillis();
            Schedule eventSchedule = new Schedule(this, eventName, eventTime, command);

            weeklyEventSchedules.add(eventSchedule);
            allSchedules.add(eventSchedule);

            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTimeInMillis(eventTime);
            Bukkit.getLogger().info("[Schedule] Added event: " + eventName + " scheduled for " + cal.getTime());
        }
    }

    private boolean isTimeBeforeHourEnd(Calendar cal, int endHour, int endMinute) {
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = cal.get(Calendar.MINUTE);

        if (currentHour < endHour) {
            return true;
        } else if (currentHour == endHour) {
            return currentMinute < endMinute;
        }
        return false;
    }

    private int getDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek) {
            case "SUNDAY":
                return Calendar.SUNDAY;
            case "MONDAY":
                return Calendar.MONDAY;
            case "TUESDAY":
                return Calendar.TUESDAY;
            case "WEDNESDAY":
                return Calendar.WEDNESDAY;
            case "THURSDAY":
                return Calendar.THURSDAY;
            case "FRIDAY":
                return Calendar.FRIDAY;
            case "SATURDAY":
                return Calendar.SATURDAY;
            default:
                throw new IllegalArgumentException("Invalid day of the week: " + dayOfWeek);
        }
    }

    private void reloadSchedule() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "zeus reload");
        Bukkit.getLogger().info("[ScheduleManager] Reloaded schedule to ensure events start correctly.");
    }

    private void tick() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int min = calendar.get(Calendar.MINUTE);
        this.nextSchedule = checkNextSchedule();

        checkDailyReset();

        if (oldMin == min) return;

        this.oldMin = min;

        long currentTime = calendar.getTimeInMillis();
        allSchedules.stream()
                .filter(schedule -> Math.abs(schedule.getScheduledTime() - currentTime) <= 5000)
                .findFirst()
                .ifPresent(schedule -> {

                    long timeUntilEvent = schedule.getScheduledTime() - System.currentTimeMillis();
                    if (timeUntilEvent <= 5000 && timeUntilEvent > 0 && !hasReloaded) {
                        reloadSchedule();
                        hasReloaded = true;
                    }

                    int maxPlayers = getSchedulesConfig().getInt("SCHEDULE_CONFIG.KOTH_SCHEDULES_PLAYERS_REQUIRED");
                    if (maxPlayers > 0 && Bukkit.getOnlinePlayers().size() < maxPlayers) {
                        Bukkit.broadcastMessage(Config.SCHEDULE_NO_PLAYERS_START.replace("%amount%", String.valueOf(maxPlayers)));
                        return;
                    }

                    Bukkit.getLogger().info("[Schedule] Executing event: " + schedule.getName() +
                            " at " + new Date(schedule.getScheduledTime()));

                    schedule.execute();
                    hasReloaded = false;

                    if (weeklyEventSchedules.contains(schedule)) {
                        rescheduleWeeklyEvent(schedule);
                    }
                });
    }

    private void checkDailyReset() {
        String hourEnd = getSchedulesConfig().getString("TIME.HOUR_END");
        int endHour = Integer.parseInt(hourEnd.split(":")[0]);
        int endMinute = Integer.parseInt(hourEnd.split(":")[1]);

        Calendar endTimeCalendar = Calendar.getInstance(timeZone);
        endTimeCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        endTimeCalendar.set(Calendar.MINUTE, endMinute);
        endTimeCalendar.set(Calendar.SECOND, 0);
        endTimeCalendar.set(Calendar.MILLISECOND, 0);

        long endTimeMillis = endTimeCalendar.getTimeInMillis();
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis > endTimeMillis &&
                (lastDailyReset == 0 || currentTimeMillis - lastDailyReset > 12 * 60 * 60 * 1000)) {

            Bukkit.getLogger().info("[Schedule] Detected end of schedule day. Reloading for next day's schedule.");
            reload();
            lastDailyReset = currentTimeMillis;
        }
    }

    private void rescheduleWeeklyEvent(Schedule schedule) {
        Calendar eventCalendar = Calendar.getInstance(timeZone);
        eventCalendar.setTimeInMillis(schedule.getScheduledTime());

        eventCalendar.add(Calendar.WEEK_OF_YEAR, 1);

        long newEventTime = eventCalendar.getTimeInMillis();
        Schedule newSchedule = new Schedule(this, schedule.getName(), newEventTime, String.join(";", schedule.getCommands()));

        weeklyEventSchedules.add(newSchedule);
        allSchedules.add(newSchedule);
        Bukkit.getLogger().info("[Schedule] Rescheduled weekly event: " + schedule.getName() +
                " for next week at " + eventCalendar.getTime());
    }

    private NextSchedule checkNextSchedule() {
        long currentTime = System.currentTimeMillis();
        return allSchedules.stream()
                .filter(schedule -> schedule.getScheduledTime() > currentTime)
                .min(Comparator.comparingLong(Schedule::getScheduledTime))
                .map(schedule -> new NextSchedule(Collections.singletonList(schedule), schedule.getScheduledTime() - currentTime))
                .orElse(new NextSchedule(Config.SCHEDULE_NONE_NAME, Config.SCHEDULE_NONE_TIME));
    }

    public NextSchedule getNextSchedule() {
        return nextSchedule;
    }

    public Map<Long, List<Schedule>> getKothSchedules() {
        return kothSchedules;
    }
}