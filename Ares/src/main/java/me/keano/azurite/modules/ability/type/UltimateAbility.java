package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UltimateAbility extends Ability {

    private final List<PotionEffect> effects;
    private final int seconds;

    public UltimateAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Ultimate Ability"
        );
        this.effects = getAbilitiesConfig().getStringList("ULTIMATE_ABILITY.EFFECTS").stream().map(Serializer::getEffect).collect(Collectors.toList());
        this.seconds = getAbilitiesConfig().getInt("ULTIMATE_ABILITY.SECONDS");
    }

    private final Map<UUID, Integer> activeParticleTasks = new HashMap<>();

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        // Crear el efecto de partículas donde el jugador usa el ítem
        int taskId = createParticleEffect(player);
        activeParticleTasks.put(player.getUniqueId(), taskId);

        Location location = player.getLocation();
        Tasks.executeLater(getManager(), 20L * seconds, () -> {

            String m = getAbilitiesConfig().getString("ULTIMATE_ABILITY.BACK");
            player.teleport(location);
            player.sendMessage(m);
            stopParticleEffect(player);
        });

        for (String s : getLanguageConfig().getStringList("ABILITIES.ULTIMATE_ABILITY.USED")) {
            player.sendMessage(s);
        }

        for (PotionEffect effect : effects) {
            getInstance().getClassManager().addEffect(player, effect);
        }
    }

    private int createParticleEffect(Player player) {
        Location loc = player.getLocation().clone();
        loc = loc.subtract(0, 0, 0);

        Location finalLoc = loc;
        return Tasks.repeat(getManager(), 0L, 2L, () -> {
            if (!player.isOnline()) return;

            player.getWorld().playEffect(finalLoc, Effect.SPELL, 0);
        });
    }

    /**
     * Detener el efecto de partículas cuando el jugador es teletransportado.
     */
    private void stopParticleEffect(Player player) {
        Integer taskId = activeParticleTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Tasks.cancelTask(taskId);
        }
    }

}
