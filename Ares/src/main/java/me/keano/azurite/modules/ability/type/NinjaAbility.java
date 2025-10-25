package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.TeleportTask;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NinjaAbility con AntiBuild + efectos configurables
 */
public class NinjaAbility extends Ability {

    private final Set<PotionEffect> effects;
    private final int seconds;
    private final int hitsValid;
    private final int antiBuildTime;

    public NinjaAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Ninja Ability"
        );

        this.seconds = getAbilitiesConfig().getInt("NINJA_ABILITY.SECONDS");
        this.hitsValid = getAbilitiesConfig().getInt("NINJA_ABILITY.HITS_VALID");
        this.antiBuildTime = getAbilitiesConfig().getInt("NINJA_ABILITY.ANTI_BUILD_TIME");
        this.effects = loadEffects("NINJA_ABILITY.EFFECTS_ON_TELEPORT");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        // Usa el damager guardado por FocusMode
        Player damager = ((FocusModeAbility) getManager().getAbility("FocusMode")).getDamager(player, hitsValid);

        if (damager == null) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.NINJA_ABILITY.NO_LAST_HIT"));
            return;
        }

        AntiBuildAbility antiBuildAbility = (AntiBuildAbility) getManager().getAbility("AntiBuild");

        takeItem(player);
        applyCooldown(player);

        // Aplicar AntiBuild al atacante
        antiBuildAbility.getAntiBuild().applyCooldown(damager, antiBuildTime);

        // Task de teletransporte con delay
        new TeleportTask(this, () -> {
            player.teleport(damager);

            // Aplica efectos configurados
            for (PotionEffect effect : effects) {
                getInstance().getClassManager().addEffect(player, effect);
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.NINJA_ABILITY.TELEPORTED_SUCCESSFULLY")) {
                player.sendMessage(s.replace("%player%", damager.getName()));
            }
        }, (i) -> {
            for (String s : getLanguageConfig().getStringList("ABILITIES.NINJA_ABILITY.TELEPORTING"))
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.NINJA_ABILITY.TELEPORTING_ATTACKER"))
                damager.sendMessage(s
                        .replace("%player%", player.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );
        }, seconds);
    }

    /**
     * Parser de efectos desde config
     * Formato esperado: EFFECT_NAME, 8s, 2
     */
    private Set<PotionEffect> loadEffects(String path) {
        return getAbilitiesConfig().getStringList(path).stream().map(line -> {
            try {
                String[] parts = line.split(",");
                String type = parts[0].trim().toUpperCase();
                String durationRaw = parts[1].trim().toLowerCase().replace("s", "");
                String amplifierRaw = parts[2].trim();

                int duration = Integer.parseInt(durationRaw); // en segundos
                int amplifier = Integer.parseInt(amplifierRaw) - 1; // Bukkit usa 0 = nivel 1

                PotionEffectType effectType = PotionEffectType.getByName(type);
                if (effectType == null) {
                    System.out.println("⚠️ Efecto inválido en config: " + type);
                    return null;
                }

                return new PotionEffect(effectType, duration * 20, amplifier);
            } catch (Exception e) {
                System.out.println("⚠️ Error al leer efecto: " + line);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
