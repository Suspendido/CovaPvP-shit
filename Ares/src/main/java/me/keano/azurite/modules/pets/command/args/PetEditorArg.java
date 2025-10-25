package me.keano.azurite.modules.pets.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.pets.menu.PetsEditorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetEditorArg extends Argument {

    public PetEditorArg(CommandManager manager) {
        super(manager, Arrays.asList("editor", "manage"));
        this.setPermissible("azurite.petadmin");
    }

    @Override
    public String usage() {
        return "&cUsage: /pet editor";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, me.keano.azurite.modules.framework.Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        if (getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            sendMessage(player, getLanguageConfig().getString("COMBAT_TIMER.BLOCKED_COMMAND"));
            return;
        }

        new PetsEditorMenu(getInstance().getMenuManager(), player).open();
    }
}
