package me.keano.azurite.modules.pvpclass.type.fisherman;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FishermanClass extends PvPClass {

    private final List<PotionEffect> soupeffects;
    private CustomCooldown rodCooldown;
    private CustomCooldown soupCooldown;
    private Material soup;
    private Material rod;

    public FishermanClass(PvPClassManager manager) {
        super(manager, "Fisherman");
        this.soupeffects = new ArrayList<>();
        this.load();
    }

    @Override
    public void load() {
        this.soupeffects.addAll(getClassesConfig().getStringList("FISHERMAN_CLASS.SOUP_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList()));

        this.soup = ItemUtils.getMat(getClassesConfig().getString("FISHERMAN_CLASS.SOUP_ITEM"));
        this.rod = ItemUtils.getMat(getClassesConfig().getString("FISHERMAN_CLASS.ROD_ITEM"));
        this.soupCooldown = new CustomCooldown(this, getScoreboardConfig().getString("FISHERMAN_CLASS.SOUP_COOLDOWN"));
        this.rodCooldown = new CustomCooldown(this, getScoreboardConfig().getString("FISHERMAN_CLASS.ROD_COOLDOWN"));

    }

    @Override
    public void handleEquip(Player player) {

    }

    @Override
    public void handleUnequip(Player player) {

    }
    @Override
    public void reload() {
        soupeffects.clear();
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || hand.getType() != rod) return;
        if (!players.contains(player.getUniqueId())) return;

        if (e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        if (!(e.getCaught() instanceof Player)) return;

        if (rodCooldown.hasCooldown(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this ability for another " +
                    rodCooldown.getRemaining(player) + " seconds.");
            e.setCancelled(true);
            return;
        }
        if (checkfisherman(player, true)) return;

        Player target = (Player) e.getCaught();

        rodCooldown.applyCooldown(player, getClassesConfig().getInt("FISHERMAN_CLASS.ROD_COOLDOWN"));
        target.teleport(player.getLocation());
    }

    @EventHandler
    public void onSoup(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || hand.getType() != soup) return;
        if (!players.contains(player.getUniqueId())) return;

        if (soupCooldown.hasCooldown(player)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot use this ability for another " +
                    soupCooldown.getRemaining(player) + " seconds.");
            return;
        }
        if (checkfisherman(player, true)) return;

        soupCooldown.applyCooldown(player, getClassesConfig().getInt("FISHERMAN_CLASS.SOUP_COOLDOWN"));

        for (PotionEffect soupeffects : this.soupeffects) {
            player.addPotionEffect(soupeffects);
        }
    }

    public boolean checkfisherman(Player player, boolean message) {
        if (getInstance().getTimerManager().getPvpTimer().hasTimer(player) || getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player) ||getInstance().getSotwManager().isActive()) {
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
