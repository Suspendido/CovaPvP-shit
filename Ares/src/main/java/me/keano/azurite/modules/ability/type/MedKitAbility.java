package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MedKitAbility extends Ability {

    private final List<PotionEffect> effects;
    private final int maxAirHeight;

    public MedKitAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Med Kit"
        );
        this.effects = getAbilitiesConfig().getStringList("MED_KIT.EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList());

        this.maxAirHeight = getAbilitiesConfig().getInt("MED_KIT.MAX_AIR_HEIGHT", 5);
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        // Checar altura en el aire
        int airDistance = getAirDistance(player);
        if (airDistance > maxAirHeight) {
            player.sendMessage("§cYou cannot use this ability while in the air higher than " + maxAirHeight + " blocks.");
            return;
        }

        takeItem(player);
        applyCooldown(player);

        for (PotionEffect effect : effects) {
            getInstance().getClassManager().addEffect(player, effect);
        }

        for (String s : getLanguageConfig().getStringList("ABILITIES.MED_KIT.USED")) {
            player.sendMessage(s);
        }
    }

    /**
     * Cuenta los bloques de aire debajo del jugador hasta el próximo sólido.
     */
    private int getAirDistance(Player player) {
        int distance = 0;
        Block block = player.getLocation().getBlock();

        while (block.getY() > 0) {
            block = block.getRelative(0, -1, 0);
            if (block.getType() != Material.AIR) {
                break;
            }
            distance++;
        }
        return distance;
    }
}
