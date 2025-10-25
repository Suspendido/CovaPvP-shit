package me.keano.azurite.modules.pets.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.pets.menu.PetsMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetsCommand extends Command {

    public PetsCommand(CommandManager manager) {
        super(manager, "pets");
        this.handleArguments(java.util.Arrays.asList(
                new me.keano.azurite.modules.pets.command.args.PetEditorArg(manager),
                new me.keano.azurite.modules.pets.command.args.PetListArg(manager),
                new me.keano.azurite.modules.pets.command.args.PetCreateArg(manager),
                new me.keano.azurite.modules.pets.command.args.PetDeleteArg(manager),
                new me.keano.azurite.modules.pets.command.args.PetGiveArg(manager),
                new me.keano.azurite.modules.pets.command.args.PetSetArg(manager)
        ));
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("pet");
    }

    @Override
    public List<String> usage() {
        return Collections.singletonList("&cUsage: /pets");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            super.execute(sender, args);
            return;
        }

        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        if (getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            sendMessage(player, getLanguageConfig().getString("COMBAT_TIMER.BLOCKED_COMMAND"));
            return;
        }

        new PetsMenu(getInstance().getMenuManager(), player).open();
    }
}
