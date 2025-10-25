package me.keano.azurite.modules.pets.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.pets.Pet;
import me.keano.azurite.modules.pets.PetManager;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetListArg extends Argument {

    public PetListArg(CommandManager manager) {
        super(manager, Arrays.asList("list", "ls"));
    }

    @Override
    public String usage() {
        return "&cUsage: /pet list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        PetManager pm = getInstance().getPetManager();
        if (pm.getPets().isEmpty()) {
            sendMessage(sender, CC.t("&cNo pets created."));
            return;
        }

        String ids = pm.getPets().values().stream()
                .map(Pet::getId)
                .collect(Collectors.joining("&7, &f"));
        sendMessage(sender, CC.t("&ePets: &f" + ids));
    }
}

