package me.keano.azurite.modules.pets.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.pets.PetManager;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetSetArg extends Argument {

    public PetSetArg(CommandManager manager) {
        super(manager, Arrays.asList("set"));
        this.setPermissible("azurite.petadmin");
    }

    @Override
    public String usage() {
        return "&cUsage: /pet set <id> <name|texture|effects> <value>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String id = args[0];
        String field = args[1].toLowerCase();
        PetManager pm = getInstance().getPetManager();

        switch (field) {
            case "name": {
                if (args.length < 3) { sendUsage(sender); return; }
                String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                pm.setDisplayName(id, name);
                sendMessage(sender, "&aUpdated name for &f" + id + "&a to &f" + name + "&a.");
                break;
            }
            case "texture": {
                if (args.length < 3) { sendUsage(sender); return; }
                String base64 = args[2];
                pm.setTexture(id, base64);
                sendMessage(sender, "&aUpdated texture for &f" + id + "&a.");
                break;
            }
            case "effects": {
                if (args.length < 3) { sendUsage(sender); return; }
                String joined = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                // Allow comma-separated list, e.g. SPEED:2,REGENERATION:1
                String[] parts = joined.split(",");
                List<String> list = new ArrayList<>();
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) continue;
                    list.add(trimmed);
                }
                pm.setEffects(id, list);
                sendMessage(sender, "&aUpdated effects for &f" + id + "&a.");
                break;
            }
            default:
                sendUsage(sender);
        }
    }
}

