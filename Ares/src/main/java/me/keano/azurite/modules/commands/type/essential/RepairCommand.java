package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */

public class RepairCommand extends Command {

    private final List<Material> deniedItems;

    public RepairCommand(CommandManager manager) {
        super(manager, "repair");
        this.deniedItems = new ArrayList<>();
        this.setPermissible("azurite.repair");
        this.load();
    }

    private void load() {
        deniedItems.addAll(getConfig().getStringList("REPAIRING.DENIED_ITEMS")
                .stream()
                .map(Material::valueOf)
                .collect(Collectors.toList()));
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("fix", "fixhand");
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        ItemStack hand = getManager().getItemInHand(player);

        // handle repairing all
        if (args.length == 1 && sender.hasPermission("azurite.repair.all") && args[0].equalsIgnoreCase("ALL")) {
            repairAll(player);
            player.updateInventory();
            sendMessage(sender, getLanguageConfig().getString("REPAIR_COMMAND.REPAIRED_ALL"));
            return;
        }

        if (hand == null) {
            sendMessage(sender, getLanguageConfig().getString("REPAIR_COMMAND.EMPTY_HAND"));
            return;
        }

        if (deniedItems.contains(hand.getType())) {
            sendMessage(sender, getLanguageConfig().getString("REPAIR_COMMAND.FORBIDDEN_ITEM"));
            return;
        }

        if (isUnrepairable(hand) && !player.hasPermission("azurite.unrepairable.bypass")) {
            sendMessage(sender, getLanguageConfig().getString("REPAIR_COMMAND.UNREPAIRABLE"));
            return;
        }

        repairItem(hand);
        player.updateInventory();
        sendMessage(sender, getLanguageConfig().getString("REPAIR_COMMAND.REPAIRED"));
    }

    private void repairAll(Player player) {
        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null || deniedItems.contains(content.getType())) continue;

            if (isUnrepairable(content) && !player.hasPermission("azurite.unrepairable.bypass")) {
                sendMessage(player, getLanguageConfig().getString("REPAIR_COMMAND.UNREPAIRABLE"));
                continue;
            }

            repairItem(content);
        }

        for (ItemStack armorContent : player.getInventory().getArmorContents()) {
            if (armorContent == null || deniedItems.contains(armorContent.getType())) continue;

            if (isUnrepairable(armorContent) && !player.hasPermission("azurite.unrepairable.bypass")) {
                sendMessage(player, getLanguageConfig().getString("REPAIR_COMMAND.UNREPAIRABLE"));
                continue;
            }

            repairItem(armorContent);
        }
    }

    private boolean isUnrepairable(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                return meta.getLore().stream().anyMatch(lore -> lore.equalsIgnoreCase("§cUnrepairable"));
            }
        }
        return false;
    }

    private void repairItem(ItemStack item) {
        if (item.getType().getMaxDurability() > 0) {
            item.setDurability((short) 0);
        }
    }


    private boolean cannotRepair(String name) {
        return !name.contains("helmet") && !name.contains("chestplate") && !name.contains("leggings") &&
                !name.contains("boots") && !name.contains("sword") && !name.contains("shovel") &&
                !name.contains("pickaxe") && !name.contains("axe") && !name.contains("hoe") && !name.contains("bow");
    }
}
