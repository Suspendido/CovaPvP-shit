package me.keano.azurite.modules.signs.kitmap.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RefillMenu extends Menu {

    public RefillMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.MENU_TITLE"),
                manager.getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.MENU_SIZE"),
                false
        );
        this.setAllowInteract(true);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // --- Construir items list desde la config (SIGNS_CONFIG.REFILL_SIGN.ITEMS) ---
        List<ItemStack> items = new ArrayList<>();
        for (String entry : getConfig().getStringList("SIGNS_CONFIG.REFILL_SIGN.ITEMS")) {
            String[] parts = entry.split(":");
            Material material = Material.matchMaterial(parts[0]);
            if (material == null) continue;
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            items.add(new ItemStack(material, amount));
        }

        // Mantener comportamiento anterior: slot = i + 1 (puedes adaptar si deseas 0-based)
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i).clone();
            int slot = i + 1;
            buttons.put(slot, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    // comportamiento por defecto: no hacer nada
                }

                @Override
                public ItemStack getItemStack() {
                    return item.clone();
                }
            });
        }

        // --- Leer configuración del botón REFILL ---
        String refillMaterialStr = getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.REFILL.MATERIAL", "GLASS_BOTTLE");
        int refillSlot = getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.REFILL.SLOT", 1); // 0-based recomendado
        String refillDisplayName = getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.REFILL.NAME", null);

        String fillMaterialStr = getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.FILL_ITEM.MATERIAL", "POTION");
        int fillAmount = getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.FILL_ITEM.AMOUNT", 1);
        String fillPotionTypeStr = getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.FILL_ITEM.POTION_TYPE", "INSTANT_HEAL");
        int fillPotionLevel = getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.FILL_ITEM.POTION_LEVEL", 2);
        boolean fillSplash = getConfig().getBoolean("SIGNS_CONFIG.REFILL_SIGN.FILL_ITEM.SPLASH", true);

        // Crear item visual del botón REFILL
        Material refillMaterial = Material.matchMaterial(refillMaterialStr);
        ItemStack refillDisplay;
        if (refillMaterial != null) {
            refillDisplay = new ItemStack(refillMaterial, 1);
        } else {
            refillDisplay = new ItemStack(Material.GLASS_BOTTLE, 1);
        }

        if (refillDisplayName != null && !refillDisplayName.isEmpty()) {
            ItemMeta meta = refillDisplay.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(refillDisplayName);
                refillDisplay.setItemMeta(meta);
            }
        }

        // Pre-construir el ItemStack que rellenará el inventario (para mostrar en el menú)
        ItemStack fillItemExample;
        if ("POTION".equalsIgnoreCase(fillMaterialStr)) {
            PotionType pType;
            try {
                pType = PotionType.valueOf(fillPotionTypeStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                pType = PotionType.INSTANT_HEAL;
            }
            Potion potion = new Potion(pType, Math.max(1, fillPotionLevel));
            potion.setSplash(fillSplash); // <-- configurable: splash o normal
            fillItemExample = potion.toItemStack(1);
        } else {
            Material fillMaterial = Material.matchMaterial(fillMaterialStr);
            if (fillMaterial == null) fillMaterial = Material.POTION;
            fillItemExample = new ItemStack(fillMaterial, Math.max(1, fillAmount));
        }

        // Normalizar slot: asegurarse que está dentro del tamaño del menú
        int menuSize = getSize();
        if (refillSlot < 0) refillSlot = 0;
        if (refillSlot >= menuSize) refillSlot = Math.max(0, menuSize - 1);

        ItemStack finalRefillDisplay = refillDisplay.clone();

        // Añadir el botón REFILL (sobrescribe si existe otro en ese slot)
        buttons.put(refillSlot, new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true); // prevenir movimiento por defecto
                Player p = (Player) e.getWhoClicked();
                PlayerInventory inv = p.getInventory();

                ItemStack itemToPlacePrototype;

                if ("POTION".equalsIgnoreCase(fillMaterialStr)) {
                    PotionType pType;
                    try {
                        pType = PotionType.valueOf(fillPotionTypeStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        pType = PotionType.INSTANT_HEAL;
                    }
                    Potion potion = new Potion(pType, Math.max(1, fillPotionLevel));
                    potion.setSplash(fillSplash); // <-- aplicación runtime del flag splash
                    itemToPlacePrototype = potion.toItemStack(1);
                } else {
                    Material fillMaterial = Material.matchMaterial(fillMaterialStr);
                    if (fillMaterial == null) fillMaterial = Material.POTION;
                    itemToPlacePrototype = new ItemStack(fillMaterial, Math.max(1, fillAmount));
                }

                boolean filledAny = false;
                for (int slot = 0; slot < inv.getSize(); slot++) {
                    ItemStack current = inv.getItem(slot);
                    if (current == null || current.getType() == Material.AIR) {
                        inv.setItem(slot, itemToPlacePrototype.clone());
                        filledAny = true;
                    }
                }

                if (filledAny) {
                    p.sendMessage("§aInventario rellenado con el item configurado.");
                } else {
                    p.sendMessage("§cNo hay huecos vacíos en tu inventario.");
                }
            }

            @Override
            public ItemStack getItemStack() {
                return finalRefillDisplay.clone();
            }
        });

        return buttons;
    }
}
