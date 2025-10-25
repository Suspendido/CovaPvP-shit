package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AntiPearlAbility extends Ability {

    private final Map<UUID, Integer> hits;
    private final int maxHits;
    private final int seconds;

    public AntiPearlAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.HIT_PLAYER,
                "Anti Pearl"
        );
        this.hits = new HashMap<>();
        this.maxHits = getAbilitiesConfig().getInt("ANTI_PEARL.HITS_REQUIRED");
        this.seconds = getAbilitiesConfig().getInt("ANTI_PEARL.ENDERPEARL_SECONDS");
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;
        if (!hits.containsKey(damagerUUID)) hits.put(damagerUUID, 0);

        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);

        if (current == maxHits) {
            hits.remove(damagerUUID);

            takeItem(damager);
            applyCooldown(damager);
            getInstance().getTimerManager().getEnderpearlTimer().applyTimer(damaged, seconds * 1000L);

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_PEARL.USED"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                        .replace("%seconds%", String.valueOf(seconds))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_PEARL.BEEN_HIT"))
                damaged.sendMessage(s
                        .replace("%player%", damager.getName())
                );
        }
    }
}