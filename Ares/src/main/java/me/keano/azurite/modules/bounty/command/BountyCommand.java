package me.keano.azurite.modules.bounty.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BountyCommand extends Command {

    public BountyCommand(CommandManager manager) {
        super(manager, "bounty");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "b",
                "bt"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("BOUNTY.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.t("&cOnly players can execute this command."));
            return;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            this.handleLengthOne(player, args[0]);
        } else if (args.length == 2) {
            this.handleLengthTwo(player, args[0], args[1]);
        } else if (args.length == 3) {
            this.handleLengthThree(player, args[0], args[1], args[2]);
        } else {
            this.sendUsage(player);
        }
    }

    private void handleLengthOne(Player sender, String arg1) {
        if (arg1.equalsIgnoreCase("help")) {
            this.sendUsage(sender);
        } else if (arg1.equalsIgnoreCase("list")) {
            getInstance().getBountyManager().listBounties(sender);
        } else {
            this.sendUsage(sender);
        }
    }

    private void handleLengthTwo(Player sender, String arg1, String arg2) {
        if (arg1.equalsIgnoreCase("remove")) {
            Player target = Bukkit.getPlayer(arg2);
            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", arg2)
                );
                return;
            }
            getInstance().getBountyManager().removeBounty(target, sender);
        } else {
            this.sendUsage(sender);
        }
    }

    private void handleLengthThree(Player sender, String arg1, String arg2, String arg3) {
        if (arg1.equalsIgnoreCase("add")) {
            Player target = Bukkit.getPlayer(arg2);
            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", arg1)
                );
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(arg3);
            } catch (NumberFormatException e) {
                sender.sendMessage(CC.t("&cAmount has to be a correct number."));
                return;
            }

            getInstance().getBountyManager().addBounty(target, sender, amount);
        } else {
            this.sendUsage(sender);
        }
    }
}
