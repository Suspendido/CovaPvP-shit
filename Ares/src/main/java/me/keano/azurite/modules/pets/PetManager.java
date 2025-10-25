package me.keano.azurite.modules.pets;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author: RodriDevs © 2025
 * Date: 8/28/2025
 * Project: Ares
 */

@Getter
public class PetManager extends Manager {

    private final Map<String, Pet> pets; // id -> pet (case-insensitive)
    private final Map<UUID, Pet> equipped; // currently equipped

    public PetManager(HCF instance) {
        super(instance);
        this.pets = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.equipped = new HashMap<>();
        this.loadFromConfig();
        this.registerListener(new PetManagerListener(this));
    }

    public void loadFromConfig() {
        pets.clear();

        // Ensure root exists with an example if absent
        if (!getConfig().contains("PETS")) {
            String path = "PETS.WOLF";
            getConfig().set(path + ".DISPLAY_NAME", "Wolf");
            getConfig().set(path + ".TEXTURE", "");
            getConfig().set(path + ".EFFECTS", Arrays.asList("SPEED:1", "REGENERATION:1"));
            getConfig().save();
            getConfig().reloadCache();
        }

        ConfigurationSection section = getConfig().getConfigurationSection("PETS");
        for (String id : section.getKeys(false)) {
            String p = "PETS." + id + ".";
            String name = getConfig().getString(p + "DISPLAY_NAME");
            String b64 = getConfig().getUntranslatedString(p + "TEXTURE"); // keep as-is
            List<String> effs = getConfig().getStringList(p + "EFFECTS");

            Map<PotionEffectType, Integer> map = new HashMap<>();
            for (String s : effs) {
                String[] split = s.split(":");
                if (split.length != 2) continue;
                PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
                Integer level = null;
                try { level = Integer.parseInt(split[1]); } catch (Exception ignored) { }
                if (type == null || level == null) continue;
                map.put(type, Math.max(0, level - 1));
            }

            pets.put(id, new Pet(id, name, b64, map));
        }
    }

    public boolean createPet(String id) {
        if (pets.containsKey(id)) return false;
        Pet pet = new Pet(id);
        pets.put(id, pet);

        String path = "PETS." + id + ".";
        getConfig().set(path + "DISPLAY_NAME", pet.getDisplayName());
        getConfig().set(path + "TEXTURE", "");
        getConfig().set(path + "EFFECTS", Collections.emptyList());
        getConfig().save();
        getConfig().reloadCache();
        return true;
    }

    public boolean deletePet(String id) {
        Pet removed = pets.remove(id);
        if (removed == null) return false;
        getConfig().set("PETS." + id, null);
        getConfig().save();
        getConfig().reloadCache();
        // Unequip for any players using it
        equipped.entrySet().removeIf(e -> {
            if (e.getValue().getId().equalsIgnoreCase(id)) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p != null) unequip(p, true);
                return true;
            }
            return false;
        });
        return true;
    }

    public void setDisplayName(String id, String name) {
        Pet pet = pets.get(id);
        if (pet == null) return;
        pet.setDisplayName(name);
        getConfig().set("PETS." + pet.getId() + ".DISPLAY_NAME", name);
        getConfig().save();
        getConfig().reloadCache();
    }

    public void setTexture(String id, String base64) {
        Pet pet = pets.get(id);
        if (pet == null) return;
        pet.setBase64Texture(base64);
        getConfig().set("PETS." + pet.getId() + ".TEXTURE", base64);
        getConfig().save();
        getConfig().reloadCache();
    }

    public void setEffects(String id, List<String> list) {
        Pet pet = pets.get(id);
        if (pet == null) return;
        Map<PotionEffectType, Integer> map = new HashMap<>();

        for (String s : list) {
            String[] split = s.split(":");
            if (split.length != 2) continue;
            PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
            Integer level = null;
            try { level = Integer.parseInt(split[1]); } catch (Exception ignored) { }
            if (type == null || level == null) continue;
            map.put(type, Math.max(0, level - 1));
        }
        pet.setEffects(map);
        getConfig().set("PETS." + pet.getId() + ".EFFECTS", list);
        getConfig().save();
        getConfig().reloadCache();
    }

    public Pet getPetFromItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null || !item.getItemMeta().hasLore()) return null;
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return null;
        for (String line : lore) {
            String stripped = CC.t(line);
            stripped = stripped.replace('§', '&');
            if (!stripped.toLowerCase().contains("pet id:")) continue;
            String id = stripped.substring(stripped.toLowerCase().indexOf("pet id:") + 8).trim();
            return pets.get(id);
        }
        return null;
    }

    public ItemStack getPetItem(String id) {
        Pet pet = pets.get(id);
        if (pet == null) return null;
        return pet.toItem();
    }

    public void equip(Player player, Pet pet, boolean fromInventoryRemoval) {
        // remove previous
        Pet old = equipped.put(player.getUniqueId(), pet);
        if (old != null) removeEffects(player, old);

        // apply
        applyEffects(player, pet);
        player.sendMessage(CC.t("&aEquipped pet &f" + pet.getDisplayName() + "&a."));
        getInstance().getMenuManager().playSound(player, "LEVEL_UP", false);
    }

    public void unequip(Player player, boolean giveBack) {
        Pet pet = equipped.remove(player.getUniqueId());
        if (pet == null) return;
        removeEffects(player, pet);
        if (giveBack) {
            ItemUtils.giveItem(player, pet.toItem(), player.getLocation());
        }
        player.sendMessage(CC.t("&cUnequipped pet &f" + pet.getDisplayName() + "&c."));
        getInstance().getMenuManager().playSound(player, "ITEM_BREAK", false);
    }

    private void applyEffects(Player player, Pet pet) {
        for (Map.Entry<PotionEffectType, Integer> entry : pet.getEffects().entrySet()) {
            PotionEffectType type = entry.getKey();
            int amplifier = entry.getValue();
            // Long duration, ambient=false, particles=false
            player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false), true);
        }
    }

    private void removeEffects(Player player, Pet pet) {
        for (PotionEffectType type : pet.getEffects().keySet()) {
            player.removePotionEffect(type);
        }
    }

    public static class PetManagerListener implements org.bukkit.event.Listener {
        private final PetManager manager;
        public PetManagerListener(PetManager manager) { this.manager = manager; }

        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            // Reapply effects if they had an equipped pet (in case of reload we don't persist, so noop)
            Pet pet = manager.getEquipped().get(e.getPlayer().getUniqueId());
            if (pet != null) manager.applyEffects(e.getPlayer(), pet);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            // Clear mapping; effects will wear off naturally on quit
            manager.getEquipped().remove(e.getPlayer().getUniqueId());
        }
    }
}

