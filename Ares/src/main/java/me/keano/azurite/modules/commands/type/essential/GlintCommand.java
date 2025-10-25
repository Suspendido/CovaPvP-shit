package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.ApolloUtils;
import me.keano.azurite.utils.CC;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class GlintCommand extends Command {

    public GlintCommand(CommandManager manager) {
        super(
                manager,
                "glint"
        );
        this.setPermissible("zeus.command.glint");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "glintcolor",
                "glintcolour",
                "addglint"
        );
    }

    @Override
    public List<String> usage() {
        return Arrays.asList(
                "/glint <#hexcolor>"
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            sendMessage(sender, CC.t("&cYou must be holding an item to apply a glint."));
            return;
        }

        if (args.length != 1) {
            sendMessage(sender, CC.t("&cUsage: /glint <#hexcolor>"));
            return;
        }

        String color = args[0];
        if (!color.matches("^#[a-fA-F0-9]{6}$")) {
            sendMessage(sender, CC.t("&cPlease provide a valid hex color. Example: #FF0000"));
            return;
        }

        ItemStack updatedItem = ApolloUtils.applyGlint(itemInHand, color);
        player.getInventory().setItemInHand(updatedItem);

        sendMessage(sender, CC.t("&aSuccessfully applied " + color + " colour to your item."));
    }
}
