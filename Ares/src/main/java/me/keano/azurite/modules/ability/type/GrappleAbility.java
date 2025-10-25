package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GrappleAbility extends Ability {

    private static final Field HOOK_ENTITY = ReflectionUtils.accessField(org.bukkit.event.player.PlayerFishEvent.class, "hookEntity");

    private final double multiplier;
    private final double yMultiplier;
    private final short durabilityLoss;

    private final Set<Player> noFallDamage = new HashSet<>();

    public GrappleAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Grapple"
        );
        this.multiplier = getAbilitiesConfig().getDouble("GRAPPLE.MULTIPLIER");
        this.yMultiplier = getAbilitiesConfig().getDouble("GRAPPLE.Y_MULTIPLIER");
        this.durabilityLoss = (short) getAbilitiesConfig().getInt("GRAPPLE.DURABILITY_LOSS", 6);
    }

    @EventHandler
    public void onLaunch(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();

        if (hasAbilityInHand(player)) {
            if (cannotUse(player) || hasCooldown(player)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();

        if (e.getState() == PlayerFishEvent.State.FISHING) return;
        if (!hasAbilityInHand(player)) return;

        try {
            Entity hookEntity = Utils.isModernVer() ? (FishHook) HOOK_ENTITY.get(e) : e.getHook();

            if (hookEntity.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
                return;
            }


            player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1.0f, 1.0f);


            onlyabillityCooldown(player);
            takeItem(player);


            pullEntityToLocation(player, hookEntity);


            noFallDamage.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    noFallDamage.remove(player);
                }
            }.runTaskLater(getInstance(), 21); // ~1 segundo


            ItemStack rod = player.getItemInHand();
            if (rod != null && rod.getType() == Material.FISHING_ROD) {
                short newDurability = (short) (rod.getDurability() + durabilityLoss);
                if (newDurability >= rod.getType().getMaxDurability()) {
                    player.setItemInHand(null);
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    rod.setDurability(newDurability);
                }
            }


            hookEntity.remove();


            e.setExpToDrop(0);


            List<String> messages = getLanguageConfig().getStringList("ABILITIES.GRAPPLE.USED");
            if (messages != null && !messages.isEmpty()) {
                for (String msg : messages) {
                    player.sendMessage(msg);
                }
            }

        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Error accessing hookEntity field", ex);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL && noFallDamage.contains(p)) {
                e.setCancelled(true);
            }
        }
    }

    private void pullEntityToLocation(Entity entity, Entity loc) {
        Location locPos = loc.getLocation().subtract(0, 1, 0);
        if (locPos.getBlock().getType() == Material.AIR) return;

        Vector grapple = locPos.toVector().subtract(entity.getLocation().toVector());
        grapple.multiply(multiplier);
        grapple.setY(grapple.getY() + yMultiplier);
        entity.setVelocity(grapple);
    }
}