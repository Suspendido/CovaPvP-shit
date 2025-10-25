package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.pvpclass.type.rogue.RogueBackstabEvent;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class GuardianAngelAbility extends Ability {

    private final Set<UUID> activePlayers;
    private final List<PotionEffect> effects;
    private final int duration;
    private final double healthThreshold;
    private final Map<UUID, Float> savedExp = new HashMap<>();
    private final Map<UUID, Integer> savedLevels = new HashMap<>();

    public GuardianAngelAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Guardian Angel"
        );
        this.activePlayers = new HashSet<>();
        this.effects = getAbilitiesConfig().getStringList("GUARDIAN_ANGEL.EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList());
        this.duration = getAbilitiesConfig().getInt("GUARDIAN_ANGEL.DURATION");
        this.healthThreshold = getAbilitiesConfig().getDouble("GUARDIAN_ANGEL.HEALTH_THRESHOLD", 1.5);
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        activePlayers.add(player.getUniqueId());

        for (String s : getLanguageConfig().getStringList("ABILITIES.GUARDIAN_ANGEL.ACTIVATED")) {
            player.sendMessage(s);
        }

        startExperienceBarTimer(player);

        Tasks.executeLater(getManager(), 20L * duration, () -> {
            if (activePlayers.contains(player.getUniqueId())) {
                activePlayers.remove(player.getUniqueId());
                restorePlayerExp(player);
                player.sendMessage(getLanguageConfig().getString("ABILITIES.GUARDIAN_ANGEL.EXPIRED"));
            }
        });
    }

    private void startExperienceBarTimer(Player player) {
        UUID uuid = player.getUniqueId();

        savedExp.put(uuid, player.getExp());
        savedLevels.put(uuid, player.getLevel());

        new BukkitRunnable() {
            int timeLeft = duration * 20;

            @Override
            public void run() {
                if (!activePlayers.contains(uuid)) {
                    restorePlayerExp(player);
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    restorePlayerExp(player);
                    activePlayers.remove(uuid);
                    player.sendMessage(getLanguageConfig().getString("ABILITIES.GUARDIAN_ANGEL.EXPIRED"));
                    cancel();
                    return;
                }

                float progress = (float) timeLeft / (duration * 20);
                player.setExp(progress);
                player.setLevel((int) Math.ceil(timeLeft / 20.0));

                timeLeft--;
            }
        }.runTaskTimer(getManager().getInstance(), 0L, 1L);
    }

    private void restorePlayerExp(Player player) {
        UUID uuid = player.getUniqueId();

        if (savedExp.containsKey(uuid) && savedLevels.containsKey(uuid)) {
            player.setExp(savedExp.get(uuid));
            player.setLevel(savedLevels.get(uuid));
            savedExp.remove(uuid);
            savedLevels.remove(uuid);
        } else {
            player.setExp(0);
            player.setLevel(0);
        }
    }



    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        if (!activePlayers.contains(player.getUniqueId())) return;

        double finalDamage = e.getFinalDamage();
        double healthAfter = player.getHealth() - finalDamage;

        if (healthAfter <= healthThreshold) {
            e.setCancelled(true);

            if (!player.isOnline() || player.isDead()) return;

            if (activePlayers.remove(player.getUniqueId())) {
                double maxHealth = player.getMaxHealth();
                player.setHealth(maxHealth);

                for (PotionEffect effect : effects) {
                    player.addPotionEffect(effect);
                }

                player.playSound(player.getLocation(), Sound.NOTE_BASS, 2.0f, 2.0f);
                player.sendMessage(getLanguageConfig().getString("ABILITIES.GUARDIAN_ANGEL.HEALED"));

                restorePlayerExp(player);
            }
        }
    }



    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRogueBackstab(RogueBackstabEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        if (!activePlayers.contains(player.getUniqueId())) return;

        double backstabDamage = e.getDamage();
        double healthAfter = player.getHealth() - backstabDamage;

        if (healthAfter <= healthThreshold) {

            e.setCancelled(true);

            if (!player.isOnline() || player.isDead()) return;

            if (activePlayers.remove(player.getUniqueId())) {
                double maxHealth = player.getMaxHealth();
                player.setHealth(maxHealth);

                for (PotionEffect effect : effects) {
                    player.addPotionEffect(effect);
                }

                player.playSound(player.getLocation(), Sound.NOTE_BASS, 2.0f, 2.0f);
                player.sendMessage(getLanguageConfig().getString("ABILITIES.GUARDIAN_ANGEL.HEALED"));

                restorePlayerExp(player);
            }
        }
    }
}