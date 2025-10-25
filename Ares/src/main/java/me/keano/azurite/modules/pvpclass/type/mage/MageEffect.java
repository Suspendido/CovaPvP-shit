package me.keano.azurite.modules.pvpclass.type.mage;

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
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class MageEffect extends Module<PvPClassManager> {

    private PotionEffect effect;

    private int mageDistance;
    private int energyRequired;

    private boolean effectFriendlies;
    private boolean effectSelf;
    private boolean effectEnemies;

    public MageEffect(PvPClassManager manager, Map<String, Object> map) {
        super(manager);
        this.effect = Serializer.getEffect((String) map.get("EFFECT"));
        this.mageDistance = getClassesConfig().getInt("MAGE_CLASS.MAGE_DISTANCE");
        this.energyRequired = (int) map.get("ENERGY_REQUIRED");
        this.effectFriendlies = (boolean) map.get("EFFECT_FRIENDLIES");
        this.effectSelf = (boolean) map.get("EFFECT_SELF");
        this.effectEnemies = (boolean) map.get("EFFECT_ENEMIES");
    }

    public void applyEffect(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MAGE_CLASS.USED_EFFECT")
                .replace("%effect%", Utils.convertName(effect.getType()))
                .replace("%energy%", String.valueOf(energyRequired))
        );


        java.util.List<Player> affectedPlayers = new ArrayList<>();


        if (effectSelf) {
            getManager().addEffect(player, effect);
            affectedPlayers.add(player);
        }

        int halfDist = mageDistance / 2;

        for (Entity nearbyEntity : player.getNearbyEntities(mageDistance, halfDist, mageDistance)) {
            if (!(nearbyEntity instanceof Player)) continue;
            Player nearby = (Player) nearbyEntity;
            if (nearby == player) continue;

            Team atNearby = getInstance().getTeamManager().getClaimManager().getTeam(nearby.getLocation());
            if (atNearby instanceof SafezoneTeam) continue;

            boolean shouldApply = false;

            if (effectFriendlies && pt != null && pt.getPlayers().contains(nearby.getUniqueId())) {
                getManager().addEffect(nearby, effect);
                shouldApply = true;
            }

            if (effectEnemies) {
                if (pt != null) {
                    if (pt.isAlly(nearby)) continue;
                    if (pt.getPlayers().contains(nearby.getUniqueId())) continue;
                }
                getManager().addEffect(nearby, effect);
                shouldApply = true;
            }

            if (shouldApply) {
                affectedPlayers.add(nearby);
            }
        }


        try {
            Sound sound = Sound.valueOf("FIRE_IGNITE");
            player.playSound(player.getLocation(), sound, 0.4f, 1.2f);
        } catch (IllegalArgumentException ex) {

            player.playSound(player.getLocation(), "fire.ignite", 0.4f, 1.2f);
        }


        sendEnchantParticles(player);


        for (Player affected : affectedPlayers) {
            if (affected != player) {
                sendEnchantParticles(affected);
            }
        }
    }


    public void sendEnchantParticles(Player player) {
        int particleCount = 8;
        double radius = 0.7;
        Location base = player.getLocation().add(0, 1.0, 0);

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = 0.1 + Math.random() * 0.6;

            Location particleLoc = base.clone().add(x, y, z);

            player.getWorld().spigot().playEffect(
                    particleLoc,
                    Effect.SPELL,
                    0, 0,
                    0F, 0F, 0F,
                    0.25F,
                    0,
                    32
            );
        }
    }
}