package me.keano.azurite.modules.hooks.placeholder.type;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.keano.azurite.modules.events.dtc.DTCManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.placeholder.Placeholder;
import me.keano.azurite.modules.hooks.placeholder.PlaceholderHook;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PlaceholderAPIHook extends Module<PlaceholderHook> implements Placeholder {

    private int membersValue;
    private int alliesValue;


    public PlaceholderAPIHook(PlaceholderHook manager) {
        super(manager);
        this.load();
        Bukkit.getPluginManager().registerEvents(this, getInstance()); // Registra el listener
    }

    private void load() {
        new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return "azurite";
            }

            @Override
            public @NotNull String getAuthor() {
                return "keano";
            }

            @Override
            public @NotNull String getVersion() {
                return "1.0";
            }

            @Override
            public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
                if (player == null) {
                    return null;
                }

                if (params.equalsIgnoreCase("dtc_health")) {
                    DTCManager dtcManager = getInstance().getDtcManager();
                    if (dtcManager != null && dtcManager.isActive()) {
                        return String.valueOf(dtcManager.getBlockHealth());
                    } else {

                        return getLanguageConfig().getString("DTC_COMMAND.HOLOGRAM");
                    }
                }

                if (params.startsWith("team_top") && !params.startsWith("team_topraidable")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("team_top")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<PlayerTeam> teams = getInstance().getTeamManager().getTeamSorting().getTeamTop();

                    if (copy <= teams.size()) {
                        PlayerTeam pt = teams.get(copy - 1);
                        return Config.HOLOGRAM_FTOP
                                .replace("%team%", pt.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%points%", String.valueOf(pt.getPoints()));
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }

                if (params.startsWith("team_topraidable")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("team_topraidable")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<PlayerTeam> teams = getInstance().getTeamManager().getTeamSorting().getTeamTopRaidable();

                    if (copy <= teams.size()) {
                        PlayerTeam pt = teams.get(copy - 1);
                        return Config.HOLOGRAM_FTOP_RAIDABLE
                                .replace("%team%", pt.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%raidablepoints%", String.valueOf(pt.getRaidablePoints()));
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }

                if (params.startsWith("kills_top")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("kills_top")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<User> users = getInstance().getUserManager().getTopKills();

                    if (copy <= users.size()) {
                        User user = users.get(copy - 1);
                        return Config.HOLOGRAM_KILLS
                                .replace("%player%", user.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%kills%", String.valueOf(user.getKills()));
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }

                if (params.startsWith("deaths_top")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("deaths_top")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<User> users = getInstance().getUserManager().getTopDeaths();

                    if (copy <= users.size()) {
                        User user = users.get(copy - 1);
                        return Config.HOLOGRAM_DEATHS
                                .replace("%player%", user.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%deaths%", String.valueOf(user.getDeaths()));
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }

                if (params.startsWith("kdr_top")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("kdr_top")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<User> users = getInstance().getUserManager().getTopKDR();

                    if (copy <= users.size()) {
                        User user = users.get(copy - 1);
                        return Config.HOLOGRAM_KDR
                                .replace("%player%", user.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%kdr%", user.getKDRString());
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }

                if (params.startsWith("killstreak_top")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("killstreak_top")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<User> users = getInstance().getUserManager().getTopKillStreaks();

                    if (copy <= users.size()) {
                        User user = users.get(copy - 1);
                        return Config.HOLOGRAM_KILLSTREAK
                                .replace("%player%", user.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%killstreak%", String.valueOf(user.getKillstreak()));
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }
                if (params.startsWith("balance_top")) {
                    int copy;

                    try {

                        copy = Integer.parseInt(params.split("balance_top")[1]);

                    } catch (Exception e) {
                        return "Error: " + e;
                    }

                    List<User> users = getInstance().getUserManager().getTopbalance();

                    if (copy <= users.size()) {
                        User user = users.get(copy - 1);
                        return Config.HOLOGRAM_BALANCE
                                .replace("%player%", user.getName())
                                .replace("%number%", String.valueOf(copy))
                                .replace("%balance%", String.valueOf(user.getBalance()));
                    }

                    return Config.HOLOGRAM_EMPTY.replace("%number%", String.valueOf(copy));
                }

                if (params.equalsIgnoreCase("MEMBERS")) {
                    return String.valueOf(getMembersValue());
                }

                if (params.equalsIgnoreCase("ALLIES")) {
                    return String.valueOf(getAlliesValue());
                }



                return null;
            }
        }.register();
    }

    private int getMembersValue() {
        return Config.HOLOGRAM_MEMBERS;
    }

    private int getAlliesValue() {
        return Config.HOLOGRAM_ALLIES;
    }

    @EventHandler
    public void onPluginReload(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("Azurite")) {
            membersValue = getMembersValue();
            alliesValue = getAlliesValue();
        }
    }

    @Override
    public String replace(Player player, String string) {
        if (player == null || string == null) return "";
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Override
    public List<String> replace(Player player, List<String> list) {
        if (player == null || list == null) return Collections.emptyList();
        return PlaceholderAPI.setPlaceholders(player, list);
    }

}