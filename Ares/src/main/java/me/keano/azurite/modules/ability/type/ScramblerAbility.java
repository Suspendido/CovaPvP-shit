package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ScramblerAbility extends Ability {

    private final Set<UUID> scrambled; // jugadores que activaron la ability
    private final int chance;

    public ScramblerAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Scrambler"
        );
        this.scrambled = new HashSet<>();
        this.chance = getAbilitiesConfig().getInt("SCRAMBLER.CHANCE");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();

        // Checamos que tenga el item correcto en la mano
        if (!hasAbilityInHand(player)) return;

        if (cannotUse(player) || hasCooldown(player)) {
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        // Aplicamos cooldown y marcamos al jugador
        applyCooldown(player);
        scrambled.add(player.getUniqueId());
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Egg)) return;

        Player damager = Utils.getDamager(e.getDamager());
        Player damaged = (Player) e.getEntity();

        if (damager == null) return;
        if (!scrambled.contains(damager.getUniqueId())) return;

        if (e.isCancelled()) {
            damager.getInventory().addItem(item);
            damager.updateInventory(); // refund
            return;
        }

        // Ejecutamos scramble con % chance
        if (new Random().nextInt(100) < chance) {
            shuffleHotbar(damaged);

            damager.playSound(damager.getLocation(), Sound.SUCCESSFUL_HIT, 1f, 1f);
            damaged.playSound(damaged.getLocation(), Sound.VILLAGER_HIT, 1f, 1f);

            for (String s : getLanguageConfig().getStringList("ABILITIES.SCRAMBLER.SCRAMBLED"))
                damager.sendMessage(s.replace("%player%", damaged.getName()));

            for (String s : getLanguageConfig().getStringList("ABILITIES.SCRAMBLER.SCRAMBLED_BY"))
                damaged.sendMessage(s.replace("%player%", damager.getName()));
        }

        // Solo se puede usar una vez por huevo lanzado
        scrambled.remove(damager.getUniqueId());
    }

    private void shuffleHotbar(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] hotbarContents = Arrays.copyOfRange(inventory.getContents(), 0, 9);

        List<ItemStack> hotbarItems = Arrays.asList(hotbarContents);
        Collections.shuffle(hotbarItems);

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, hotbarItems.get(i));
        }

        player.updateInventory();
    }
}
