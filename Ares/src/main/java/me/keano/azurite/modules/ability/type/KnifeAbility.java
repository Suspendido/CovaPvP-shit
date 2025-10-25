package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KnifeAbility extends Ability {

    private final Map<UUID, Integer> hitCount;
    private final double bleedDamage;
    private final int bleedDuration;

    public KnifeAbility(AbilityManager manager) {
        super(manager, null, "Knife");
        this.hitCount = new HashMap<>();
        this.bleedDamage = getAbilitiesConfig().getDouble("KNIFE.BLEED_DAMAGE");
        this.bleedDuration = getAbilitiesConfig().getInt("KNIFE.BLEED_DURATION");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();

        if (!hasAbilityInHand(damager)) return;
        if (cannotUse(damager) || hasCooldown(damager)) return;

        hitCount.put(damaged.getUniqueId(), hitCount.getOrDefault(damaged.getUniqueId(), 0) + 1);

        if (hitCount.get(damaged.getUniqueId()) >= 3) {
            hitCount.remove(damaged.getUniqueId());
            applyBleedEffect(damaged);
            applyCooldown(damager);
            takeItem(damager);
            for (String s : getLanguageConfig().getStringList("ABILITIES.KNIFE_ABILITY.USED"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                        .replace("%seconds%", String.valueOf(bleedDuration))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.KNIFE_ABILITY.BEEN_HIT"))
                damaged.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(bleedDuration))
                );
        }
    }

    private void applyBleedEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, bleedDuration * 20, 1));

        new BukkitRunnable() {
            int timeLeft = bleedDuration;

            @Override
            public void run() {
                if (timeLeft <= 0 || player.isDead()) {
                    cancel();
                    return;
                }

                player.damage(bleedDamage);
                for (Player nearbyPlayer : player.getWorld().getPlayers()) {
                    if (nearbyPlayer.getLocation().distance(player.getLocation()) <= 10) {
                        nearbyPlayer.playEffect(player.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                    }
                }

                timeLeft--;
            }
        }.runTaskTimer(getInstance(), 0, 20);
    }
}
