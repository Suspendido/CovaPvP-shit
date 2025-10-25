package me.keano.azurite.modules.powerups.command;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.powerups.PowerUpsManager;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 7/16/2025
 * Project: Ares
 *
 */

public class PowerUpsCommand extends Command {

    public PowerUpsCommand(CommandManager manager) {
        super(manager, "powerups");
        this.setPermissible("powerups.admin");

        this.completions.add(new TabCompletion(
                Arrays.asList("start", "stop", "list", "tp", "spawn", "last"),
                0
        ));

        ConfigurationSection typesCfg = getInstance()
                .getConfig()
                .getConfigurationSection("POWERUPS.TYPES");
        if (typesCfg != null) {
            List<String> typeKeys = new ArrayList<>(typesCfg.getKeys(false));
            this.completions.add(new TabCompletion(typeKeys, 1));
        }
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("powerups");
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("POWERUPS_COMMAND.USAGE");
    }

    @Override
    public void sendUsage(CommandSender sender) {
        for (String line : usage()) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        HCF plugin = getInstance();
        PowerUpsManager manager = plugin.getPowerUpsManager();

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        switch (args[0].toLowerCase()) {

            case "start":

                if (manager.isRunning()) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.ALREADY_RUNNING"));
                    return;
                }
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }
                long mins;
                try {
                    mins = Long.parseLong(args[1]);
                    if (mins <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.INVALID_NUMBER"));
                    return;
                }
                manager.start(mins);
                sendMessage(sender,
                        getLanguageConfig()
                                .getString("POWERUPS_COMMAND.STARTED")
                                .replace("%mins%", String.valueOf(mins))
                );
                break;

            case "stop":

                if (!manager.isRunning()) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.NOT_RUNNING"));
                    return;
                }
                manager.stop();
                sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.STOPPED"));
                break;

            case "list":

                ConfigurationSection typesCfg = plugin
                        .getConfig()
                        .getConfigurationSection("POWERUPS.TYPES");
                if (typesCfg == null || typesCfg.getKeys(false).isEmpty()) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.NO_TYPES"));
                    return;
                }
                sendMessage(sender, "§aPower‑ups config:");
                for (String key : typesCfg.getKeys(false)) {
                    ConfigurationSection cs = typesCfg.getConfigurationSection(key);
                    String name = cs.getString("NAME");
                    String effect = cs.getString("EFFECT");
                    sendMessage(sender,
                            Formatter.format(" §7- %s: §f%s §7(§f%s§7)", key, name, effect)
                    );
                }
                break;

            case "tp":

                if (!(sender instanceof Player)) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.ONLY_PLAYER"));
                    return;
                }
                Map<Location, Integer> active = manager.getActivePowerups();
                if (active.isEmpty()) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.NO_ACTIVE"));
                    return;
                }
                Location loc = active.keySet().iterator().next();
                ((Player) sender).teleport(loc);
                sendMessage(sender,
                        getLanguageConfig()
                                .getString("POWERUPS_COMMAND.TELEPORTED")
                                .replace("%x%", String.valueOf(loc.getBlockX()))
                                .replace("%y%", String.valueOf(loc.getBlockY()))
                                .replace("%z%", String.valueOf(loc.getBlockZ()))
                );
                break;

            case "spawn":

                if (!(sender instanceof Player)) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.ONLY_PLAYER"));
                    return;
                }
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }
                int typeId;
                try {
                    typeId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.INVALID_TYPE"));
                    return;
                }
                ConfigurationSection tcfg = plugin
                        .getConfig()
                        .getConfigurationSection("POWERUPS.TYPES");
                if (tcfg == null || !tcfg.getKeys(false).contains(String.valueOf(typeId))) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.INVALID_TYPE"));
                    return;
                }
                manager.spawn(typeId, ((Player) sender).getLocation());
                sendMessage(sender,
                        getLanguageConfig()
                                .getString("POWERUPS_COMMAND.SPAWNED")
                                .replace("%type%", String.valueOf(typeId))
                );
                break;

            case "last":

                String lastPlayer = manager.getLastPicker();
                int lastType = manager.getLastType();
                if (lastPlayer == null) {
                    sendMessage(sender, getLanguageConfig().getString("POWERUPS_COMMAND.NO_LAST"));
                } else {
                    sendMessage(sender,
                            getLanguageConfig()
                                    .getString("POWERUPS_COMMAND.LAST")
                                    .replace("%player%", lastPlayer)
                                    .replace("%type%", String.valueOf(lastType))
                    );
                }
                break;

            default:
                sendUsage(sender);
                break;
        }
    }
}
