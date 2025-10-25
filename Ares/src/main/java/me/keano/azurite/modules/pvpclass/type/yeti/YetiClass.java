package me.keano.azurite.modules.pvpclass.type.yeti;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import me.keano.azurite.HCF;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class YetiClass extends PvPClass {

    private final List<PotionEffect> iceEffects;
    private final List<PotionEffect> snowballEffects;
    private final List<PotionEffect> targetEffects;
    private final HCF hcf;
    private Material iceItem;
    private Material snowball;
    private CustomCooldown iceCooldown;
    private CustomCooldown snowballCooldown;
    private final Map<UUID, ItemStack> helmetBackup = new HashMap<>();
    private final Map<Location, Material> iceReplacements = new HashMap<>();

    public YetiClass(PvPClassManager manager, HCF hcf) {
        super(manager, "Yeti");
        this.hcf = hcf;
        this.iceEffects = new ArrayList<>();
        this.snowballEffects = new ArrayList<>();
        this.targetEffects = new ArrayList<>();
        this.load();
    }

    @Override
    public void load() {
        this.iceEffects.addAll(getClassesConfig().getStringList("YETI_CLASS.ICE_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList()));
        this.snowballEffects.addAll(getClassesConfig().getStringList("YETI_CLASS.SNOWBALL_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList()));
        this.targetEffects.addAll(getClassesConfig().getStringList("YETI_CLASS.TARGET_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList()));
        this.iceCooldown = new CustomCooldown(this, getScoreboardConfig().getString("YETI_CLASS.ICE_COOLDOWN"));
        this.snowballCooldown = new CustomCooldown(this, getScoreboardConfig().getString("YETI_CLASS.SNOWBALL_COOLDOWN"));
        this.iceItem = ItemUtils.getMat(getClassesConfig().getString("YETI_CLASS.ICE_ITEM"));
        this.snowball = ItemUtils.getMat(getClassesConfig().getString("YETI_CLASS.SNOWBALL_ITEM"));
    }

    @Override
    public void handleEquip(Player player) {
        players.add(player.getUniqueId());
    }

    @Override
    public void handleUnequip(Player player) {
        players.remove(player.getUniqueId());
    }

    @Override
    public void reload() {
        iceEffects.clear();
        snowballEffects.clear();
        targetEffects.clear();
        this.load();
    }

    @EventHandler
    public void onIceAbility(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || hand.getType() != iceItem) return;
        if (!players.contains(player.getUniqueId())) return;

        if (iceCooldown.hasCooldown(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this ability for another " +
                    iceCooldown.getRemaining(player) + " seconds.");
            e.setCancelled(true);
            return;
        }

        if (checkyeti(player, true)) {
            e.setCancelled(true);
            return;
        }

        iceCooldown.applyCooldown(player, getClassesConfig().getInt("YETI_CLASS.ICE_COOLDOWN"));
        createHollowIceSphere(player.getLocation().add(0, -1, 0));
        for (PotionEffect effect : this.iceEffects) {
            player.addPotionEffect(effect);
        }

        player.playSound(player.getLocation(), "step.snow", 1.0f, 0.8f);
    }

    @EventHandler
    public void onThrowSnowball(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack hand = player.getItemInHand();

        if (!players.contains(player.getUniqueId())) return;
        if (hand == null || hand.getType() != snowball) return;
        if (!e.getAction().name().contains("RIGHT")) return;

        if (snowballCooldown.hasCooldown(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this ability for another " +
                    snowballCooldown.getRemaining(player) + " seconds.");
            e.setCancelled(true);
            return;
        }

        if (checkyeti(player, true)) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        snowballCooldown.applyCooldown(player, getClassesConfig().getInt("YETI_CLASS.SNOWBALL_COOLDOWN"));

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInHand(null);
        }
        player.updateInventory();

        Snowball snowballEntity = player.launchProjectile(Snowball.class);
        snowballEntity.setMetadata("YetiSnowball", new FixedMetadataValue(hcf, true));

        player.playSound(player.getLocation(), "random.bow", 0.5f, 1.0f);
    }

    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Snowball)) return;

        Snowball snowballEntity = (Snowball) e.getDamager();
        if (!snowballEntity.hasMetadata("YetiSnowball")) return;

        if (snowballEntity.getShooter() instanceof Player && e.getEntity() instanceof Player) {
            Player shooter = (Player) snowballEntity.getShooter();
            Player target = (Player) e.getEntity();

            if (hasDiamondHelmet(target)) {
                ItemStack targetHelmet = target.getInventory().getHelmet();

                helmetBackup.put(target.getUniqueId(), targetHelmet);
                target.getInventory().setHelmet(new ItemStack(Material.ICE));

                shooter.sendMessage(CC.t("&aYou removed the helmet of " + target.getDisplayName()));
                target.sendMessage(CC.t("&cYour helmet has been removed by " + shooter.getDisplayName()));

                for (PotionEffect effect : this.snowballEffects) {
                    shooter.addPotionEffect(effect);
                }

                shooter.playSound(shooter.getLocation(), "random.orb", 1.0f, 0.8f);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (target.isOnline()) {
                            target.getInventory().setHelmet(helmetBackup.getOrDefault(target.getUniqueId(), null));
                        }
                        helmetBackup.remove(target.getUniqueId());
                    }
                }.runTaskLater(hcf, 100L);
                return;
            }

            if (hcf.getClassManager().isInAnyClass(target.getUniqueId())) {
                for (PotionEffect effect : this.snowballEffects) {
                    shooter.addPotionEffect(effect, true);
                }

                for (PotionEffect effect : this.targetEffects) {
                    target.addPotionEffect(effect, true);
                }

                shooter.sendMessage(CC.t("&aYou afflicted &f" + target.getDisplayName() + " &awith chilling effects."));
                target.sendMessage(CC.t("&cYeti snowball hit you! Debuffs applied."));

                shooter.playSound(shooter.getLocation(), "random.orb", 1.0f, 0.8f);
                return;
            }

            shooter.sendMessage(CC.t("&cThis snowball only works on players in a class or &b&lDiamond &cclass."));
        }
    }

    private boolean hasDiamondHelmet(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        return helm != null && helm.getType() == Material.DIAMOND_HELMET;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.ICE) {
            if (event.getSlot() == 39 || event.getRawSlot() == 39) {
                event.setCancelled(true);
                player.sendMessage(CC.t("&cYou cannot remove your ice-helmet."));
            }
        }
    }

    private void createHollowIceSphere(Location center) {
        int radius = 6;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance <= radius && distance > radius - 1) {
                        Location loc = center.clone().add(x, y, z);
                        Block block = loc.getBlock();
                        Material type = block.getType();

                        if (type == Material.AIR
                                || type == Material.LONG_GRASS
                                || type == Material.DOUBLE_PLANT
                                || type == Material.WATER
                                || type == Material.STATIONARY_WATER) {
                            iceReplacements.put(loc, type);
                            block.setType(Material.ICE);
                        }
                    }
                }
            }
        }

        // 🔧 Duración configurable en segundos
        int seconds = getClassesConfig().getInt("YETI_CLASS.ICE_DURATION_SECONDS", 5);
        int ticks = seconds * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                removeIceSphere();
            }
        }.runTaskLater(hcf, ticks);
    }

    private void removeIceSphere() {
        for (Map.Entry<Location, Material> entry : iceReplacements.entrySet()) {
            Location loc = entry.getKey();
            Material original = entry.getValue();
            Block block = loc.getBlock();

            if (block.getType() == Material.ICE) {
                block.setType(original);
            }

            for (Player player : loc.getWorld().getPlayers()) {
                if (player.getLocation().distance(loc) <= 4.0) {
                    player.playSound(player.getLocation(), Sound.GLASS, 0.8f, 0.7f);
                }
            }
        }
        iceReplacements.clear();
    }

    public boolean checkyeti(Player player, boolean message) {
        if (getInstance().getTimerManager().getPvpTimer().hasTimer(player)
                || getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player)
                || getInstance().getSotwManager().isActive()) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_PVPTIMER"));
            }
            return true;
        }

        Team team = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        if (team instanceof SafezoneTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_SAFEZONE"));
            }
            return true;
        }
        if (team instanceof EventTeam || team instanceof CitadelTeam
                || team instanceof ConquestTeam || team instanceof DTCTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.YETI_CLASS.CANNOT_ICE_EVENT"));
            }
            return true;
        }

        return false;
    }
}