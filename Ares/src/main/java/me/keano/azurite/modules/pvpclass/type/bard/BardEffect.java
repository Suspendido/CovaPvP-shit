package me.keano.azurite.modules.pvpclass.type.bard;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Map;

@Getter
@Setter
public class BardEffect extends Module<PvPClassManager> {

    private PotionEffect effect;
    private int bardDistance;
    private int energyRequired;
    private boolean effectFriendlies;
    private boolean effectSelf;
    private boolean effectEnemies;
    private boolean clickable;

    public BardEffect(PvPClassManager manager, Map<String, Object> map, boolean clickable) {
        super(manager);
        this.effect = Serializer.getEffect((String) map.get("EFFECT"));
        this.bardDistance = getClassesConfig().getInt("BARD_CLASS.BARD_DISTANCE");
        this.effectFriendlies = (boolean) map.get("EFFECT_FRIENDLIES");
        this.effectSelf = (boolean) map.get("EFFECT_SELF");
        this.effectEnemies = (boolean) map.get("EFFECT_ENEMIES");
        this.clickable = clickable;

        if (clickable) {
            this.energyRequired = (int) map.get("ENERGY_REQUIRED");
        }
    }

    public BardEffect(PvPClassManager manager, boolean clickable, PotionEffect effect) {
        super(manager);
        this.clickable = clickable;
        this.effect = effect;
        this.bardDistance = getClassesConfig().getInt("BARD_CLASS.BARD_DISTANCE");
        this.energyRequired = 0;
        this.effectFriendlies = true;
        this.effectSelf = true;
        this.effectEnemies = false;
    }

    public void applyEffect(Player player) {
        if (effect == null) {
            return; // Evita NullPointer si no hay efecto
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (effectSelf) {
            getManager().addEffect(player, effect);
        }

        int halfDist = bardDistance / 2;
        int affected = (effectSelf ? 1 : 0);

        for (Entity nearbyEntity : player.getNearbyEntities(bardDistance, halfDist, bardDistance)) {
            if (!(nearbyEntity instanceof Player)) continue;
            if (nearbyEntity == player) continue;

            Player nearby = (Player) nearbyEntity;
            Team atNearby = getInstance().getTeamManager().getClaimManager().getTeam(nearby.getLocation());

            if (atNearby instanceof SafezoneTeam) continue;

            if (effectFriendlies && pt != null && pt.getPlayers().contains(nearby.getUniqueId())) {
                boolean alreadyHasEffect = nearby.getActivePotionEffects().stream()
                        .anyMatch(activeEffect -> activeEffect.getType().equals(effect.getType()) &&
                                activeEffect.getAmplifier() >= effect.getAmplifier());

                if (!alreadyHasEffect) {
                    getManager().addEffect(nearby, effect);

                    if (clickable) {
                        sendVillagerParticles(nearby);
                        affected++;
                        nearby.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.TEAM_EFFECT")
                                .replace("%player%", player.getName())
                                .replace("%effect%", Utils.convertName(effect.getType()))
                        );
                    }
                }
            }

            if (effectEnemies) {
                if (pt != null) {
                    if (pt.isAlly(nearby)) continue;
                    if (pt.getPlayers().contains(nearby.getUniqueId())) continue;
                }

                affected++;
                getManager().addEffect(nearby, effect);
            }
        }

        if (clickable) {
            if (energyRequired == 0) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.USED_EFFECT_NO_ENERGY")
                        .replace("%effect%", Utils.convertName(effect.getType()))
                );
            } else {
                sendVillagerParticles(player);
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.USED_EFFECT")
                        .replace("%effect%", Utils.convertName(effect.getType()))
                        .replace("%energy%", String.valueOf(energyRequired))
                        .replace("%affected%", String.valueOf(affected))
                );
            }
        }
    }

    // Partículas verdes de aldeano alrededor del jugador
    public void sendVillagerParticles(Player player) {
        int particleCount = 12; // Pocas partículas
        double radius = 0.6; // Radio alrededor del jugador
        Location base = player.getLocation();

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = 0.2 + (Math.random() * 1.0); // Altura aleatoria leve

            Location particleLoc = base.clone().add(x, y, z);
            player.getWorld().spigot().playEffect(
                    particleLoc,
                    Effect.HAPPY_VILLAGER, // Partículas verdes del aldeano
                    0,
                    0,
                    0, 0, 0,
                    1.0F, // Velocidad
                    0,
                    32 // Distancia máxima visible
            );
        }
    }
}
