package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */

public class GappleTimer extends PlayerTimer {

    private final int regenSeconds;
    private final int regenAmplifier;

    public GappleTimer(TimerManager manager) {
        super(
                manager,
                /* action */ null,
                /* async */ false,
                "Gapple",
                "PLAYER_TIMERS.GAPPLE",
                "TIMERS_COOLDOWN.GAPPLE"
        );

        this.regenSeconds   = getConfig().getInt("TIMERS_COOLDOWN.GAPPLE_DURATION");
        this.regenAmplifier = getConfig().getInt("TIMERS_COOLDOWN.GAPPLE_AMPLIFIER");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        final Player player = e.getPlayer();
        final ItemStack item = e.getItem();

        if (item == null
                || item.getType() != Material.GOLDEN_APPLE
                || item.getDurability() != (short) 1
                || !getManager().isGapple(item)) {
            return;
        }

        if (hasTimer(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("GAPPLE_TIMER.COOLDOWN")
                    .replace("%seconds%", getRemainingString(player)));
            return;
        }

        if (regenSeconds > 0) {
            final int amp = Math.max(regenAmplifier - 1, 0);
            final PotionEffect regen = new PotionEffect(
                    PotionEffectType.REGENERATION,
                    regenSeconds * 20,
                    amp
            );

            Bukkit.getScheduler().runTaskLater(getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.addPotionEffect(regen, true);
                }
            }, 1L);
        }

        if (seconds != 0) {
            applyTimer(player);

            for (String s : getLanguageConfig().getStringList("GAPPLE_TIMER.ADDED_COOLDOWN")) {
                player.sendMessage(s.replace(
                        "%cooldown%",
                        Formatter.formatDetailed(getSeconds() * 1000L)
                ));
            }

            player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 20);
        }
    }
}
