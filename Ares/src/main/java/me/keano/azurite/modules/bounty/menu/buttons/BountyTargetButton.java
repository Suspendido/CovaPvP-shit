package me.keano.azurite.modules.bounty.menu.buttons;

import lombok.RequiredArgsConstructor;
import me.keano.azurite.modules.bounty.BountyData;
import me.keano.azurite.modules.bounty.BountyManager;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class BountyTargetButton extends Button {

    private final BountyManager manager;

    private final Player player;
    private final Player target;

    private final BountyData bountyData;

    @Override
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        player.playSound(player.getLocation(), Sound.CLICK, 1F, 1F);

        e.setCancelled(true);

        if (bountyData.hasPlayer(player.getUniqueId())) {
            player.sendMessage(CC.t("&cYou're already tracking this player!"));
            player.playSound(player.getLocation(), Sound.DIG_GRASS, 20F, 0.1F);
            return;
        }

        if (target == player) {
            player.sendMessage(CC.t("&cYou cannot track yourself!"));
            player.playSound(player.getLocation(), Sound.DIG_GRASS, 20F, 0.1F);
            return;
        }

        manager.getBounties().values().forEach(data -> data.removePlayer(player.getUniqueId()));
        bountyData.addPlayer(player.getUniqueId());

        player.sendMessage(CC.t("&eNow you're tracking to &f" + bountyData.getTargetName() + "&e."));
        player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1F, 1F);

        player.setCompassTarget(Bukkit.getPlayer(bountyData.getTarget()).getLocation());
        player.closeInventory();
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack item = new ItemBuilder(Material.SKULL_ITEM)
                .data(manager, (short) 3)
                .setSkullOwner(target.getName())
                .setName(ChatColor.RED + target.getName())
                .setLore(
                        "",
                        "&fBounty&7: &2$&a" + bountyData.getAmount(),
                        ""
                )
                .toItemStack();

        if (bountyData.hasPlayer(player.getUniqueId())) {
            item = new ItemBuilder(item).addLoreLine("&cYou're already tracking this player").toItemStack();
        } else {
            item = new ItemBuilder(item).addLoreLine("&eClick to track player").toItemStack();
        }

        return item;
    }
}