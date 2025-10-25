package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FreezerAbility extends Ability implements Listener {

    private final Map<UUID, Integer> hitCount;
    private final Set<UUID> frozenPlayers;
    private final Map<UUID, Long> victimCooldown;
    private final Map<UUID, BukkitRunnable> titleTasks;

    private final int hitsRequired;
    private final int freezeDuration;
    private final int abilityCooldown;
    private final int victimImmunityTime;
    private final boolean takeItem;

    public FreezerAbility(AbilityManager manager) {
        super(manager, AbilityUseType.HIT_PLAYER, "Freezer");

        this.hitCount = new HashMap<>();
        this.frozenPlayers = new HashSet<>();
        this.victimCooldown = new HashMap<>();
        this.titleTasks = new HashMap<>();


        this.hitsRequired = getAbilitiesConfig().getInt("FREEZER.HITS_REQUIRED", 3);
        this.freezeDuration = getAbilitiesConfig().getInt("FREEZER.DURATION", 10);
        this.abilityCooldown = getAbilitiesConfig().getInt("FREEZER.COOLDOWN", 30);
        this.victimImmunityTime = getAbilitiesConfig().getInt("FREEZER.VICTIM_IMMUNITY", 15);
        this.takeItem = getAbilitiesConfig().getBoolean("FREEZER.TAKE_ITEM", true);

        Bukkit.getPluginManager().registerEvents(this, getManager().getInstance());


        Bukkit.getScheduler().runTaskTimer(getManager().getInstance(), () -> {
            long now = System.currentTimeMillis();
            victimCooldown.entrySet().removeIf(entry -> entry.getValue() <= now);
        }, 20L, 20L);
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();
        UUID damagedUUID = damaged.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;


        if (frozenPlayers.contains(damagedUUID)) {
            String message = getLanguageConfig().getString("ABILITIES.FREEZER.ALREADY_FROZEN");
            if (message != null) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
            return;
        }


        if (victimCooldown.containsKey(damagedUUID)) {
            long endTime = victimCooldown.get(damagedUUID);
            long timeLeftMillis = endTime - System.currentTimeMillis();

            if (timeLeftMillis <= 0) {

                victimCooldown.remove(damagedUUID);
            } else {

                int timeLeft = (int) Math.max(1, timeLeftMillis / 1000);
                String message = getLanguageConfig().getString("ABILITIES.FREEZER.VICTIM_IMMUNE");
                if (message != null) {
                    damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            message.replace("%time%", "&c" + timeLeft)
                    ));
                }
                return;
            }
        }


        if (!hasFullDiamondArmor(damaged)) {
            String message = getLanguageConfig().getString("ABILITIES.FREEZER.NEEDS_DIAMOND");
            if (message != null) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
            return;
        }


        hitCount.putIfAbsent(damagerUUID, 0);
        int currentHits = hitCount.get(damagerUUID) + 1;
        hitCount.put(damagerUUID, currentHits);

        int remainingHits = hitsRequired - currentHits;
        if (remainingHits > 0) {
            String progress = getLanguageConfig().getString("ABILITIES.FREEZER.PROGRESS");
            if (progress != null) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        progress.replace("%hits%", "&c" + remainingHits)
                ));
            }
        }

        damager.playSound(damager.getLocation(), Sound.CLICK, 1.0f, 1.5f);


        if (currentHits >= hitsRequired) {
            hitCount.remove(damagerUUID);
            if (takeItem) takeItem(damager);
            applyCooldown(damager);
            freezePlayer(damaged, damager);
        }
    }

    private void freezePlayer(Player target, Player damager) {
        UUID uuid = target.getUniqueId();
        frozenPlayers.add(uuid);

        target.setAllowFlight(true);
        target.setFlying(false);
        target.setVelocity(target.getVelocity().multiply(0.001));

        sendFreezeTitle(target);


        if (titleTasks.containsKey(uuid)) {
            titleTasks.get(uuid).cancel();
        }


        BukkitRunnable titleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || !frozenPlayers.contains(uuid)) {
                    cancel();
                    titleTasks.remove(uuid);
                    return;
                }
                sendFreezeTitle(target);
            }
        };

        titleTask.runTaskTimer(getManager().getInstance(), 80L, 80L); // 4 segundos
        titleTasks.put(uuid, titleTask);

        target.playSound(target.getLocation(), Sound.GLASS, 1.0f, 1.5f);
        damager.playSound(damager.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.5f);


        for (String msg : getLanguageConfig().getStringList("ABILITIES.FREEZER.APPLIED")) {
            damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    msg.replace("%player%", "&c" + target.getName())
            ));
        }
        for (String msg : getLanguageConfig().getStringList("ABILITIES.FREEZER.AFFECTED")) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }


        Tasks.executeLater(getManager(), 20L * freezeDuration, () -> {
            if (target.isOnline() && frozenPlayers.contains(uuid)) {
                unfreezePlayer(target, damager);
            }
        });
    }

    private void sendFreezeTitle(Player player) {
        try {
            IChatBaseComponent title = IChatBaseComponent.ChatSerializer.a(
                    "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("ABILITIES.FREEZER.TITLE")) + "\"}"
            );
            PacketPlayOutTitle packetTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, title);
            PacketPlayOutTitle packetTimes = new PacketPlayOutTitle(10, 60, 20); // fade in, stay, fade out
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTimes);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unfreezePlayer(Player target, Player damager) {
        UUID uuid = target.getUniqueId();
        if (!frozenPlayers.contains(uuid)) return;

        frozenPlayers.remove(uuid);
        target.setAllowFlight(false);
        target.setFlying(false);


        if (titleTasks.containsKey(uuid)) {
            titleTasks.get(uuid).cancel();
            titleTasks.remove(uuid);
        }

        target.playSound(target.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 0.8f);


        for (String msg : getLanguageConfig().getStringList("ABILITIES.FREEZER.UNFROZEN")) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
        for (String msg : getLanguageConfig().getStringList("ABILITIES.FREEZER.RELEASED")) {
            damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    msg.replace("%player%", "&c" + target.getName())
            ));
        }


        long immunityEnd = System.currentTimeMillis() + (victimImmunityTime * 1000L);
        victimCooldown.put(uuid, immunityEnd);
    }

    private boolean hasFullDiamondArmor(Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chestplate = inv.getChestplate();
        ItemStack leggings = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        return isDiamond(helmet) && isDiamond(chestplate) && isDiamond(leggings) && isDiamond(boots);
    }

    private boolean isDiamond(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.DIAMOND_HELMET ||
                item.getType() == Material.DIAMOND_CHESTPLATE ||
                item.getType() == Material.DIAMOND_LEGGINGS ||
                item.getType() == Material.DIAMOND_BOOTS;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!frozenPlayers.contains(uuid)) return;

        if (event.getFrom().distanceSquared(event.getTo()) > 0.0001) {
            event.setTo(event.getFrom());
        }
        player.setVelocity(player.getVelocity().multiply(0.001));
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (frozenPlayers.contains(uuid)) frozenPlayers.remove(uuid);
        if (titleTasks.containsKey(uuid)) {
            titleTasks.get(uuid).cancel();
            titleTasks.remove(uuid);
        }
        if (victimCooldown.containsKey(uuid)) victimCooldown.remove(uuid);
    }
}