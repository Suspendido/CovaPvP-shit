package me.keano.azurite.modules.pvpclass.type.chemist;

import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChemistClass extends PvPClass {

    private CustomCooldown alchemiststaffCooldown;
    private CustomCooldown toxicdyeCooldown;
    private Material alchemiststaff;
    private Material toxicdye;

    public ChemistClass(PvPClassManager manager) {
        super(manager, "Chemist");
        this.load();
    }

    @Override
    public void load() {
        this.alchemiststaff = ItemUtils.getMat(getClassesConfig().getString("CHEMIST_CLASS.ALCHEMIST_STAFF_ITEM"));
        this.toxicdye = ItemUtils.getMat(getClassesConfig().getString("CHEMIST_CLASS.TOXIC_DYE_ITEM"));
        this.alchemiststaffCooldown = new CustomCooldown(this, getScoreboardConfig().getString("CHEMIST_CLASS.ALCHEMIST_STAFF_COOLDOWN"));
        this.toxicdyeCooldown = new CustomCooldown(this, getScoreboardConfig().getString("CHEMIST_CLASS.TOXIC_DYE_COOLDOWN"));
    }

    @Override
    public void handleEquip(Player player) {

    }

    @Override
    public void handleUnequip(Player player) {

    }

    @Override
    public void reload() {
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler
    public void onAlchemistStaff(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || hand.getType() != alchemiststaff) return;
        if (!players.contains(player.getUniqueId())) return;

        if (alchemiststaffCooldown.hasCooldown(player)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot use this ability for another " +
                    alchemiststaffCooldown.getRemaining(player) + " seconds.");
            return;
        }

        if (checkchemist(player, true)) return;

        alchemiststaffCooldown.applyCooldown(player, getClassesConfig().getInt("CHEMIST_CLASS.ALCHEMIST_STAFF_COOLDOWN"));
        ItemStack potionItem = new ItemStack(Material.POTION, 1, (short) 16428);
        ThrownPotion thrownPotion = player.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(potionItem);
    }

    @EventHandler
    public void onToxicItem(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || hand.getType() != toxicdye) return;
        if (!players.contains(player.getUniqueId())) return;

        if (toxicdyeCooldown.hasCooldown(player)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot use this ability for another " +
                    toxicdyeCooldown.getRemaining(player) + " seconds.");
            return;
        }
        if (checkchemist(player, true)) return;

        toxicdyeCooldown.applyCooldown(player, getClassesConfig().getInt("CHEMIST_CLASS.TOXIC_DYE_COOLDOWN"));
        ItemStack potionItem = new ItemStack(Material.POTION, 1, (short) 16388);
        ThrownPotion thrownPotion = player.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(potionItem);
    }


    public boolean checkchemist(Player player, boolean message) {
        if (getInstance().getTimerManager().getPvpTimer().hasTimer(player) || getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player) || getInstance().getSotwManager().isActive()) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_PVPTIMER"));
            }
            return true;

        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof SafezoneTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_SAFEZONE"));
            }
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof EventTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_EVENT"));
            }
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof CitadelTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_EVENT"));
            }
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof ConquestTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_EVENT"));
            }
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof DTCTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_EVENT"));
            }
            return true;
        }

        return false;
    }
}
