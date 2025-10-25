package me.keano.azurite.modules.pvpclass.type.archer.upgrades.archers;

import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Slowness extends Button {

    private final ItemStack item;

    public Slowness() {
        this.item = new ItemBuilder(Material.LEATHER_HELMET)
                .setLeatherArmorColor(Color.WHITE)
                .setName(CC.t("&7Slowness Archer"))
                .setLore(Arrays.asList(
                        CC.t("&7► &eBy having this kit equipped you will have"),
                        CC.t("&7► &e25% of Slowness 1 for 5 seconds to your enemies"),
                        CC.t(""),
                        CC.t("&eClick to upgrade to slowness archer")
                ))
                .toItemStack();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        player.playSound(player.getLocation(), Sound.CLICK, 1F, 1F);

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (!item.getType().name().contains("LEATHER_")) {
                player.sendMessage(CC.t("&cYou need to have all 4 pieces of leather armor."));
                return;
            }
        }

        int i = 0;
        for (ItemStack armorPiece : armor) {
            armor[i] = new ItemBuilder(armorPiece).setLeatherArmorColor(Color.WHITE).toItemStack();
            i++;
        }
        player.sendMessage(CC.t("&aSuccessfully changed to Slowness archer."));
        player.updateInventory();
        player.closeInventory();
    }

    @Override
    public ItemStack getItemStack() {
        return item;
    }
}