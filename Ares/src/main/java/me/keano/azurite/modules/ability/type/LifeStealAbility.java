package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class LifeStealAbility extends Ability {

    private final Set<UUID> activeUsers;
    private final int duration;
    private final double lifeStealPercentage;
    private final double maxHealth;

    public LifeStealAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "LifeSteal"
        );

        this.activeUsers = new HashSet<>();
        this.duration = getAbilitiesConfig().getInt("LIFESTEAL.DURATION");
        this.lifeStealPercentage = getAbilitiesConfig().getDouble("LIFESTEAL.PERCENTAGE");
        this.maxHealth = 20.0;
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        activeUsers.add(player.getUniqueId());
        takeItem(player);
        applyCooldown(player);

        for (String s : getLanguageConfig().getStringList("ABILITIES.LIFESTEAL_ABILITY.USED")) {
            player.sendMessage(s
                    .replace("%seconds%", String.valueOf(duration)));
        }

        Tasks.executeLater(getManager(), 20L * duration, () -> {
            activeUsers.remove(player.getUniqueId());

            for (String s : getLanguageConfig().getStringList("ABILITIES.LIFESTEAL_ABILITY.EXPIRED")) {
                player.sendMessage(s);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();

        if (!activeUsers.contains(damager.getUniqueId())) return;

        double damageDealt = e.getDamage();
        double stolenHealth = damageDealt * lifeStealPercentage;
        double newHealth = Math.min(damager.getHealth() + stolenHealth, maxHealth);

        damager.setHealth(newHealth);
        damaged.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, false));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        activeUsers.remove(e.getEntity().getUniqueId());
    }
}
