package me.keano.azurite.modules.pets.menu;

import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
import me.keano.azurite.modules.pets.Pet;
import me.keano.azurite.modules.pets.PetManager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

public class PetsEditorMenu extends PaginatedMenu {

    private final PetManager petManager;

    public PetsEditorMenu(MenuManager manager, Player player) {
        super(manager, player, CC.t("&9&lPets Editor"), 54, false);
        this.petManager = getInstance().getPetManager();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();

        int i = 0;
        for (Pet pet : petManager.getPets().values()) {
            final int slot = ++i;
            map.put(slot, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    if (e.getClick() == ClickType.LEFT) {
                        ItemUtils.giveItem(player, pet.toItem(), player.getLocation());
                        getManager().playSound(player, "ORB_PICKUP", false);
                        player.sendMessage(CC.t("&aGiven yourself &f" + pet.getDisplayName() + " &apet."));
                    } else if (e.getClick() == ClickType.RIGHT) {
                        if (!player.hasPermission("azurite.petadmin")) {
                            player.sendMessage(CC.t("&cYou do not have permission."));
                            return;
                        }
                        petManager.deletePet(pet.getId());
                        player.sendMessage(CC.t("&cDeleted pet &f" + pet.getDisplayName() + "&c."));
                        update();
                    }
                }

                @Override
                public ItemStack getItemStack() {
                    return new ItemBuilder(pet.toItem())
                            .addLoreLine(" ")
                            .addLoreLine("&eLeft-Click: &7Give to yourself")
                            .addLoreLine("&eRight-Click: &7Delete")
                            .addLoreLine("&7ID: &f" + pet.getId())
                            .toItemStack();
                }
            });
        }

        // Create button
        map.put(45, new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                if (!player.hasPermission("azurite.petadmin")) {
                    player.sendMessage(CC.t("&cYou do not have permission."));
                    return;
                }
                String id = "PET_" + (petManager.getPets().size() + 1);
                int tries = 0;
                while (petManager.getPets().containsKey(id) && tries++ < 100) {
                    id = "PET_" + (petManager.getPets().size() + 1 + tries);
                }
                petManager.createPet(id);
                player.sendMessage(CC.t("&aCreated pet with id &f" + id + "&a. Use &e/pet set " + id + " name <name>&a, &e/pet set " + id + " texture <base64>&a, &e/pet set " + id + " effects <EFFECT:LEVEL,...>&a."));
                update();
            }

            @Override
            public ItemStack getItemStack() {
                return new ItemBuilder(Material.ANVIL)
                        .setName("&aCreate Pet")
                        .setLore("&7Click to create a new pet")
                        .toItemStack();
            }
        });

        return map;
    }
}

