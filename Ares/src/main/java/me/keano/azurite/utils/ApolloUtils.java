package me.keano.azurite.utils;

import com.lunarclient.apollo.Apollo;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ApolloUtils {

    public static ItemStack applyGlint(ItemStack item, String color) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound lunarCompound = nbtItem.getOrCreateCompound("lunar");
        lunarCompound.setString("glint", color);

        return nbtItem.getItem();
    }

    public static boolean isUsingLC(Player player) {
        return Apollo.getPlayerManager().hasSupport(player.getUniqueId());
    }
}
