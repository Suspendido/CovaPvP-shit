package me.keano.azurite.modules.pets.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.pets.PetManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetDeleteArg extends Argument {

    public PetDeleteArg(CommandManager manager) {
        super(manager, Arrays.asList("delete", "del", "remove"));
        this.setPermissible("azurite.petadmin");
    }

    @Override
    public String usage() {
        return "&cUsage: /pet delete <id>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return;
        }

        String id = args[0];
        PetManager pm = getInstance().getPetManager();
        if (!pm.deletePet(id)) {
            sendMessage(sender, "&cPet not found.");
            return;
        }
        sendMessage(sender, "&cDeleted pet &f" + id + "&c.");
    }
}

