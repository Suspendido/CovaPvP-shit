package me.keano.azurite.modules.bounty.listener;

import me.keano.azurite.modules.bounty.BountyData;
import me.keano.azurite.modules.bounty.BountyManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BountyListener extends Module<BountyManager> {

    public BountyListener(BountyManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        BountyData data = getManager().getBounties().values().stream()
                .findFirst()
                .filter(bountyData -> bountyData.hasPlayer(player.getUniqueId()))
                .orElse(null);

        if (data == null) return;

        Player target = Bukkit.getPlayer(data.getTarget());
        if (target == null) return;

        String distance = String.format("%.2f", player.getLocation().distance(target.getLocation()));

        player.setItemInHand(new ItemBuilder(item).setName("&eTracking Player&7: &f" + target.getName() + " &7(" + distance + "m)").toItemStack());
        player.setCompassTarget(Bukkit.getPlayer(data.getTarget()).getLocation());

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player target = event.getEntity();
        Player killer = target.getKiller();

        if (killer == null) return;

        BountyData bountyData = getManager().getBounties().get(target.getUniqueId());
        if (bountyData == null) return;

        int bountyAmount = bountyData.getAmount();
        getManager().onTargetDeath(target, killer);

        killer.sendMessage(getLanguageConfig().getString("BOUNTY.CLAIMED")
                .replace("%amount%", String.valueOf(bountyAmount))
                .replace("%player%", target.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target))));

        Bukkit.broadcastMessage(getLanguageConfig().getString("BOUNTY.DEATH.GLOBAL")
                .replace("%player%", killer.getName())
                .replace("%target%", target.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                .replace("%amount%", String.valueOf(bountyAmount)));

        if (getInstance().getConfig().getBoolean("BOUNTY.REWARDS_ENABLED")) {
            getInstance().getConfig().getStringList("BOUNTY.REWARDS_COMMANDS").forEach(command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%player%", killer.getName())
                            .replace("%prefix%", CC.t(getInstance().getRankHook().getRankPrefix(killer)))));
        }
    }
}