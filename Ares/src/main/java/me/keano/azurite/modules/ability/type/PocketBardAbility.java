package me.keano.azurite.modules.ability.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.type.bard.BardEffect;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PocketBardAbility extends Ability {

    private final Map<String, PocketBard> pocketBards;
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    public PocketBardAbility(AbilityManager manager) {
        super(manager, AbilityUseType.INTERACT, "PocketBard");
        this.pocketBards = new HashMap<>();
        this.load();
    }

    private void load() {
        for (String key : getAbilitiesConfig().getConfigurationSection("POCKETBARD.TYPES").getKeys(false)) {
            String path = "POCKETBARD.TYPES." + key + ".";
            ItemStack item = new ItemBuilder(ItemUtils.getMat(getAbilitiesConfig().getString(path + "MATERIAL")))
                    .setName(getAbilitiesConfig().getString(path + "NAME"))
                    .setLore(getAbilitiesConfig().getStringList(path + "LORE"))
                    .data(getManager(), getAbilitiesConfig().getInt(path + "DATA"))
                    .toItemStack();

            item.setAmount(getAbilitiesConfig().getInt(path + "AMOUNT"));
            pocketBards.put(key, new PocketBard(
                    key,
                    getInstance().getClassManager(),
                    Serializer.getEffect(getAbilitiesConfig().getString(path + "EFFECT")),
                    (getAbilitiesConfig().getBoolean(path + "ADD_GLOW") ?
                            getInstance().getVersionManager().getVersion().addGlow(item) :
                            item))
            );
        }

        // Menu items
        for (String key : getAbilitiesConfig().getConfigurationSection("POCKETBARD.POCKETBARD_MENU.ITEMS").getKeys(false)) {
            String path = "POCKETBARD.POCKETBARD_MENU.ITEMS." + key + ".";
            PocketBard pocketBard = pocketBards.get(getAbilitiesConfig().getString(path + "POCKET_BARD"));
            ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getAbilitiesConfig().getString(path + "MATERIAL")))
                    .setName(getAbilitiesConfig().getString(path + "NAME"))
                    .setLore(getAbilitiesConfig().getStringList(path + "LORE"))
                    .data(getManager(), getAbilitiesConfig().getInt(path + "DATA"));

            pocketBard.setMenuItem(builder.toItemStack());
            pocketBard.setSlot(getAbilitiesConfig().getInt(path + "SLOT"));
        }
    }

    @Override
    public void onClick(Player player) {
        new PocketBardMenu(getInstance().getMenuManager(), this, player).open();
    }

    @EventHandler
    public void onInteractPocket(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        PocketBard pocketBard = getPocketBardInHand(player);

        if (pocketBard != null) {
            String typeKey = pocketBard.getTypeKey();

            if (cannotUse(player)) return;

            if (hasCooldown(player)) return;

            if (hasCooldownType(player, typeKey)) {
                long remaining = getCooldownRemainingType(player, typeKey);

                String displayName = getAbilitiesConfig().getString("POCKETBARD.TYPES." + typeKey + ".NAME");
                if (displayName == null) {
                    displayName = typeKey;
                }

                String m = getLanguageConfig().getString("ABILITIES.POCKET_BARD.COOLDOWN_SINGLE");
                if (m != null) {
                    m = m.replace("%remaining%", String.valueOf(remaining))
                            .replace("%type%", displayName);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
                }
                return;
            }

            pocketBard.getEffect().applyEffect(player);
            takeItem(player);

            int cooldown = getCooldownForType(typeKey);
            applyCooldownType(player, typeKey, cooldown);

            applyCooldown(player);

            e.setCancelled(true);
        }
    }

    public boolean hasCooldownType(Player player, String typeKey) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return false;
        Long end = cooldowns.get(typeKey);
        return end != null && System.currentTimeMillis() < end;
    }

    public void applyCooldownType(Player player, String typeKey, int seconds) {
        playerCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(typeKey, System.currentTimeMillis() + seconds * 1000L);
    }

    public long getCooldownRemainingType(Player player, String typeKey) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return 0;
        Long end = cooldowns.get(typeKey);
        if (end == null) return 0;
        long diff = (end - System.currentTimeMillis()) / 1000L;
        return diff > 0 ? diff : 0;
    }

    public int getCooldownForType(String typeKey) {
        String path = "POCKETBARD.TYPES." + typeKey + ".COOLDOWN";
        if (getAbilitiesConfig().contains(path)) {
            return getAbilitiesConfig().getInt(path);
        }
        return getAbilitiesConfig().getInt("POCKETBARD.COOLDOWN", 60); // Default
    }


    public boolean isPocketBard(ItemStack item) {
        for (PocketBard pocketBard : pocketBards.values()) {
            if (!pocketBard.getItem().isSimilar(item)) continue;
            return true;
        }
        return false;
    }

    public PocketBard getPocketBardInHand(Player player) {
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null) return null;

        for (PocketBard pocketBard : pocketBards.values()) {
            if (!pocketBard.getItem().isSimilar(hand)) continue;
            return pocketBard;
        }
        return null;
    }

    @Getter
    @Setter
    public static class PocketBard {
        private final String typeKey;
        private BardEffect effect;
        private ItemStack item;
        private ItemStack menuItem;
        private int slot;

        public PocketBard(String typeKey, PvPClassManager manager, PotionEffect effect, ItemStack item) {
            this.typeKey = typeKey;
            this.effect = new BardEffect(manager, true, effect);
            this.item = item;
            this.menuItem = null;
            this.slot = 0;
        }
    }

    private static class PocketBardMenu extends Menu {

        private final PocketBardAbility ability;

        public PocketBardMenu(MenuManager manager, PocketBardAbility ability, Player player) {
            super(
                    manager,
                    player,
                    manager.getAbilitiesConfig().getString("POCKETBARD.POCKETBARD_MENU.TITLE"),
                    manager.getAbilitiesConfig().getInt("POCKETBARD.POCKETBARD_MENU.SIZE"),
                    false
            );
            this.ability = ability;
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            for (PocketBard pocketBard : ability.getPocketBards().values()) {
                buttons.put(pocketBard.getSlot(), new Button() {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        // Check first and then check after as well.
                        if (!ability.hasAbilityInHand(player)) {
                            e.setCancelled(true);
                            player.closeInventory();
                            return;
                        }

                        e.setCancelled(true);
                        getManager().takeItemInHand(player, 1);
                        ItemUtils.giveItem(player, pocketBard.getItem(), player.getLocation());
                        if (!ability.hasAbilityInHand(player)) player.closeInventory();
                    }

                    @Override
                    public ItemStack getItemStack() {
                        return pocketBard.getMenuItem();
                    }
                });
            }

            return buttons;
        }
    }
}
