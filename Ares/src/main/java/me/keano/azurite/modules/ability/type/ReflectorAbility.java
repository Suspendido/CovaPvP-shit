package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ReflectorAbility extends Ability implements Listener {

    private final Map<UUID, Integer> hitCount;
    private final Map<UUID, Player> reflectorTarget; // Enemigo objetivo
    private final Set<UUID> cannotRemoveArmor; // Jugadores que no pueden quitarse la armadura
    private final List<PotionEffectConfig> potionEffects;

    private final int hitsRequired;
    private final int reflectDuration;
    private final int cooldown;
    private final boolean takeItem;

    public ReflectorAbility(AbilityManager manager) {
        super(manager, AbilityUseType.HIT_PLAYER, "Reflector");
        this.hitCount = new HashMap<>();
        this.reflectorTarget = new HashMap<>();
        this.cannotRemoveArmor = new HashSet<>();
        this.potionEffects = new ArrayList<>();

        this.hitsRequired = getAbilitiesConfig().getInt("REFLECTOR.HITS_REQUIRED", 3);
        this.reflectDuration = getAbilitiesConfig().getInt("REFLECTOR.DURATION", 10);
        this.cooldown = getAbilitiesConfig().getInt("REFLECTOR.COOLDOWN", 45);
        this.takeItem = getAbilitiesConfig().getBoolean("REFLECTOR.TAKE_ITEM", true);

        List<String> effectList = getAbilitiesConfig().getStringList("REFLECTOR.EFFECTS");
        if (effectList != null) {
            for (String line : effectList) {
                PotionEffectConfig effect = PotionEffectConfig.fromConfig(line);
                if (effect != null) {
                    potionEffects.add(effect);
                }
            }
        }

        Bukkit.getPluginManager().registerEvents(this, getManager().getInstance());
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;

        UUID damagerUUID = damager.getUniqueId();

        if (reflectorTarget.containsKey(damagerUUID)) return;

        if (!hasFullDiamondArmor(damaged)) {
            String message = getLanguageConfig().getString("ABILITIES.REFLECTOR.ENEMY_NEEDS_FULL_DIAMOND");
            if (message != null && !message.isEmpty()) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
            return;
        }

        hitCount.putIfAbsent(damagerUUID, 0);
        int currentHits = hitCount.get(damagerUUID) + 1;
        hitCount.put(damagerUUID, currentHits);


        int remaining = hitsRequired - currentHits;
        if (remaining > 0) {
            String progressMessage = getLanguageConfig().getString("ABILITIES.REFLECTOR.PROGRESS");
            if (progressMessage != null) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        progressMessage.replace("%remaining%", String.valueOf(remaining))
                ));
            }
        }

        damager.playSound(damager.getLocation(), Sound.CLICK, 1.0f, 1.5f);

        if (currentHits >= hitsRequired) {
            hitCount.remove(damagerUUID);

            if (takeItem) {
                takeItem(damager);
            }
            applyCooldown(damager);

            activateReflector(damager, damaged);
        }
    }

    private void activateReflector(Player player, Player target) {
        UUID playerUUID = player.getUniqueId();

        reflectorTarget.put(playerUUID, target);
        cannotRemoveArmor.add(playerUUID);
        cannotRemoveArmor.add(target.getUniqueId());

        applyPotionEffects(player);

        player.playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 1.0f, 0.8f);
        target.playSound(target.getLocation(), Sound.ZOMBIE_METAL, 1.0f, 0.7f);

        for (String msg : getLanguageConfig().getStringList("ABILITIES.REFLECTOR.APPLIED")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%player%", target.getName())));
        }

        for (String msg : getLanguageConfig().getStringList("ABILITIES.REFLECTOR.AFFECTED")) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%player%", player.getName())));
        }

        Tasks.executeLater(getManager(), 20L * reflectDuration, () -> {
            if (player.isOnline() && reflectorTarget.containsKey(playerUUID)) {
                deactivateReflector(player);
            }
        });
    }

    private void applyPotionEffects(Player player) {
        for (PotionEffectConfig config : potionEffects) {
            PotionEffectType type = config.type;
            int duration = config.durationSeconds * 20;
            int amplifier = config.amplifier;

            if (type != null) {
                player.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false));
            }
        }
    }

    private void deactivateReflector(Player player) {
        UUID uuid = player.getUniqueId();
        if (!reflectorTarget.containsKey(uuid)) return;

        Player target = reflectorTarget.remove(uuid);

        cannotRemoveArmor.remove(uuid);
        if (target != null) {
            cannotRemoveArmor.remove(target.getUniqueId());
        }

        for (String msg : getLanguageConfig().getStringList("ABILITIES.REFLECTOR.REMOVED")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        if (target != null && target.isOnline()) {
            for (String msg : getLanguageConfig().getStringList("ABILITIES.REFLECTOR.TARGET_FREE")) {
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player damaged = (Player) event.getEntity();

        UUID damagedUUID = damaged.getUniqueId();
        if (!reflectorTarget.containsKey(damagedUUID)) return;

        Player target = reflectorTarget.get(damagedUUID);
        if (target == null || !target.isOnline()) return;

        if (!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) event;
        if (!(edbee.getDamager() instanceof Player)) return;

        Player attacker = (Player) edbee.getDamager();
        if (!attacker.getUniqueId().equals(target.getUniqueId())) return;

        double finalDamage = event.getFinalDamage();
        double newHealth = attacker.getHealth() - finalDamage;

        if (newHealth <= 0) {
            attacker.setHealth(0.0);
            attacker.setLastDamageCause(event);
            attacker.damage(1.0);
        } else {
            attacker.setHealth(Math.max(0.0, newHealth));
        }

        attacker.playSound(attacker.getLocation(), Sound.ZOMBIE_METAL, 1.0f, 0.7f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!cannotRemoveArmor.contains(player.getUniqueId())) return;
        if (isArmorSlot(event.getSlot())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (!cannotRemoveArmor.contains(player.getUniqueId())) return;
        if (isArmorPiece(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (reflectorTarget.containsKey(uuid)) {
            reflectorTarget.remove(uuid);
        }
        if (cannotRemoveArmor.contains(uuid)) {
            cannotRemoveArmor.remove(uuid);
        }
        if (hitCount.containsKey(uuid)) {
            hitCount.remove(uuid);
        }
    }

    private boolean hasFullDiamondArmor(Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chestplate = inv.getChestplate();
        ItemStack leggings = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        return isDiamond(helmet) && isDiamond(chestplate) && isDiamond(leggings) && isDiamond(boots);
    }

    private boolean isDiamond(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.DIAMOND_HELMET ||
                item.getType() == Material.DIAMOND_CHESTPLATE ||
                item.getType() == Material.DIAMOND_LEGGINGS ||
                item.getType() == Material.DIAMOND_BOOTS;
    }

    private boolean isArmorPiece(ItemStack item) {
        Material type = item.getType();
        return type == Material.DIAMOND_HELMET ||
                type == Material.DIAMOND_CHESTPLATE ||
                type == Material.DIAMOND_LEGGINGS ||
                type == Material.DIAMOND_BOOTS;
    }

    private boolean isArmorSlot(int slot) {
        return slot == 36 || slot == 37 || slot == 38 || slot == 39;
    }

    private static class PotionEffectConfig {
        final PotionEffectType type;
        final int durationSeconds;
        final int amplifier;

        PotionEffectConfig(PotionEffectType type, int durationSeconds, int level) {
            this.type = type;
            this.durationSeconds = durationSeconds;
            this.amplifier = level - 1;
        }

        static PotionEffectConfig fromConfig(String line) {
            try {
                String[] parts = line.split(",");
                if (parts.length != 3) return null;

                String effectName = parts[0].trim().toUpperCase();
                String secondsStr = parts[1].trim();
                int level = Integer.parseInt(parts[2].trim());

                int seconds = Integer.parseInt(secondsStr.replaceAll("[^0-9]", ""));
                PotionEffectType type = PotionEffectType.getByName(effectName);

                if (type == null) {
                    Bukkit.getLogger().warning("[Ares] Efecto desconocido: " + effectName);
                    return null;
                }

                return new PotionEffectConfig(type, seconds, level);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[Ares] Error al parsear EFFECTS: " + line);
                return null;
            }
        }
    }
}