package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.signs.items.ItemSignType;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 29/01/2025
 * Project: ZeusHCF
 */

public class SignListener extends Module<ListenerManager> {

    public SignListener(ListenerManager manager) {
        super(manager);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        if (block.getType().name().contains("SIGN")) {
            Sign sign = (Sign) block.getState();
            ItemStack item = getInstance().getCustomSignManager().getCustomSign(Arrays.asList(sign.getLines()));

            if (item != null) {
                e.setCancelled(true);
                block.setType(Material.AIR);

                if (Config.GIVE_BLOCKS_ON_MINE) {
                    ItemUtils.giveItem(player, item, player.getLocation());

                } else {
                    player.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        Block block = e.getBlock();
        Player player = e.getPlayer();

        if (item == null) return;
        if (item.getType() != Material.SIGN) return;
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasLore()) return;
        if (!block.getType().name().contains("SIGN")) return;

        ItemMeta meta = item.getItemMeta();

        for (ItemSignType signType : ItemSignType.values()) {
            if (!signType.getItemName(getManager()).equals(meta.getDisplayName())) continue;
            if (!getInstance().getCustomSignManager().isCustomSign(meta.getLore())) continue;

            Sign sign = (Sign) block.getState();
            List<String> lines = meta.getLore();

            for (int i = 0; i < lines.size(); i++) {
                sign.setLine(i, lines.get(i));
            }

            sign.update();
            player.closeInventory();
            break;
        }
    }

    @EventHandler
    public void onSign(SignChangeEvent e) {
        Sign sign = (Sign) e.getBlock().getState();

        if (getInstance().getCustomSignManager().isCustomSign(Arrays.asList(sign.getLines()))) {
            e.setCancelled(true);
        }
    }
}