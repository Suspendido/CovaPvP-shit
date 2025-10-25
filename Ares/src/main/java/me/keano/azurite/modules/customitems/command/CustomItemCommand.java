package me.keano.azurite.modules.customitems.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.customitems.CustomItem;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 21/01/2025
 */

public class CustomItemCommand extends Command {

    public CustomItemCommand(CommandManager manager) {
        super(
                manager,
                "customitem"
        );
        this.setPermissible("zeus.customitem");
        this.completions.add(new TabCompletion(Arrays.asList("give", "list", "getall"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("CUSTOMITEM_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 4) {
                    sendMessage(sender, getLanguageConfig().getString("CUSTOMITEM_COMMAND.CUSTOMITEM_GIVE.USAGE"));
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                CustomItem customItem = getInstance().getCustomItemManager().getCustomItems().get(args[2]);
                Integer amount = getInt(args[3]);

                if (target == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (customItem == null) {
                    sendMessage(sender, getLanguageConfig().getString("CUSTOMITEM_COMMAND.CUSTOMITEM_GIVE.NOT_FOUND")
                            .replace("%customitem%", args[2])
                    );
                    return;
                }

                if (amount == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[3])
                    );
                    return;
                }

                ItemStack itemStack = customItem.getItem().clone();
                itemStack.setAmount(amount);
                ItemUtils.giveItem(target, itemStack, target.getLocation());
                sendMessage(sender, getLanguageConfig().getString("CUSTOMITEM_COMMAND.CUSTOMITEM_GIVE.GAVE")
                        .replace("%player%", target.getName())
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%customitem%", customItem.getName())
                );
                return;

            case "list":

                for (String s : getLanguageConfig().getStringList("CUSTOMITEM_COMMAND.CUSTOMITEM_LIST.CUSTOMITEMS")) {
                    if (!s.equalsIgnoreCase("%customitems%")) {
                        sendMessage(sender, s);
                        continue;
                    }

                    Map<String, CustomItem> customItems = getInstance().getCustomItemManager().getCustomItems();
                    for (CustomItem item : customItems.values()) {
                        sendMessage(sender, getLanguageConfig().getString("CUSTOMITEM_COMMAND.CUSTOMITEM_LIST.CUSTOMITEM_FORMAT")
                                .replace("%customitem%", item.getName().replaceAll(" ", ""))
                        );
                    }
                }
                return;

            case "getall":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                Player player = (Player) sender;

                Map<String, CustomItem> customItems = getInstance().getCustomItemManager().getCustomItems();
                if (customItems.isEmpty()) {
                    sendMessage(sender, "No registered items were found");
                    return;
                }

                sendMessage(sender, "Number of registered items: " + customItems.size());

                for (CustomItem item : customItems.values()) {
                    if (item != null) {
                        ItemStack newItemStack = item.getItem();
                        if (newItemStack != null && newItemStack.getType() != null) {
                            if (newItemStack.getAmount() > 0) {
                                player.getInventory().addItem(newItemStack.clone());
                                sendMessage(sender, "Gave you: " + item.getName());
                            } else {
                                sendMessage(sender, "Item " + item.getName() + " has an invalid amount.");
                            }
                        } else {
                            sendMessage(sender, "Item " + item.getName() + " is null or has a null type.");
                        }
                    } else {
                        sendMessage(sender, "A registered item is null!.");
                    }
                }
                return;



        }

        sendUsage(sender);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            String string = args[args.length - 1];
            return getInstance().getCustomItemManager().getCustomItems().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}
