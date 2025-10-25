package me.keano.azurite.utils;

import me.keano.azurite.modules.framework.Config;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CC {

    private static final Function<String, String> REPLACER;

    static {
        if (Utils.isModernVer()) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

            REPLACER = s -> {
                for (Matcher matcher = pattern.matcher(s); matcher.find(); matcher = pattern.matcher(s)) {
                    String color = s.substring(matcher.start(), matcher.end());
                    s = s.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
                }
                return ChatColor.translateAlternateColorCodes('&', s);
            };

        } else {
            REPLACER = s -> ChatColor.translateAlternateColorCodes('&', s);
        }
    }

    public static String LINE = t("&9&m-------------------------");

    public static String t(String t) {
        return REPLACER.apply(applyPlaceholders(t));
    }

    public static List<String> t(List<String> t) {
        return t.stream().map(CC::t).collect(Collectors.toList());
    }

    private static String applyPlaceholders(String s) {
        if (s == null) return null;
        if (Config.COLOR_PRIMARY != null) s = s.replace("<p>", Config.COLOR_PRIMARY);
        if (Config.COLOR_SECONDARY != null) s = s.replace("<s>", Config.COLOR_SECONDARY);
        if (Config.COLOR_EXTRA != null) s = s.replace("<e>", Config.COLOR_EXTRA);
        if (Config.COLOR_ERROR != null) s = s.replace("<r>", Config.COLOR_ERROR);
        return s;
    }
}