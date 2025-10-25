package me.keano.azurite.utils.deco.utils;

public class FormatterTime {

    // parse string long type
    public static Long parse(String timeStr) {
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // format string in sec min hrs days
    public static String format(Long timeInMillis) {
        if (timeInMillis == null) return "0s";

        long seconds = timeInMillis / 1000;
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append("d ");
        }
        if (hours > 0 || days > 0) { // Include hours if there are days
            formattedTime.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) { // Include minutes if there are hours or days
            formattedTime.append(minutes).append("m ");
        }
        formattedTime.append(seconds).append("s");

        return formattedTime.toString().trim();
    }
}


