package me.keano.azurite.modules.customitems.type;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.customitems.CustomItem;
import me.keano.azurite.modules.customitems.CustomItemManager;
import me.keano.azurite.utils.CC;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 28/01/2025
 * Project: ZeusHCF
 */

public class SplashOfRegret extends CustomItem {

    private final HCF hcf;

    public SplashOfRegret(CustomItemManager manager, HCF hcf) {
        super("splash_of_regret", Material.BRICK, manager);

        setDisplayName(CC.t("&9&lSplash of Regret"));
        List<String> lore = new ArrayList<>();
        lore.add(CC.t("&7Click to throw double debuff"));
        lore.add("");
        lore.add(CC.t("&eIt doesn't consume when used"));
        setLore(lore);

        addNBTTag("custom_item", "splash_of_regret");
        manager.registerItem(this);
        this.hcf = hcf;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (!matches(item)) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        ItemStack poisonPotion = new ItemStack(Material.POTION);
        PotionMeta poisonMeta = (PotionMeta) poisonPotion.getItemMeta();
        poisonMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.POISON, 800, 1),
                true
        );
        poisonPotion.setItemMeta(poisonMeta);

        ThrownPotion thrownPoison = player.launchProjectile(ThrownPotion.class);
        thrownPoison.setItem(poisonPotion);

        ItemStack slownessPotion = new ItemStack(Material.POTION);
        PotionMeta slownessMeta = (PotionMeta) slownessPotion.getItemMeta();
        slownessMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.SLOW, 800, 1),
                true
        );
        slownessPotion.setItemMeta(slownessMeta);

        ThrownPotion thrownSlowness = player.launchProjectile(ThrownPotion.class);
        thrownSlowness.setItem(slownessPotion);

        player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 1.0F, 1.0F);
        player.sendMessage(CC.t("&eYou throw debuffs!"));
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