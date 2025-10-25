package me.keano.azurite.modules.commands.type.essential;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.glow.GlowModule;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ApolloGlowRuntime;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 13/8/2025
 * Project: Zeus
 */

public class GlowCommand extends Command {

    private final GlowModule glowModule;

    public GlowCommand(CommandManager manager) {
        super(manager, "glow");
        this.setPermissible("zeus.command.glow");
        this.glowModule = Apollo.getModuleManager().getModule(GlowModule.class);
    }

    @Override public List<String> aliases() { return Arrays.asList("outline","highlight"); }
    @Override public List<String> usage() {
        return Arrays.asList("Usage: /glow <override|reset|clear> <player>", "Usage: /glow clear");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sendMessage(sender, Config.PLAYER_ONLY); return; }
        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            try {
                ApolloGlowRuntime.resetGlow(glowModule, player.getUniqueId());
                sendMessage(player, CC.t("Resetting glow effects..."));
            } catch (Exception e) {
                sendMessage(player, CC.t("&cCannot clean glow: " + e.getClass().getSimpleName()));
            }
            return;
        }

        if (args.length != 2) { sendMessage(player, CC.t("Usage: /glow <override|reset|clear> <player>")); return; }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sendMessage(player, CC.t("Player '" + args[1] + "' not found!")); return; }

        UUID targetId = target.getUniqueId();

        try {
            switch (sub) {
                case "override":
                    ApolloGlowRuntime.overrideGlow(glowModule, targetId, Color.CYAN);
                    sendMessage(player, CC.t("Displaying glow effect...."));
                    break;
                case "reset":
                    ApolloGlowRuntime.resetGlow(glowModule, targetId);
                    sendMessage(player, CC.t("Resetting glow effect...."));
                    break;
                default:
                    sendMessage(player, CC.t("Usage: /glow <override|reset|clear> <player>"));
            }
        } catch (Exception e) {
            sendMessage(player, CC.t("&cError calling Apollo (see console): " + e.getClass().getSimpleName()));
            e.printStackTrace();
        }
    }
}
