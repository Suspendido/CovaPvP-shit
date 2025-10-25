package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2024
 * Date: 27/11/2024
 */

public class MapCommand extends Command {

    public MapCommand(CommandManager manager) {
        super(manager, "map");
        this.setPermissible("azurite.map");
        this.completions.add(new TabCompletion(Arrays.asList("members", "allies"), 0));

    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("MAP_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String type = args[0].toLowerCase();
        int value;

        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(CC.t("&cInvalid value."));
            return;
        }

            switch (type) {
                case "members":
                    Config.HOLOGRAM_MEMBERS = value;
                    getTeamConfig().set("TEAMS.TEAM_SIZE", value);
                    sendMessage(player, getLanguageConfig().getString("MAP_COMMAND.SET_MEMBERS")
                            .replace("%value%", String.valueOf(value))
                    );

                    for (Player staff : Bukkit.getOnlinePlayers()){
                        if(staff.hasPermission("zeus.headstaff")){
                            String m = getLanguageConfig().getString("STAFF_LOGS.CHANGED_MEMBERS");

                            staff.sendMessage(m.replace("%staff%", sender.getName()).replace("%value%", String.valueOf(value)));

                        }
                    }
                    break;

                case "allies":
                    Config.HOLOGRAM_ALLIES = value;
                    getTeamConfig().set("TEAMS.ALLIES", value);
                    sendMessage(player, getLanguageConfig().getString("MAP_COMMAND.SET_ALLIES")
                            .replace("%value%", String.valueOf(value))
                    );
                    for (Player staff : Bukkit.getOnlinePlayers()){
                        if(staff.hasPermission("zeus.headstaff")){
                            String m = getLanguageConfig().getString("STAFF_LOGS.CHANGED_ALLIES");

                            staff.sendMessage(m.replace("%staff%", sender.getName()).replace("%value%", String.valueOf(value)));

                        }
                    }

                    break;
            default:
                sendUsage(sender);
                return;
        }

        getTeamConfig().save();
        getInstance().saveConfig();
        updatePlaceholders();

        try {
            getTeamConfig().save(getInstance().getDataFolder() + "/teams.yml");
        } catch (Exception e) {
            sendMessage(player, getLanguageConfig().getString(""));
            e.printStackTrace();
        }
    }

private void updatePlaceholders() {
    Config.HOLOGRAM_MEMBERS = getTeamConfig().getInt("TEAMS.TEAM_SIZE");
    Config.HOLOGRAM_ALLIES = getTeamConfig().getInt("TEAMS.ALLIES");
}
}