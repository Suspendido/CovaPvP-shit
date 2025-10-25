package me.keano.azurite.modules.pets.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.pets.PetManager;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetGiveArg extends Argument {

    public PetGiveArg(CommandManager manager) {
        super(manager, Arrays.asList("give"));
        this.setPermissible("azurite.petadmin");
    }

    @Override
    public String usage() {
        return "&cUsage: /pet give <player> <id>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "&cPlayer not found.");
            return;
        }

        String id = args[1];
        PetManager pm = getInstance().getPetManager();
        ItemStack item = pm.getPetItem(id);
        if (item == null) {
            sendMessage(sender, "&cPet not found.");
            return;
        }

        ItemUtils.giveItem(target, item, target.getLocation());
        sendMessage(sender, "&aGave &f" + target.getName() + " &aone &f" + id + "&a pet.");
    }
}

