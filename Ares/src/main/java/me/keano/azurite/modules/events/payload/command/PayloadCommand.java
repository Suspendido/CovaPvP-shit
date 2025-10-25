package me.keano.azurite.modules.events.payload.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.payload.Payload;
import me.keano.azurite.modules.events.payload.PayloadManager;
import me.keano.azurite.modules.events.payload.PayloadStats;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.keano.azurite.utils.CC;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 10/8/2025
 * Project: Zeus
 */

public class PayloadCommand extends Command {

    public PayloadCommand(CommandManager manager) {
        super(manager, "payload");
        setPermissible("ares.command.payload");
    }

    private PayloadManager getPayloadManager() {
        return getInstance().getPayloadManager();
    }

    private Payload getPayload() {
        return getPayloadManager().getPayload();
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {

        return getLanguageConfig().getStringList("PAYLOAD_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        PayloadManager manager = getPayloadManager();
        Payload payload = manager.getPayload();

        switch (args[0].toLowerCase()) {
            case "start":
                if (manager.startPayload()) {
                    sender.sendMessage(lang("PAYLOAD.COMMAND_EVENT_STARTED"));
                } else {
                    sender.sendMessage(lang("PAYLOAD.COMMAND_EVENT_ALREADY_ACTIVE"));
                }
                break;

            case "stop":
                if (manager.endPayload()) {
                    sender.sendMessage(lang("PAYLOAD.COMMAND_EVENT_STOPPED"));
                } else {
                    sender.sendMessage(lang("PAYLOAD.COMMAND_EVENT_NOT_ACTIVE"));
                }
                break;

            case "tp":
                if (sender instanceof Player) {
                    if (manager.isActive()) {
                        payload.teleport((Player) sender);
                    } else {
                        sender.sendMessage(lang("PAYLOAD.COMMAND_EVENT_NOT_ACTIVE"));
                    }
                }
                break;

            case "capper":
                if (sender instanceof Player) {
                    if (manager.isActive()) {
                        payload.teleport((Player) sender);
                        if (payload.getControllingTeam() != null) {
                            sender.sendMessage(lang("PAYLOAD.COMMAND_CAPPER_TEAM")
                                    .replace("%team%", payload.getControllingTeam().getName()));
                        } else {
                            sender.sendMessage(lang("PAYLOAD.COMMAND_CAPPER_UNCONTESTED"));
                        }
                    } else {
                        sender.sendMessage(lang("PAYLOAD.COMMAND_EVENT_NOT_ACTIVE"));
                    }
                }
                break;

            case "route":
                if (sender instanceof Player) {
                    if (args.length < 2) {
                        sender.sendMessage(lang("PAYLOAD.COMMAND_USE_ROUTE"));
                        break;
                    }
                    Player p = (Player) sender;
                    if (args[1].equalsIgnoreCase("start")) {
                        manager.startRouteEdit(p);
                    } else if (args[1].equalsIgnoreCase("done")) {
                        if (manager.isEditing(p)) {
                            manager.finishRouteEdit(p);
                        } else {
                            p.sendMessage(lang("PAYLOAD.NOT_EDITING"));
                        }
                    }
                }
                break;

            case "scan":
                if (payload.getStart() == null && sender instanceof Player) {
                    payload.setStart(((Player) sender).getLocation());
                }
                // End can be null; scanPath() will auto-detect endpoints
                payload.scanPath();
                sender.sendMessage(lang("PAYLOAD.COMMAND_SCAN")
                        .replace("%points%", String.valueOf(payload.getPath().size())));
                manager.saveData();
                break;

            case "speed":
                if (args.length > 1) {
                    try {
                        double speed = Double.parseDouble(args[1]);
                        payload.setSpeed(speed);
                        sender.sendMessage(lang("PAYLOAD.COMMAND_SPEED_SET")
                                .replace("%speed%", String.valueOf(speed)));
                        manager.saveData();
                    } catch (NumberFormatException ignored) {
                    }
                }
                break;

            case "radius":
                if (args.length > 1) {
                    try {
                        double radius = Double.parseDouble(args[1]);
                        payload.setRadius(radius);
                        sender.sendMessage(lang("PAYLOAD.COMMAND_RADIUS_SET")
                                .replace("%radius%", String.valueOf(radius)));
                        manager.saveData();
                    } catch (NumberFormatException ignored) {
                    }
                }
                break;

            case "stopdelay":
                if (args.length > 1) {
                    try {
                        long delay = Long.parseLong(args[1]);
                        payload.setStopDelayMillis(delay * 1000L);
                        sender.sendMessage(lang("PAYLOAD.COMMAND_STOPDELAY_SET")
                                .replace("%delay%", String.valueOf(delay)));
                        manager.saveData();
                    } catch (NumberFormatException ignored) {
                    }
                }
                break;

            case "set":
                if (!(sender instanceof Player) || args.length < 2) break;
                Player player = (Player) sender;
                if (args[1].equalsIgnoreCase("start")) {
                    payload.setStart(player.getLocation());
                    sender.sendMessage(lang("PAYLOAD.COMMAND_START_SET"));
                    manager.saveData();
                } else if (args[1].equalsIgnoreCase("end")) {
                    payload.setEnd(player.getLocation());
                    sender.sendMessage(lang("PAYLOAD.COMMAND_END_SET"));
                    manager.saveData();
                }
                break;

            case "log":
                PayloadStats stats = manager.getLastStats();
                if (stats == null) {
                    sender.sendMessage(lang("PAYLOAD.COMMAND_LOG_NODATA"));
                    break;
                }
                sender.sendMessage(lang("PAYLOAD.COMMAND_LOG_HEADER"));
                if (stats.getWinner() != null) {
                    sender.sendMessage(lang("PAYLOAD.COMMAND_LOG_WINNER")
                            .replace("%name%", stats.getWinner()));
                }
                stats.getControlTimes().entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .forEach(e -> sender.sendMessage(lang("PAYLOAD.COMMAND_LOG_LINE")
                                .replace("%name%", e.getKey())
                                .replace("%time%", String.valueOf(e.getValue() / 1000))));
                String top = stats.getTopController();
                if (top != null) {
                    long topSeconds = stats.getControlTimes().getOrDefault(top, 0L) / 1000;
                    sender.sendMessage(lang("PAYLOAD.COMMAND_LOG_TOP")
                            .replace("%name%", top)
                            .replace("%time%", String.valueOf(topSeconds)));
                }
                break;

            default:
                sendUsage(sender);
        }
    }

    private String lang(String key) {
        String raw;
        try {
            raw = getLanguageConfig().getUntranslatedString(key);
        } catch (Throwable t) {
            raw = null;
        }
        if (raw == null) return CC.t("&c[LANG MISSING] " + key);
        return CC.t(raw);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "tp", "capper", "route", "scan", "speed", "radius", "stopdelay", "set", "log");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {
                return Arrays.asList("start", "end");
            }
            if (args[0].equalsIgnoreCase("route")) {
                return Arrays.asList("start", "done");
            }
        }
        return Collections.emptyList();
    }
}
