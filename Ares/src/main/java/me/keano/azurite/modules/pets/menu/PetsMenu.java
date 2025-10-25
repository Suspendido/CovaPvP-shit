package me.keano.azurite.modules.pets.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.pets.Pet;
import me.keano.azurite.modules.pets.PetManager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/25/2025
 * Project: Ares
 */

public class PetsMenu extends Menu {

    private final PetManager petManager;

    public PetsMenu(MenuManager manager, Player player) {
        super(manager, player, CC.t("&9&lPets"), 9, false);
        this.petManager = getInstance().getPetManager();
        this.setAllowInteract(true);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();

        map.put(5, new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                Pet equipped = petManager.getEquipped().get(player.getUniqueId());
                if (equipped != null) {
                    // Unequip and restore barrier
                    petManager.unequip(player, true);
                    inventory.setItem(4, getBarrier());
                }
            }

            @Override
            public ItemStack getItemStack() {
                Pet equipped = petManager.getEquipped().get(player.getUniqueId());
                if (equipped != null) return equipped.toItem();
                return getBarrier();
            }
        });

        return map;
    }

    private ItemStack getBarrier() {
        Material mat;
        try {
            mat = ItemUtils.getMat("BARRIER");
        } catch (Exception e) {
            mat = ItemUtils.getMat("STAINED_GLASS_PANE");
        }

        ItemBuilder builder = new ItemBuilder(mat)
                .setName("&cPlace your Pet here")
                .setLore("&7Click a pet item in your inventory to equip");

        ItemStack item = builder.toItemStack();
        if (mat.name().equals("STAINED_GLASS_PANE")) {
            getManager().setData(item, 14);
        }

        return item;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        // Cancel other slots; center handled by its button
        if (e.getClickedInventory() == inventory && e.getSlot() != 4) {
            e.setCancelled(true);
        }
    }

    @Override
    public void onClickOwn(InventoryClickEvent e) {
        // Player inventory clicked while this menu is open
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Pet pet = petManager.getPetFromItem(clicked);
        if (pet == null) return;

        // Equip only if center is not already a pet
        if (petManager.getEquipped().get(player.getUniqueId()) != null) {
            player.sendMessage(CC.t("&cYou already have a pet equipped. Click the slot to unequip."));
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        // Remove one from stack
        if (clicked.getAmount() > 1) {
            clicked.setAmount(clicked.getAmount() - 1);
        } else {
            e.setCurrentItem(new ItemStack(Material.AIR));
        }

        // Equip and show in menu
        petManager.equip(player, pet, true);
        inventory.setItem(4, pet.toItem());
        player.updateInventory();
    }
}
