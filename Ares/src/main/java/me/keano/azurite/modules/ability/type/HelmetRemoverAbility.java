package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HelmetRemoverAbility extends Ability implements Listener {

    private final Map<UUID, Integer> hits;
    private final Map<UUID, ItemStack> originalHelmets;
    private final Set<UUID> affectedPlayers;
    private final int maxHits;
    private final int duration;
    private final ItemStack temporaryHelmet;

    public HelmetRemoverAbility(AbilityManager manager) {
        super(manager, AbilityUseType.HIT_PLAYER, "Helmet Remover");
        this.hits = new HashMap<>();
        this.originalHelmets = new HashMap<>();
        this.affectedPlayers = new HashSet<>();
        this.maxHits = getAbilitiesConfig().getInt("HELMET_REMOVER.HITS_REQUIRED");
        this.duration = getAbilitiesConfig().getInt("HELMET_REMOVER.DURATION");
        this.temporaryHelmet = getAbilitiesConfig().getItemStack("HELMET_REMOVER.TEMP_HELMET");

        Bukkit.getPluginManager().registerEvents(this, getManager().getInstance());
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;

        ItemStack damagedHelmet = damaged.getInventory().getHelmet();
        if (damagedHelmet == null || !damagedHelmet.getType().equals(Material.DIAMOND_HELMET)) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.HELMET_REMOVER.NO_DIAMOND_HELMET"));
            return;
        }

        hits.putIfAbsent(damagerUUID, 0);
        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);


        int remainingHits = maxHits - current;
        if (remainingHits > 0) {
            String progress = getLanguageConfig().getString("ABILITIES.HELMET_REMOVER.PROGRESS");
            if (progress != null) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        progress.replace("%hits%", "&c" + remainingHits)
                ));
            }
        }

        damager.playSound(damager.getLocation(), Sound.GLASS, 20, 20);

        if (current == maxHits) {
            hits.remove(damagerUUID);

            takeItem(damager);
            applyCooldown(damager);


            originalHelmets.put(damaged.getUniqueId(), damagedHelmet.clone());


            damaged.getInventory().setHelmet(temporaryHelmet);
            affectedPlayers.add(damaged.getUniqueId());


            damaged.setFireTicks(duration * 20);


            startFlameParticles(damaged);

            damaged.playSound(damaged.getLocation(), Sound.ANVIL_LAND, 20, 20);
            damager.playSound(damager.getLocation(), Sound.SUCCESSFUL_HIT, 20, 20);

            for (String s : getLanguageConfig().getStringList("ABILITIES.HELMET_REMOVER.APPLIED")) {
                damager.sendMessage(s.replace("%player%", damaged.getName()));
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.HELMET_REMOVER.AFFECTED")) {
                damaged.sendMessage(s.replace("%player%", damager.getName()));
            }


            Tasks.executeLater(getManager(), 20L * duration, () -> restoreHelmet(damaged, damager));
        }
    }

    private void restoreHelmet(Player damaged, Player damager) {
        if (!damaged.isOnline()) return;

        ItemStack original = originalHelmets.remove(damaged.getUniqueId());
        if (original != null) {
            damaged.getInventory().setHelmet(original);
        }

        damaged.setFireTicks(0);
        affectedPlayers.remove(damaged.getUniqueId());

        damaged.playSound(damaged.getLocation(), Sound.VILLAGER_YES, 20, 20);
        damager.playSound(damager.getLocation(), Sound.VILLAGER_YES, 20, 20);

        damaged.sendMessage(getLanguageConfig().getString("ABILITIES.HELMET_REMOVER.RETURNED"));
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!affectedPlayers.contains(player.getUniqueId())) return;

        if (event.getSlot() == 39) { // Slot del casco
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!affectedPlayers.contains(player.getUniqueId())) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType().name().endsWith("_HELMET")) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!affectedPlayers.contains(player.getUniqueId())) return;

        if (event.getItemDrop().getItemStack().isSimilar(temporaryHelmet)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (affectedPlayers.contains(player.getUniqueId())) {
            ItemStack original = originalHelmets.remove(player.getUniqueId());
            if (original != null) {
                player.getInventory().setHelmet(original);
            }
            affectedPlayers.remove(player.getUniqueId());
        }
    }


    private void startFlameParticles(Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!affectedPlayers.contains(target.getUniqueId()) || !target.isOnline()) {
                    cancel();
                    return;
                }

                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                        EnumParticle.FLAME, true,
                        (float) target.getLocation().getX(),
                        (float) target.getLocation().getY(),
                        (float) target.getLocation().getZ(),
                        0.3f, 0.5f, 0.3f,
                        0.01f, 10
                );

                for (Player p : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }.runTaskTimer(getManager().getInstance(), 0L, 2L);
    }
}