package me.keano.azurite.modules.ability.type;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.glow.GlowModule;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.ApolloGlowRuntime;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.Color;
import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/25/2025
 * Project: Ares
 */

public class XRayModeAbility extends Ability implements Listener {

    private final int seconds;
    private final int radius;
    private final Color glowColor;
    private final boolean includeSelf;
    private final boolean includeInvisible;
    private final boolean revealInvisibleByRemoving;
    private final GlowModule glowModule;

    private final Set<UUID> activeGlows = new HashSet<>();

    public XRayModeAbility(AbilityManager manager) {
        super(manager, AbilityUseType.INTERACT, "XRay");

        FileConfiguration c = getAbilitiesConfig();
        this.seconds = c.getInt("XRAY.SECONDS", 10);
        this.radius = c.getInt("XRAY.RADIUS", 25);
        this.glowColor = parseColor(c.getString("XRAY.COLOR", "#00FFFF"));
        this.includeSelf = c.getBoolean("XRAY.INCLUDE_SELF", false);
        this.includeInvisible = c.getBoolean("XRAY.INCLUDE_INVISIBLE", true);
        this.revealInvisibleByRemoving = c.getBoolean("XRAY.REVEAL_INVISIBLE_BY_REMOVING", true);

        this.glowModule = Apollo.getModuleManager().getModule(GlowModule.class);

        Bukkit.getPluginManager().registerEvents(this, manager.getInstance());
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        Set<UUID> affected = new HashSet<>();
        Map<UUID, PotionEffect> invisBefore = new HashMap<>();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() != player.getWorld()) continue;
            if (!includeSelf && target.getUniqueId().equals(player.getUniqueId())) continue;
            if (player.getLocation().distanceSquared(target.getLocation()) > (radius * radius)) continue;

            PotionEffect invisEffect = getInvisibilityEffect(target);
            boolean isPotionInvisible = (invisEffect != null);

            if (!includeInvisible && isPotionInvisible) continue;

            if (isPotionInvisible && revealInvisibleByRemoving) {
                invisBefore.put(target.getUniqueId(), invisEffect);
                target.removePotionEffect(PotionEffectType.INVISIBILITY);
            }

            try {
                ApolloGlowRuntime.overrideGlow(glowModule, target.getUniqueId(), glowColor);
                affected.add(target.getUniqueId());

                for (String msg : getLanguageConfig().getStringList("ABILITIES.XRAY.TARGET_ALERT")) {
                    target.sendMessage(msg.replace("%seconds%", String.valueOf(seconds))
                            .replace("%by%", player.getName())
                            .replace("%radius%", String.valueOf(radius)));
                }
            } catch (Exception ignored) {}
        }

        activeGlows.addAll(affected);

        for (String s : getLanguageConfig().getStringList("ABILITIES.XRAY.USED")) {
            player.sendMessage(s.replace("%seconds%", String.valueOf(seconds))
                    .replace("%radius%", String.valueOf(radius)));
        }

        if (!affected.isEmpty() || !invisBefore.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(
                    getManager().getInstance(),
                    () -> {
                        for (UUID id : affected) {
                            try { ApolloGlowRuntime.resetGlow(glowModule, id); } catch (Exception ignored) {}
                            activeGlows.remove(id);
                        }
                        for (Map.Entry<UUID, PotionEffect> e : invisBefore.entrySet()) {
                            Player t = Bukkit.getPlayer(e.getKey());
                            if (t == null) continue;
                            PotionEffect original = e.getValue();
                            int remaining = Math.max(0, original.getDuration() - (seconds * 20));
                            if (remaining > 0) {
                                t.addPotionEffect(new PotionEffect(
                                        PotionEffectType.INVISIBILITY,
                                        remaining,
                                        original.getAmplifier(),
                                        original.isAmbient(),
                                        original.hasParticles()
                                ), true);
                            }
                        }
                        for (String s : getLanguageConfig().getStringList("ABILITIES.XRAY.ENDED")) {
                            player.sendMessage(s.replace("%seconds%", String.valueOf(seconds)));
                        }
                    },
                    seconds * 20L
            );
        }
    }

    private PotionEffect getInvisibilityEffect(Player p) {
        for (PotionEffect pe : p.getActivePotionEffects()) {
            if (pe.getType().equals(PotionEffectType.INVISIBILITY)) return pe;
        }
        return null;
    }

    private Color parseColor(String input) {
        if (input == null) return Color.CYAN;
        String s = input.trim();
        try {
            if (s.startsWith("#")) s = s.substring(1);
            if (s.matches("(?i)^[0-9A-F]{6}$")) return new Color(Integer.parseInt(s, 16));
        } catch (Exception ignored) {}
        switch (s.toUpperCase()) {
            case "RED": return Color.RED;
            case "GREEN": return Color.GREEN;
            case "BLUE": return Color.BLUE;
            case "YELLOW": return Color.YELLOW;
            case "PURPLE": case "MAGENTA": return Color.MAGENTA;
            case "PINK": return Color.PINK;
            case "ORANGE": return Color.ORANGE;
            case "WHITE": return Color.WHITE;
            case "BLACK": return Color.BLACK;
            case "GRAY": case "GREY": return Color.GRAY;
            case "CYAN": case "AQUA": default: return Color.CYAN;
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getPotion().getEffects().stream().anyMatch(pe -> pe.getType().equals(PotionEffectType.INVISIBILITY))) {
            for (LivingEntity ent : e.getAffectedEntities()) {
                if (ent instanceof Player) {
                    Player p = (Player) ent;
                    if (activeGlows.contains(p.getUniqueId())) {
                        e.setIntensity(p, 0.0);
                        p.sendMessage("§cYou can not use invisibility while revelled by XRay.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) return;
        if (e.getItem().getType().toString().contains("POTION")) {
            if (e.getItem().getDurability() == 8270 || e.getItem().getDurability() == 16382) {
                if (activeGlows.contains(e.getPlayer().getUniqueId())) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cYou can not use invisibility while revelled by XRay.");
                }
            }
        }
    }
}
