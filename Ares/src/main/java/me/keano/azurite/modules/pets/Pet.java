package me.keano.azurite.modules.pets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
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
@Setter
@AllArgsConstructor
public class Pet {

    private String id;
    private String displayName;
    private String base64Texture;
    private Map<PotionEffectType, Integer> effects; // amplifier levels (0-based)

    public Pet(String id) {
        this.id = id;
        this.displayName = id;
        this.base64Texture = null;
        this.effects = new HashMap<>(PotionEffectType.class.getModifiers());
    }

    public ItemStack toItem() {
        ItemStack head = (base64Texture != null && !base64Texture.isEmpty())
                ? ItemUtils.getCustomHead(base64Texture)
                : ItemUtils.getMatItem("SKULL_ITEM:3");

        List<String> lore = new ArrayList<>();
        lore.add("&7Place in the middle slot to equip");
        lore.add("&7Pet ID: &f" + id);
        if (!effects.isEmpty()) {
            lore.add(" ");
            lore.add("&ePerks:");
            for (Map.Entry<PotionEffectType, Integer> entry : effects.entrySet()) {
                String name = entry.getKey().getName().toLowerCase().replace('_', ' ');
                int level = entry.getValue() + 1; // human readable
                lore.add(" &7- &f" + capitalize(name) + " &7Lv." + level);
            }
        }

        return new ItemBuilder(head)
                .setName("&6" + displayName)
                .setLore(lore)
                .toItemStack();
    }

    private String capitalize(String s) {
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}

