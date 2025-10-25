package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArmorDecayAbility extends Ability implements Listener {

    private final Map<UUID, Integer> hits;
    private final Map<UUID, ItemStack[]> originalArmor;
    private final Set<UUID> affectedPlayers;
    private final int maxHits;
    private final int duration;

    public ArmorDecayAbility(AbilityManager manager) {
        super(manager, AbilityUseType.HIT_PLAYER, "Armor Decay");
        this.hits = new HashMap<>();
        this.originalArmor = new HashMap<>();
        this.affectedPlayers = new HashSet<>();
        this.maxHits = getAbilitiesConfig().getInt("ARMOR_DECAY.HITS_REQUIRED");
        this.duration = getAbilitiesConfig().getInt("ARMOR_DECAY.DURATION");

        Bukkit.getPluginManager().registerEvents(this, getManager().getInstance());
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;
        if (affectedPlayers.contains(damaged.getUniqueId())) return;

        ItemStack[] armor = damaged.getInventory().getArmorContents();

        if (!isFullDiamond(armor)) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.ARMOR_DECAY.NOT_FULL_DIAMOND"));
            return;
        }

        boolean hasAboveOne = Arrays.stream(armor)
                .filter(Objects::nonNull)
                .anyMatch(a -> a.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL) > 1);

        if (!hasAboveOne) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.ARMOR_DECAY.NO_ARMOR_ABOVE_ONE"));
            return;
        }

        hits.putIfAbsent(damagerUUID, 0);
        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);


        int remaining = maxHits - current;
        if (remaining > 0) {
            String progressMessage = getLanguageConfig().getString("ABILITIES.ARMOR_DECAY.PROGRESS");
            if (progressMessage != null) {
                damager.sendMessage(progressMessage.replace("%remaining%", String.valueOf(remaining)));
            }
        }

        damager.playSound(damager.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);

        if (current >= maxHits) {
            hits.remove(damagerUUID);

            applyCooldown(damager);
            takeItem(damager);

            originalArmor.put(damaged.getUniqueId(), armor.clone());

            ItemStack[] tempArmor = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                if (armor[i] != null) {
                    ItemStack clone = armor[i].clone();
                    clone.removeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL);
                    clone.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                    tempArmor[i] = clone;
                }
            }

            damaged.getInventory().setArmorContents(tempArmor);
            affectedPlayers.add(damaged.getUniqueId());

            // Mensajes al activar
            for (String s : getLanguageConfig().getStringList("ABILITIES.ARMOR_DECAY.APPLIED")) {
                damager.sendMessage(s.replace("%player%", damaged.getName()));
            }
            for (String s : getLanguageConfig().getStringList("ABILITIES.ARMOR_DECAY.AFFECTED")) {
                damaged.sendMessage(s.replace("%player%", damager.getName()));
            }

            damaged.playSound(damaged.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
            damager.playSound(damager.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);

            Tasks.executeLater(getManager(), 20L * duration, () -> restoreArmor(damaged, damager));
        }
    }

    private boolean isFullDiamond(ItemStack[] armor) {
        return armor.length == 4
                && armor[0] != null && armor[0].getType() == Material.DIAMOND_BOOTS
                && armor[1] != null && armor[1].getType() == Material.DIAMOND_LEGGINGS
                && armor[2] != null && armor[2].getType() == Material.DIAMOND_CHESTPLATE
                && armor[3] != null && armor[3].getType() == Material.DIAMOND_HELMET;
    }

    private void restoreArmor(Player damaged, Player damager) {
        if (!damaged.isOnline()) return;

        ItemStack[] original = originalArmor.remove(damaged.getUniqueId());
        if (original != null) {
            damaged.getInventory().setArmorContents(original);
        }

        affectedPlayers.remove(damaged.getUniqueId());


        for (String s : Collections.singletonList(getLanguageConfig().getString("ABILITIES.ARMOR_DECAY.RETURNED"))) {
            damaged.sendMessage(s);
        }


        String endedMessage = getLanguageConfig().getString("ABILITIES.ARMOR_DECAY.EFFECT_ENDED");
        if (endedMessage != null && damager.isOnline()) {
            damager.sendMessage(endedMessage.replace("%player%", damaged.getName()));
        }


        damaged.playSound(damaged.getLocation(), Sound.VILLAGER_YES, 1.0f, 1.0f);
        if (damager.isOnline()) {
            damager.playSound(damager.getLocation(), Sound.VILLAGER_YES, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!affectedPlayers.contains(player.getUniqueId())) return;

        int slot = event.getSlot();
        if (slot >= 36 && slot <= 39) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!affectedPlayers.contains(player.getUniqueId())) return;
        if (event.getItem() != null && event.getItem().getType().name().contains("_CHESTPLATE")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!affectedPlayers.contains(player.getUniqueId())) return;
        ItemStack[] tempArmor = originalArmor.get(player.getUniqueId());
        if (tempArmor != null) {
            for (ItemStack piece : tempArmor) {
                if (piece != null && event.getItemDrop().getItemStack().isSimilar(piece)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (affectedPlayers.contains(player.getUniqueId())) {
            ItemStack[] original = originalArmor.remove(player.getUniqueId());
            if (original != null) player.getInventory().setArmorContents(original);
            affectedPlayers.remove(player.getUniqueId());
        }
    }
}