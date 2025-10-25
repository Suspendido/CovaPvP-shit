package me.keano.azurite.modules.commands.type.essential;

import com.minexd.zoot.util.CC;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2024
 * Date: 01/12/2024
 */
public class FillBottleCommand extends Command {

    public FillBottleCommand(CommandManager manager) {
        super(manager,
                "fillbottle"
        );
        this.setPermissible("zeus.fillbottle");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "fillb",
                "bottle",
                "fillwater"
        );
    }

    @Override
    public List<String> usage() {
        return Collections.singletonList(CC.translate("&c/fillbottle"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("No console");
            return;
        }

        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlock((Set<Material>) null, 5);

        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            player.sendMessage(getLanguageConfig().getString("FILLBOTTLE_COMMAND.BLOCK"));
            return;
        }

        ItemStack itemInHand = player.getItemInHand(); // API 1.8.9
        if (itemInHand == null || itemInHand.getType() != Material.GLASS_BOTTLE) {
            player.sendMessage(getLanguageConfig().getString("FILLBOTTLE_COMMAND.EMPTY"));
            return;
        }

        Chest chest = (Chest) targetBlock.getState();
        Inventory chestInventory = chest.getInventory();

        int bottlesAvailable = itemInHand.getAmount();
        int bottlesFilled = 0;

        for (int i = 0; i < chestInventory.getSize(); i++) {
            if (bottlesAvailable <= 0) break;
            ItemStack slot = chestInventory.getItem(i);

            if (slot == null || slot.getType() == Material.AIR) {
                chestInventory.setItem(i, new ItemStack(Material.POTION, 1, (short) 0));
                bottlesAvailable--;
                bottlesFilled++;
            }
        }

        if (bottlesFilled == 0) {
            player.sendMessage(getLanguageConfig().getString("FILLBOTTLE_COMMAND.FULL_CHEST"));
            return;
        }

        itemInHand.setAmount(itemInHand.getAmount() - bottlesFilled);

        if (itemInHand.getAmount() <= 0) {
            player.setItemInHand(null);
        } else {
            player.setItemInHand(itemInHand);
        }

        player.sendMessage(getLanguageConfig().getString("FILLBOTTLE_COMMAND.SUCCESSFUL").replace("%amount%", String.valueOf(bottlesFilled)));
    }
}
