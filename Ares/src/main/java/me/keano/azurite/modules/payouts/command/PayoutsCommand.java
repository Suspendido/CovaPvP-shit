package me.keano.azurite.modules.payouts.command;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.payouts.menu.PayoutsMenu;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 26/02/2025
 * Project: Zeus
 */

public class PayoutsCommand extends Command {

    private final PayoutsMenu payoutsMenu;

    public PayoutsCommand(CommandManager manager) {
        super(manager, "payouts");
        this.payoutsMenu = new PayoutsMenu(getInstance().getListenerManager(), getInstance());
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("payouts", "rewards");
    }

    @Override
    public List<String> usage() {
        return Arrays.asList(
                "&7&m----------------------------------------",
                "&c/payouts ",
                "&7&m----------------------------------------"
        );
    }

    @Override
    public void sendUsage(CommandSender sender) {
        for (String line : usage()) {
            sender.sendMessage(CC.t(line));
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        payoutsMenu.openMenu(player);
    }
}