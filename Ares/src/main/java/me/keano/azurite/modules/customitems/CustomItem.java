package me.keano.azurite.modules.customitems;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 21/01/2025
 */

@Getter
@Setter
public abstract class CustomItem implements Listener {

    private final String id;
    private final Material material;
    private String displayName;
    private List<String> lore;
    private final Map<Enchantment, Integer> enchantments;
    private final Map<String, String> nbtTags;

    public CustomItem(String id, Material material, CustomItemManager manager) {
        this.id = id;
        this.material = material;
        this.enchantments = new HashMap<>();
        this.nbtTags = new HashMap<>();

        manager.registerItem(this);}

    public String getName() {
        return displayName != null ? displayName : material.name();
    }

    public ItemStack getItem() {
        return build();
    }

    public CustomItem setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public CustomItem setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public CustomItem addEnchantment(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public CustomItem addNBTTag(String key, String value) {
        this.nbtTags.put(key, value);
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            throw new IllegalStateException("Unable to modify ItemMeta for material: " + material);
        }

        // Set display name
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        // Set lore
        if (lore != null) {
            meta.setLore(lore);
        }

        // Apply meta to item
        item.setItemMeta(meta);

        // Add enchantments
        ItemStack finalItem = item;
        enchantments.forEach((enchantment, level) -> finalItem.addUnsafeEnchantment(enchantment, level));

        // Add NBT tags
        if (!nbtTags.isEmpty()) {
            item = addNBTToItem(item);
        }

        return item;
    }

    private ItemStack addNBTToItem(ItemStack item) {
        try {
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
            net.minecraft.server.v1_8_R3.NBTTagCompound nbt = nmsItem.hasTag() ? nmsItem.getTag() : new net.minecraft.server.v1_8_R3.NBTTagCompound();

            for (Map.Entry<String, String> entry : nbtTags.entrySet()) {
                nbt.setString(entry.getKey(), entry.getValue());
            }

            nmsItem.setTag(nbt);
            return org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asBukkitCopy(nmsItem);

        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    public String getNBTValue(ItemStack item, String key) {
        try {
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
            if (!nmsItem.hasTag()) return null;

            net.minecraft.server.v1_8_R3.NBTTagCompound nbt = nmsItem.getTag();
            return nbt.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public abstract void onPlayerInteract(PlayerInteractEvent event);

    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String customItemTag = getNBTValue(item, "custom_item");
        return id.equals(customItemTag);
    }

    public abstract void onUse();
}
