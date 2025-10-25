package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;

public class SoupListener extends Module<ListenerManager> {


    private final HCF hcf;

    public SoupListener(ListenerManager manager, HCF hcf) {
        super(manager);
        this.hcf = hcf;


        this.load();
    }

    private void load() {
    }

    @EventHandler
    public void onPlayerUseSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (item == null || item.getType() != Material.MUSHROOM_SOUP) return;
        if (!hcf.isSoup()) return;

        double healAmount = hcf.getConfig().getDouble("SOUP_HEAL_AMOUNT");

        if (player.getHealth() >= player.getMaxHealth() || healAmount <= 0) return;

        double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + healAmount);
        player.setHealth(newHealth);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().addItem(new ItemStack(Material.BOWL));
        } else {
            player.setItemInHand(new ItemStack(Material.BOWL));
        }

        event.setCancelled(true);
    }
    @EventHandler
    public void onBowlDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.BOWL) {
            event.getItemDrop().remove();
        }
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (hcf.isSoup()) {
                player.setFoodLevel(20);
                event.setCancelled(true);
            }
        }
    }
}
