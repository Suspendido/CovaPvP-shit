package me.keano.azurite.modules.users.extra;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.utils.BukkitSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class StoredInventory {

    private ItemStack[] contents;
    private ItemStack[] armor;
    private Date date;
    private Collection<PotionEffect> effects;
    private double health;

    public StoredInventory(ItemStack[] contents, ItemStack[] armor, Date date) {
        this(contents, armor, date, new ArrayList<>(), 20D);
    }

    public StoredInventory(ItemStack[] contents, ItemStack[] armor, Date date, Collection<PotionEffect> effects, double health) {
        this.contents = contents;
        this.armor = armor;
        this.date = date;
        this.effects = new ArrayList<>(effects);
        this.health = health;
    }

    public String serialize() {
        List<String> serializedEffects = new ArrayList<>();
        for (PotionEffect effect : effects) {
            serializedEffects.add(effect.getType().getName() + ":" + effect.getDuration() + ":" + effect.getAmplifier());
        }

        return String.join(", ",
                BukkitSerialization.itemStackArrayToBase64(contents),
                BukkitSerialization.itemStackArrayToBase64(armor),
                String.valueOf(date.getTime()),
                String.join("|", serializedEffects),
                String.valueOf(health)
        );
    }

    public static StoredInventory fromString(String string) {
        String[] split = string.split(", ");
        ItemStack[] contents = BukkitSerialization.itemStackArrayFromBase64(split[0]);
        ItemStack[] armor = BukkitSerialization.itemStackArrayFromBase64(split[1]);
        Date date = new Date(Long.parseLong(split[2]));

        Collection<PotionEffect> effects = new ArrayList<>();
        double health = 20D;

        if (split.length >= 4 && !split[3].isEmpty()) {
            for (String eff : split[3].split("\\|")) {
                if (eff.isEmpty()) continue;
                String[] eSplit = eff.split(":");
                PotionEffectType type = PotionEffectType.getByName(eSplit[0]);
                if (type == null) continue;
                int duration = Integer.parseInt(eSplit[1]);
                int amplifier = Integer.parseInt(eSplit[2]);
                effects.add(new PotionEffect(type, duration, amplifier));
            }
        }

        if (split.length >= 5) {
            try {
                health = Double.parseDouble(split[4]);
            } catch (NumberFormatException ignored) {
                health = 20D;
            }
        }

        return new StoredInventory(contents, armor, date, effects, health);
    }
}