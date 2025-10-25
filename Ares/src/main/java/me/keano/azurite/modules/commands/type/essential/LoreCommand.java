package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class LoreCommand extends Command {

    public LoreCommand(CommandManager manager) {
        super(manager, "lore");
        this.setPermissible("zeus.lore");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return Collections.singletonList("Usage: /lore <list|add|edit|remove> <line> <texto>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || hand.getType() == Material.AIR) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.EMPTY_HAND"));
            return;
        }

        if (args.length < 1) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_USAGE"));
            return;
        }

        ItemMeta meta = hand.getItemMeta();
        List<String> lore = (meta != null && meta.hasLore()) ? meta.getLore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "list":
                listLore(sender, lore);
                break;
            case "add":
                addLore(sender, lore, args);
                break;
            case "edit":
                editLore(sender, lore, args);
                break;
            case "remove":
                removeLore(sender, lore, args);
                break;
            default:
                sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_USAGE"));
                break;
        }

        if (meta != null) {
            meta.setLore(lore);
            hand.setItemMeta(meta);
        }
    }

    private void listLore(CommandSender sender, List<String> lore) {
        if (lore.isEmpty()) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.NO_LORE"));
        } else {
            for (int i = 0; i < lore.size(); i++) {
                String coloredLine = ChatColor.translateAlternateColorCodes('&', lore.get(i));
                sender.sendMessage((i + 1) + ". " + coloredLine);
            }
        }
    }

    private void addLore(CommandSender sender, List<String> lore, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_USAGE_ADD"));
            return;
        }

        String line = String.join(" ", args).substring(args[0].length()).trim();
        line = ChatColor.translateAlternateColorCodes('&', line);
        lore.add(line);
        sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.LORE_ADDED").replace("%text%", line));
    }

    private void editLore(CommandSender sender, List<String> lore, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_USAGE_EDIT"));
            return;
        }

        try {
            int index = Integer.parseInt(args[1]) - 1;
            if (index < 0 || index >= lore.size()) {
                sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_INDEX"));
                return;
            }

            String newLine = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
            newLine = ChatColor.translateAlternateColorCodes('&', newLine);
            lore.set(index, newLine);
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.LORE_EDITED").replace("%line%", args[1]).replace("%text%", newLine));

        } catch (NumberFormatException e) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_INDEX"));
        }
    }

    private void removeLore(CommandSender sender, List<String> lore, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_USAGE_REMOVE"));
            return;
        }

        try {
            int index = Integer.parseInt(args[1]) - 1;
            if (index < 0 || index >= lore.size()) {
                sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_INDEX"));
                return;
            }

            String removedLine = lore.remove(index);
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.LORE_REMOVED").replace("%line%", args[1]).replace("%text%", removedLine));

        } catch (NumberFormatException e) {
            sendMessage(sender, getLanguageConfig().getString("LORE_COMMAND.INVALID_INDEX"));
        }
    }
}
