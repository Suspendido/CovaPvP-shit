package me.keano.azurite.modules.payouts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 27/02/2025
 * Project: Zeus
 */

public class ItemUtils {

    public static ItemStack getCustomHead(String skullOwner) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(skullOwner);
        head.setItemMeta(meta);
        return head;
    }

}
