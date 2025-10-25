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

public class PetCreateArg extends Argument {

    public PetCreateArg(CommandManager manager) {
        super(manager, Arrays.asList("create", "new"));
        this.setPermissible("azurite.petadmin");
    }

    @Override
    public String usage() {
        return "&cUsage: /pet create <id>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return;
        }

        String id = args[0];
        PetManager pm = getInstance().getPetManager();
        if (!pm.createPet(id)) {
            sendMessage(sender, "&cPet with that id already exists.");
            return;
        }
        sendMessage(sender, "&aCreated pet &f" + id + "&a. Use &e/pet set " + id + " name <name>&a, &e/pet set " + id + " texture <base64>&a, &e/pet set " + id + " effects <EFFECT:LEVEL,...>&a.");
    }
}

