package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotionCounterAbility extends Ability {

    private final Map<UUID, Integer> hits;
    private final int maxHits;

    public PotionCounterAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.HIT_PLAYER,
                "Potion Counter"
        );
        this.hits = new HashMap<>();
        this.maxHits = getAbilitiesConfig().getInt("POTION_COUNTER.HITS_REQUIRED");
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

            int potionCount = countInstantRegenPotions(damaged);

            for (String s : getLanguageConfig().getStringList("ABILITIES.POTION_COUNTER.USED"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                        .replace("%potion_count%", String.valueOf(potionCount))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.POTION_COUNTER.BEEN_HIT"))
                damaged.sendMessage(s.replace("%player%", damager.getName()));
        }
    }

    private int countInstantRegenPotions(Player damaged) {
        int count = 0;
        for (ItemStack item : damaged.getInventory().getContents()) {
            if (item != null && item.getType() == Material.POTION) {
                if (item.getDurability() == 16421) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

}