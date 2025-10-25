package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 07/07/2025
 */

public class AbilityDisablerAbility extends Ability {

    private final int seconds;
    private final int radius;

    private final Map<UUID, Long> playerCooldowns;

    public AbilityDisablerAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Ability Disabler"
        );
        this.seconds = getAbilitiesConfig().getInt("ABILITY_DISABLER.SECONDS_DISABLE");
        this.radius = getAbilitiesConfig().getInt("ABILITY_DISABLER.RADIUS");

        this.playerCooldowns = new ConcurrentHashMap<>();
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        applyAbilityCooldownToPlayersInRadius(player);

        for (String s : getLanguageConfig().getStringList("ABILITIES.ABILITY_DISABLER.USED")) {
            player.sendMessage(s);
        }
    }

    private void applyAbilityCooldownToPlayersInRadius(Player disablerPlayer) {
        long cooldownTime = System.currentTimeMillis() + (seconds * 1000L);

        for (Entity entity : disablerPlayer.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;

            Player nearbyPlayer = (Player) entity;

            if (getInstance().getTeamManager().canHit(disablerPlayer, nearbyPlayer, false)) {
                playerCooldowns.put(nearbyPlayer.getUniqueId(), cooldownTime);
                getInstance().getAbilityManager().getGlobalCooldown().applyTimer(nearbyPlayer, seconds);

                for (String s : getLanguageConfig().getStringList("ABILITIES.ABILITY_DISABLER.COOLDOWN_APPLIED")) {
                    nearbyPlayer.sendMessage(s.replace("%player%", disablerPlayer.getName()));
                }
            }
        }
    }

    public boolean hasAbilityCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        Long cooldownTime = playerCooldowns.get(playerId);

        if (cooldownTime == null) return false;

        if (System.currentTimeMillis() >= cooldownTime) {
            playerCooldowns.remove(playerId);

            for (String s : getLanguageConfig().getStringList("ABILITIES.ABILITY_DISABLER.COOLDOWN_EXPIRED")) {
                player.sendMessage(s);
            }
            return false;
        }

        return true;
    }

    public long getRemainingCooldownTime(Player player) {
        UUID playerId = player.getUniqueId();
        Long cooldownTime = playerCooldowns.get(playerId);

        if (cooldownTime == null) return 0;

        long remaining = cooldownTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }
}
