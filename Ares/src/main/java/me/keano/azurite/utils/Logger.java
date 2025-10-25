package me.keano.azurite.utils;

import org.bukkit.Bukkit;

public class Logger {

    public static final String LINE_CONSOLE = CC.t("");

    public static void state(String state, int managers, int teams, int users, int kits, int koths) {
        print(LINE_CONSOLE);
        print("- &cLoading Ares HCF...");
        print("- &cLoading modules: &f" + Utils.getNMSVer());
        print("- &c" + convert(state) + " &f" + managers + " &cmanagers.");
        print("- &c" + convert(state) + " &f" + teams + " &cteams.");
        print("- &c" + convert(state) + " &f" + users + " &cusers");
        print("- &c" + convert(state) + " &f" + koths + " &ckoths");
        print("- &c" + convert(state) + " &f" + kits + " &ckits");
        print(LINE_CONSOLE);
        print("- &cAuthor&f:" + Bukkit.getPluginManager().getPlugin("Ares").getDescription().getAuthors());
        print("- &cVersion&f: " + Bukkit.getPluginManager().getPlugin("Ares").getDescription().getVersion());
        print("- &cState&f: " + state);
        print(LINE_CONSOLE);
    }

    public static void print(String... message) {
        for (String s : message) {
            Bukkit.getServer().getConsoleSender().sendMessage(CC.t(s));
        }
    }

    private static String convert(String string) {
        return string.equalsIgnoreCase("enabled") ? "Loaded" : "Saved";
    }
}