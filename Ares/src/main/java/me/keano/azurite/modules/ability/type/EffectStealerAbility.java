package me.keano.azurite.modules.ability.type;

/*
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 15/01/2025
 */

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EffectStealerAbility extends Ability {

    private final Map<UUID, Integer> hits;
    private final int maxHits;
    private final int duration;

    public EffectStealerAbility(AbilityManager manager) {
        super(manager,
                AbilityUseType.HIT_PLAYER,
                "Effect Stealer"
        );
        this.hits = new HashMap<>();
        this.maxHits = getAbilitiesConfig().getInt("EFFECT_STEALER.HITS_REQUIRED");
        this.duration = getAbilitiesConfig().getInt("EFFECT_STEALER.DURATION");
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;
        if (!hits.containsKey(damagerUUID)) hits.put(damagerUUID, 0);

        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);

        damager.playSound(damager.getLocation(), Sound.GLASS, 20 ,20);

        ItemStack damagedHelmet = damaged.getInventory().getHelmet();
        if (damagedHelmet == null || !damagedHelmet.getType().equals(org.bukkit.Material.DIAMOND_HELMET)) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.EFFECT_STEALER.NO_CLASSES"));
            return;
        }

        if (current == maxHits) {
            hits.remove(damagerUUID);

            takeItem(damager);
            applyCooldown(damager);

            Collection<PotionEffect> effectsToSteal = damaged.getActivePotionEffects();
            if (effectsToSteal.isEmpty()) {

                String m = getLanguageConfig().getString("ABILITIES.EFFECT_STEALER.NO_EFFECTS");
                damager.sendMessage(m);
                return;
            }

            Map<PotionEffectType, PotionEffect> stolenEffects = new HashMap<>();

            for (PotionEffect effect : effectsToSteal) {
                stolenEffects.put(effect.getType(), new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier()));
                damaged.removePotionEffect(effect.getType());
            }

            for (PotionEffect effect : stolenEffects.values()) {
                damager.addPotionEffect(new PotionEffect(effect.getType(), 200, effect.getAmplifier())); // 10 seconds = 200 ticks
            }

            damager.playSound(damager.getLocation(), Sound.SUCCESSFUL_HIT, 20, 20);
            damaged.playSound(damaged.getLocation(), Sound.VILLAGER_HIT, 20, 20);

            for (String s : getLanguageConfig().getStringList("ABILITIES.EFFECT_STEALER.STOLEN")) {
                damager.sendMessage(s.replace("%player%", damaged.getName()));
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.EFFECT_STEALER.STOLEN_BY")) {
                damaged.sendMessage(s.replace("%player%", damager.getName()));
            }

            Tasks.executeLater(getManager(), duration, () -> {
                for (PotionEffect effect : stolenEffects.values()) {
                    if (damager.hasPotionEffect(effect.getType())) {
                        damager.removePotionEffect(effect.getType());
                    }
                    damaged.addPotionEffect(effect);
                }

                damager.playSound(damaged.getLocation(), Sound.VILLAGER_YES, 20, 20);
                damaged.playSound(damaged.getLocation(), Sound.VILLAGER_NO, 20, 20);

                damager.sendMessage(getLanguageConfig().getString("ABILITIES.EFFECT_STEALER.RETURNED"));
                damaged.sendMessage(getLanguageConfig().getString("ABILITIES.EFFECT_STEALER.RESTORED"));

            });
        }
    }
}
