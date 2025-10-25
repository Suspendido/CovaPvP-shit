package me.keano.azurite.utils.deco.listeners;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.utils.ApolloUtils;
import me.keano.azurite.utils.CC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

import static com.cryptomorin.xseries.messages.ActionBar.sendActionBar;

public class PlayerListener extends Module<ListenerManager> {


    private final HCF hcf;

    public PlayerListener(ListenerManager manager, HCF hcf) {
        super(manager);
        this.hcf = hcf;


        this.load();
    }

    private void load() {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinStaff(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("zeus.staff")) {
            String[] messages = getLanguageConfig().getStringList("STAFF_MODE.JOIN_MESSAGE").toArray(new String[0]);

            for (String s : messages) {
                s = s
                        .replace("%player%", player.getName())
                        .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(player)))
                        .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(player)));

                player.sendMessage(s);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathSendActionBar(PlayerDeathEvent event) {

        Player victim = event.getEntity().getPlayer();
        Player killer = event.getEntity().getKiller();

        String[] messages = getLanguageConfig().getStringList("DEATH_LISTENER.ACTIONBAR").toArray(new String[0]);

        if (killer != null) {

            for (String s : messages) {
                s = s
                        .replace("%player%", victim.getName())
                        .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(victim)))
                        .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(victim)));

                killer.sendMessage(s);
            }

            sendActionBar(killer, Arrays.toString(messages));


        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItemsSpawn(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Location pLocation = player.getLocation();

        boolean isEnabled = getLanguageConfig().getBoolean("DROPITEMS_SPAWN.ENABLED");
        String message = getLanguageConfig().getString("DROPITEMS_SPAWN.MESSAGE");

        boolean hasBypassPermission = player.hasPermission("zeus.staff");
        boolean isVanished = hcf.getStaffManager().isVanished(player) || hcf.getStaffManager().isHeadVanished(player);
        boolean isStaffMode = hcf.getStaffManager().isStaffEnabled(player) || hcf.getStaffManager().isHeadStaffEnabled(player);

        if (isStaffMode) {
            event.setCancelled(true);
            return;
        }

        if (hcf.getTeamManager().getClaimManager().getTeam(pLocation) instanceof SafezoneTeam) {
            if (hasBypassPermission && isVanished) {
                event.setCancelled(true);
            } else if (hasBypassPermission && !isVanished) {
                event.setCancelled(false);
            } else {
                event.setCancelled(false);
                Item itemDropped = event.getItemDrop();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        itemDropped.remove();
                    }
                }.runTaskLater(hcf, 100L);

                if (isEnabled) {
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) event.getEntity().getShooter();
        Location pLocation = player.getLocation();
        Projectile projectile = event.getEntity();

        boolean hasBypassPermission = player.hasPermission("zeus.staff");
        boolean isVanished = hcf.getStaffManager().isVanished(player);

        if (hcf.getTeamManager().getClaimManager().getTeam(pLocation) instanceof SafezoneTeam) {
            if (hasBypassPermission && isVanished) {
                event.setCancelled(true);
            } else if (hasBypassPermission && !isVanished) {
                event.setCancelled(false);
            } else {
                if (projectile instanceof EnderPearl ||
                        projectile instanceof FishHook ||
                        projectile instanceof Arrow) {

                    event.setCancelled(true);

                    if (projectile instanceof EnderPearl) {
                        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                    } else if (projectile instanceof Arrow) {
                        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathSend(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();

        boolean isEnabled = getLanguageConfig().getBoolean("DEATH_LISTENER.ENABLED_TITLES");
        String title = getLanguageConfig().getString("DEATH_LISTENER.TITLE");
        String subtitle = getLanguageConfig().getString("DEATH_LISTENER.SUBTITLE");

        if (isEnabled) {
            player.sendTitle(title, subtitle);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinSendTitle(PlayerJoinEvent event) {
        Player player = event.getPlayer();


        boolean isEnabled = getLanguageConfig().getBoolean("JOIN_LISTENER.ENABLED");
        String title;
        String subtitle;

        title = getLanguageConfig().getString("JOIN_LISTENER.TITLE");
        subtitle = getLanguageConfig().getString("JOIN_LISTENER.SUBTITLE");


        if (isEnabled) {
            player.sendTitle(title, subtitle);
        }
    }

    public List<Player> getOnlineTeammates(Player player) {
        PlayerTeam pt = hcf.getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null) {
            return pt.getOnlinePlayers(false);
        }

        return Collections.emptyList();
    }

    @EventHandler
    public void onJoinCheckPerms(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("zeus.staff.bypass")) {
            System.out.println("Player " + player.getName() + " has bypass permission.");
        } else {

            player.setGameMode(GameMode.SURVIVAL);
            System.out.println("Player " + player.getName() + " set to survival mode.");
        }

    }

    @EventHandler
    public void onHitWithKnockBack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        Location location = player.getLocation();

        if (hcf.getTeamManager().getClaimManager().getTeam(location) instanceof EventTeam ||
                hcf.getTeamManager().getClaimManager().getTeam(location) instanceof CitadelTeam ||
                hcf.getTeamManager().getClaimManager().getTeam(location) instanceof ConquestTeam) {

            if (player.hasPermission("zeus.bypass"))
                return;

            ItemStack itemInHand = player.getInventory().getItemInHand();
            if (itemInHand != null && itemInHand.containsEnchantment(Enchantment.KNOCKBACK)) {
                event.setCancelled(true);
                player.sendMessage(getLanguageConfig().getString("KOTH_EVENTS.HIT_KNOCKBACK"));
            }
        }
    }

    @EventHandler
    public void onUseBucket(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInHand();

        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) &&
                (itemInHand.getType() == Material.WATER_BUCKET || itemInHand.getType() == Material.LAVA_BUCKET)) {

            if (player.hasPermission("zeus.bypass")) {
                return;
            }

            Block block = event.getClickedBlock();
            Location blockLocation = block != null ? block.getLocation() : player.getLocation();

            if (hcf.getTeamManager().getClaimManager().getTeam(blockLocation) instanceof WildernessTeam) {
                event.setCancelled(true);
                String message = getLanguageConfig().getString("TEAM_LISTENER.CANNOT_PLACE_LIQUID");
                player.sendMessage(message.replace("%team%", getTeamConfig().getString("SYSTEM_TEAMS.WILDERNESS")));
            }
        }
    }

    @EventHandler
    public void onPlaceDisabledBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if(hcf.getTeamManager().getClaimManager().getTeam(block.getLocation()) instanceof WarzoneTeam) {
            if (getConfig().getStringList("DENY_PLACE_BLOCKS").contains(blockType.toString())) {
                if (!player.hasPermission("zeus.bypass" + blockType.toString().toLowerCase())) {
                    event.setCancelled(true);

                    String message = getLanguageConfig().getString("GENERAL_LISTENER.DENY_PLACE_BLOCKS");
                    player.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onBoatPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block clickedBlock = event.getClickedBlock();

        List<Material> boatTypes = Arrays.asList(
                Material.BOAT
        );

        if (item != null && boatTypes.contains(item.getType())) {
            if (clickedBlock != null && hcf.getTeamManager().getClaimManager().getTeam(clickedBlock.getLocation()) instanceof WarzoneTeam) {
                if (getConfig().getStringList("DENY_PLACE_ENTITIES").contains(item.getType().toString())) {
                    if (!player.hasPermission("zeus.bypass.boat")) {
                        event.setCancelled(true);

                        String message = getLanguageConfig().getString("GENERAL_LISTENER.DENY_PLACE_BLOCKS");
                        player.sendMessage(message);
                    }
                }
            }
        }
    }

    private boolean isBoat(Material material) {
        return material.name().endsWith("_BOAT");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinCheckLC(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(hcf, () -> {
            if (!ApolloUtils.isUsingLC(player)) {
                player.sendMessage(CC.t(getLanguageConfig().getString("JOIN_LISTENER.NOT_USING_LC")));
            }
        }, 40L); // 2 seconds
    }


    private final Random random = new Random();

    private final List<PotionEffectType> efectos = Arrays.asList(
            PotionEffectType.SPEED,
            PotionEffectType.SLOW,
            PotionEffectType.FAST_DIGGING,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.JUMP,
            PotionEffectType.CONFUSION,
            PotionEffectType.BLINDNESS,
            PotionEffectType.REGENERATION
    );

    @EventHandler
    public void onStepLaunchPad(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        String blockName = getConfig().getString("LAUNCHPAD.BLOCK", "SPONGE");
        Material launchMaterial = Material.matchMaterial(blockName);

        if (launchMaterial == null) return;

        Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();

        if (blockBelow == launchMaterial) {
            double power = getConfig().getDouble("LAUNCHPAD.POWER", 2.0);
            double y = getConfig().getDouble("LAUNCHPAD.Y", 1.0);

            Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(power);
            direction.setY(y);

            player.setVelocity(direction);


            String soundName = getConfig().getString("LAUNCHPAD.SOUND", "FIREWORK_LAUNCH");
            float volume = (float) getConfig().getDouble("LAUNCHPAD.SOUND_VOLUME", 1.0);
            float pitch = (float) getConfig().getDouble("LAUNCHPAD.SOUND_PITCH", 1.0);

            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {

            }
        }
    }


    
}

