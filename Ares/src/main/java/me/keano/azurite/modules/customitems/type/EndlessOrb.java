package me.keano.azurite.modules.customitems.type;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.customitems.CustomItem;
import me.keano.azurite.modules.customitems.CustomItemManager;
import me.keano.azurite.utils.CC;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 21/01/2025
 */

public class EndlessOrb extends CustomItem {

    private final HCF hcf;

    public EndlessOrb(CustomItemManager manager, HCF hcf) {
        super("endless_orb", Material.INK_SACK, manager);

        setDisplayName(CC.t("&3&lEndless Orb"));
        List<String> lore = new ArrayList<>();
        lore.add(CC.t("&7Click to throw an Ender Pearl"));
        lore.add("");
        lore.add(CC.t("&eIt doesn't consume when used"));
        setLore(lore);

        addNBTTag("custom_item", "endless_orb");

        manager.registerItem(this);
        this.hcf = hcf;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (!matches(item)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (hcf.getTimerManager().getEnderpearlTimer().hasTimer(player)) {
            player.sendMessage(CC.t("&cYou already have an Ender Pearl cooldown"));
            event.setCancelled(true);
        } else {
            EnderPearl enderPearl = player.launchProjectile(EnderPearl.class);
            enderPearl.setShooter(player);

            player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            event.setCancelled(true);
        }
    }

    @Override
    public void onUse() {
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = super.getItem();
        item.setDurability((short) 4);
        return item;
    }
}
