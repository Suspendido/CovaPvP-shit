package me.keano.azurite.modules.listeners.type.team;

import me.keano.azurite.modules.ability.type.AntiTrapBeaconAbility;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import me.keano.azurite.HCF;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("deprecation")
public class TeamListener extends Module<TeamManager> {

    private final List<Material> deniedInteract;
    private final HCF hcf;
    private final Set<Player> inSpawn = new HashSet<>();

    public TeamListener(TeamManager manager, HCF hcf) {
        super(manager);
        this.hcf = hcf;
        this.deniedInteract = getTeamConfig().getStringList("SYSTEM_TEAMS.DENIED_INTERACT")
                .stream().map(ItemUtils::getMat).collect(Collectors.toList());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntity(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        Team team = getManager().getClaimManager().getTeam(entity.getLocation());

        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        if (team instanceof WarzoneTeam && !getConfig().getBoolean("MOB_SPAWN_WARZONE")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemFrameHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Hanging)) return;

        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (getManager().canBuild(damager, e.getEntity().getLocation())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onCitadelPearl(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.ENDER_PEARL) return;

        Player player = e.getPlayer();
        Team atPlayer = getManager().getClaimManager().getTeam(player.getLocation());

        if (atPlayer instanceof CitadelTeam) {
            e.setCancelled(true);
            player.updateInventory();
            player.sendMessage(getLanguageConfig().getString("CITADEL.DENIED_PEARL"));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onTeleportDisqualified(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = e.getPlayer();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) return;

        Team toTeleport = getManager().getClaimManager().getTeam(e.getTo());

        if (pt.isDisqualified() && (toTeleport instanceof EventTeam || toTeleport instanceof ConquestTeam)) {
            e.setCancelled(true);
            getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            player.sendMessage(getLanguageConfig().getString("DISQUALIFIED.DENIED_TELEPORT"));
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onCitadelTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = e.getPlayer();
        Team toTeleport = getManager().getClaimManager().getTeam(e.getTo());

        if (toTeleport instanceof CitadelTeam) {
            e.setCancelled(true);
            getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            player.sendMessage(getLanguageConfig().getString("CITADEL.DENIED_TELEPORT"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemFrameRotate(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Hanging)) return;

        Player damager = e.getPlayer();

        if (!getManager().canBuild(damager, e.getRightClicked().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemFrameBreak(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player)) return;

        Player damager = (Player) e.getRemover();

        if (!getManager().canBuild(damager, e.getEntity().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemFramePlace(HangingPlaceEvent e) {
        Player player = e.getPlayer();

        if (!getManager().canBuild(player, e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDecay(LeavesDecayEvent e) {
        Team atDecay = getManager().getClaimManager().getTeam(e.getBlock().getLocation());

        if (atDecay instanceof WildernessTeam || atDecay instanceof PlayerTeam) return;

        e.setCancelled(true); // In spawn, road, events
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFade(BlockFadeEvent e) {
        if (e.getBlock().getType().name().contains("REDSTONE")) return; // Optimize redstone checks

        Team atFade = getManager().getClaimManager().getTeam(e.getBlock().getLocation());

        if (atFade instanceof WildernessTeam || atFade instanceof PlayerTeam) return;

        e.setCancelled(true); // In spawn, road, events
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBurn(BlockBurnEvent e) {
        Team atBurn = getManager().getClaimManager().getTeam(e.getBlock().getLocation());

        if (atBurn instanceof WildernessTeam || atBurn instanceof PlayerTeam) return;

        e.setCancelled(true); // In spawn, road, events
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onForm(BlockFormEvent e) {
        Team atForm = getManager().getClaimManager().getTeam(e.getBlock().getLocation());

        if (atForm instanceof WildernessTeam || atForm instanceof PlayerTeam) return;

        e.setCancelled(true); // In spawn, road, events
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return; // iHCF - XP orbs might lag spam

        if (e.getReason() != EntityTargetEvent.TargetReason.CLOSEST_PLAYER ||
                e.getReason() != EntityTargetEvent.TargetReason.RANDOM_TARGET) return;

        Player player = (Player) e.getTarget();
        PlayerTeam pt = getManager().getByPlayer(player.getUniqueId());
        Team atTarget = getManager().getClaimManager().getTeam(e.getEntity().getLocation());

        if (atTarget instanceof SafezoneTeam || atTarget == pt) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityBlockChange(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (!getManager().canBuild(player, e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExtend(BlockPistonExtendEvent e) {
        Team start = getManager().getClaimManager().getTeam(e.getBlock().getLocation());

        for (Block block : e.getBlocks()) {
            Team toExtend = getManager().getClaimManager().getTeam(block.getRelative(e.getDirection()).getLocation());

            if (toExtend instanceof WarzoneTeam || toExtend instanceof WildernessTeam) continue;

            if (start != toExtend) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRetract(BlockPistonRetractEvent e) {
        Team start = getManager().getClaimManager().getTeam(e.getBlock().getLocation());

        if (Utils.isModernVer()) {
            for (Block block : e.getBlocks()) {
                Team toExtend = getManager().getClaimManager().getTeam(block.getLocation());

                if (toExtend instanceof WarzoneTeam || toExtend instanceof WildernessTeam) continue;

                if (start != toExtend) {
                    e.setCancelled(true);
                    break;
                }
            }
        } else {
            Block retractBlock = e.getRetractLocation().getBlock();

            if (retractBlock.isEmpty() || retractBlock.isLiquid()) return;

            Team piston = getManager().getClaimManager().getTeam(e.getBlock().getLocation());
            Team retract = getManager().getClaimManager().getTeam(retractBlock.getLocation());

            if (piston != retract) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStickyPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky())
            return;

        Block retractLocation = event.getBlock()
                .getRelative(event.getDirection().getOppositeFace(), 2);

        if (retractLocation.isEmpty()
                || retractLocation.isLiquid())
            return;

        Block block = event.getBlock();
        Team team = getManager().getClaimManager().getTeam(retractLocation.getLocation());

        if (team instanceof PlayerTeam && !((PlayerTeam) team).isRaidable() &&
                team != getManager().getClaimManager().getTeam(block.getLocation()))
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleportSpawn(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        if (!getConfig().getBoolean("DENY_ENDERPEARL_TO_SPAWN")) return;

        Player player = e.getPlayer();
        Team to = getManager().getClaimManager().getTeam(e.getTo());
        Team from = getManager().getClaimManager().getTeam(e.getFrom());

        if (!(from instanceof SafezoneTeam) && to instanceof SafezoneTeam) {
            e.setCancelled(true);
            getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            player.sendMessage(getLanguageConfig().getString("TEAM_LISTENER.DENY_TELEPORT_TO_SPAWN"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractSpawn(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (!getConfig().getBoolean("DENY_ENDERPEARL_IN_SPAWN")) return;
        if (e.getItem() == null || e.getItem().getType() != Material.ENDER_PEARL) return;

        Player player = e.getPlayer();
        Team at = getManager().getClaimManager().getTeam(player.getLocation());

        if (at instanceof SafezoneTeam) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("TEAM_LISTENER.DENY_ENDERPEARL_IN_SPAWN"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent e) {
        Block block = e.getBlock();
        Team from = getManager().getClaimManager().getTeam(block.getLocation());
        Team to = getManager().getClaimManager().getTeam(e.getVelocity().toLocation(block.getWorld()));

        if (to != from && !(to instanceof WildernessTeam) && !(to instanceof WarzoneTeam)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFromTo(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        Block fromBlock = event.getBlock();

        Material fromType = fromBlock.getType();
        Material toType = toBlock.getType();

        Team toBlockTeam = getManager().getClaimManager().getTeam(toBlock.getLocation());
        Team fromBlockTeam = getManager().getClaimManager().getTeam(fromBlock.getLocation());

        if (!getConfig().getBoolean("FIXES.OBSIDIAN_GENS")) {
            if ((toType == Material.REDSTONE_WIRE || toType == Material.TRIPWIRE) &&
                    (fromType == Material.AIR || fromType.name().contains("WATER") || fromType.name().contains("LAVA"))) {
                toBlock.setType(Material.AIR);
            }
        }

        if (fromType.name().contains("WATER") || fromType.name().contains("LAVA")) { // use contains for multi ver support
            if (getConfig().getBoolean("WATER_MOVE_CLAIMS") && toBlockTeam instanceof PlayerTeam) {
                return;
            }

            if (toBlockTeam != fromBlockTeam) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == e.getFrom()) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        checkClaim(e.getPlayer(), e.getTo(), e.getFrom());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getTo() == e.getFrom()) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        checkClaim(e.getPlayer(), e.getTo(), e.getFrom());
    }

    private void checkClaim(Player player, Location toLoc, Location fromLoc) {
        Team to = getManager().getClaimManager().getTeam(toLoc);
        Team from = getManager().getClaimManager().getTeam(fromLoc);
        SOTWManager sotwManager = getInstance().getSotwManager();

        if (from == to) return;

        if (sotwManager.getFlying().contains(player.getUniqueId()) && !sotwManager.canFly(player, to)) {
            sotwManager.toggleFly(player);
            player.sendMessage(getLanguageConfig().getString("SOTW_COMMAND.SOTW_FLY.TOGGLE_FALSE"));
        }

        for (String string : Config.CLAIM_CHANGE) {
            player.sendMessage(string
                    .replace("%to-team%", to.getDisplayName(player))
                    .replace("%from-team%", from.getDisplayName(player))
                    .replace("%to-deathban%", to.isDeathban() ? Config.DEATHBAN_STRING : Config.NON_DEATHBAN_STRING)
                    .replace("%from-deathban%", from.isDeathban() ? Config.DEATHBAN_STRING : Config.NON_DEATHBAN_STRING)
            );
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Team atBreak = getManager().getClaimManager().getTeam(block.getLocation());

        if (!getManager().canBuild(player, block.getLocation(), true)) {
            e.setCancelled(true);
            player.sendMessage(Config.BLOCK_DIG_DENIED
                    .replace("%team%", atBreak.getDisplayName(player))
            );
        }

        if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof DTCTeam) {
            if (getInstance().getDtcManager().isActive()) {
                Material dtcMaterial = getInstance().getDtcManager().getDTCMaterial();
                if (block.getType() == dtcMaterial) {
                    e.setCancelled(false);

                } else {
                    e.setCancelled(true);
                    player.sendMessage(getLanguageConfig().getString("DTC_COMMAND.NO_BLOCK"));
                }
            } else {

                if(player.hasPermission("zeus.headstaff")){
                    e.setCancelled(false);
                } else {
                    e.setCancelled(true);
                    player.sendMessage(getLanguageConfig().getString("DTC_COMMAND.NOT_ACTIVE"));
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location blockPlace = block.getLocation();
        Team team = getManager().getClaimManager().getTeam(blockPlace);

        if (block.getType() == ItemUtils.getMat("WEB") && !e.getBlockReplacedState().getBlock().isLiquid()) {
            if ((team instanceof WarzoneTeam && Config.COBWEB_PLACE_WARZONE) ||
                    (team instanceof WildernessTeam && Config.COBWEB_PLACE_WILDERNESS) ||
                    (team instanceof RoadTeam && Config.COBWEB_PLACE_ROAD)) {
                team.getCobwebs().put(blockPlace, new BukkitRunnable() {
                    @Override
                    public void run() {
                        block.setType(Material.AIR);
                    }
                }.runTaskLater(getInstance(), 20L * Config.COBWEB_DESPAWN_TIME));
                return;
            }
        }

        AntiTrapBeaconAbility beacon = (AntiTrapBeaconAbility) getInstance().getAbilityManager().getAbility("AntiTrapBeacon");
        ItemStack hand = getManager().getItemInHand(player);

        if (hand != null && beacon.isSimilar(hand)) {
            beacon.handlePlace(e, team);
            return;
        }

        if (!getManager().canBuild(player, blockPlace)) {
            Location location = player.getLocation();

            if (location.getY() > blockPlace.getY() && location.getBlockX() == blockPlace.getBlockX() &&
                    location.getBlockZ() == blockPlace.getBlockZ()) {
                player.setVelocity(new Vector(0, -3.0, 0).multiply(2.0F));
            }

            e.setCancelled(true);
            player.sendMessage(Config.BLOCK_PLACE_DENIED
                    .replace("%team%", team.getDisplayName(player))
            );
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();

        if (clicked == null) return;
        Team atInteract = getManager().getClaimManager().getTeam(clicked.getLocation());

        // Si el jugador golpea un cofre en una montaña, lo eliminamos
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && atInteract instanceof MountainTeam) {
            MountainTeam mt = (MountainTeam) atInteract;

            if (mt.getChests().contains(clicked.getLocation()) && clicked.getType() == Material.CHEST) {
                Chest chest = (Chest) clicked.getState();
                Location chestLocation = chest.getLocation();

                // Drop items
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        chest.getWorld().dropItemNaturally(chestLocation, item);
                    }
                }

                // Limpiar inventario y romper cofre
                chest.getInventory().clear();
                clicked.setType(Material.AIR);
                chest.getWorld().playSound(chestLocation, Sound.ZOMBIE_WOODBREAK, 1.0f, 1.0f);

                e.setCancelled(true);
                return;
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && atInteract instanceof MountainTeam) {
            MountainTeam mt = (MountainTeam) atInteract;
            if (mt.getChests().contains(clicked.getLocation())) return;
        }

        if (atInteract instanceof WarzoneTeam) {
            WarzoneTeam wt = (WarzoneTeam) atInteract;
            if (wt.canInteract(clicked.getLocation())) return;
        }

        if (atInteract instanceof RoadTeam) {
            RoadTeam rt = (RoadTeam) atInteract;
            if (rt.canInteract(clicked.getLocation())) return;
        }

        if (atInteract instanceof SafezoneTeam && deniedInteract.contains(clicked.getType())) {
            return;
        }

        if (!getManager().canBuild(player, clicked.getLocation()) && deniedInteract.contains(clicked.getType())) {
            e.setCancelled(true);
            player.sendMessage(Config.BLOCK_INTERACT_DENIED
                    .replace("%team%", atInteract.getDisplayName(player))
            );
        }
    }

    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest)) return;

        Chest chest = (Chest) event.getInventory().getHolder();
        Location chestLocation = chest.getLocation();
        Team atInteract = getManager().getClaimManager().getTeam(chestLocation);

        if (!(atInteract instanceof MountainTeam)) return;

        // Verificar si el bloque aún es un cofre
        if (chest.getBlock().getType() != Material.CHEST) {
            return; // Si no es un cofre, no intentamos acceder al inventario
        }

        Bukkit.getScheduler().runTaskLater(hcf, () -> {
            if (isChestEmpty(chest)) {
                chest.getBlock().setType(Material.AIR);
                chest.getWorld().playSound(chestLocation, Sound.ZOMBIE_WOODBREAK, 1.0f, 1.0f);
            }
        }, 5L);
    }


    private boolean isChestEmpty(Chest chest) {
        for (ItemStack item : chest.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFill(PlayerBucketFillEvent e) {
        Player player = e.getPlayer();
        Team team = getManager().getClaimManager().getTeam(e.getBlockClicked().getLocation());

        if (!getManager().canBuild(player, e.getBlockClicked().getLocation())) {
            e.setCancelled(true);
            player.sendMessage(Config.BLOCK_INTERACT_DENIED
                    .replace("%team%", team.getDisplayName(player))
            );
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Team team = getManager().getClaimManager().getTeam(e.getBlockClicked().getLocation());

        if (!getManager().canBuild(player, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation(), true)) {
            e.setCancelled(true);
            player.sendMessage(Config.BLOCK_INTERACT_DENIED
                    .replace("%team%", team.getDisplayName(player))
            );
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotion(PotionSplashEvent e) {
        if (!Utils.isDebuff(e.getPotion())) return;

        Player thrower = Utils.getDamager(e.getEntity());

        if (thrower == null) return;

        if (getManager().getClaimManager().getTeam(thrower.getLocation()) instanceof SafezoneTeam) {
            e.setCancelled(true);
            return;
        }

        for (LivingEntity entity : e.getAffectedEntities()) {
            if (!(entity instanceof Player)) continue;
            if (entity == thrower) continue;

            PlayerTeam throwerTeam = getManager().getByPlayer(thrower.getUniqueId());
            PlayerTeam affectedTeam = getManager().getByPlayer(entity.getUniqueId());

            // Allow same team debuff
            if (affectedTeam != null && throwerTeam == affectedTeam) continue;

            if (!getManager().canHit(thrower, (Player) entity, false)) {
                e.setIntensity(entity, 0.0D);

            } else {
                // If they can hit the player, apply combat so that a player cant debuff and run back into spawn.
                getInstance().getTimerManager().getCombatTimer().applyTimer(thrower);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        Team atChange = getManager().getClaimManager().getTeam(player.getLocation());

        if (atChange instanceof SafezoneTeam) {
            e.setCancelled(true);
            player.setSaturation(20F);
            player.setFoodLevel(20);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        Team atDamage = getManager().getClaimManager().getTeam(player.getLocation());

        if (atDamage instanceof SafezoneTeam) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Entity damager = e.getDamager();
        Player damaged = (Player) e.getEntity();

        if (Bukkit.getPlayer(damaged.getUniqueId()) == null) {
            e.setCancelled(true); // It's a NPC
            return;
        }

        if (damager instanceof Player) {
            if (!getManager().canHit((Player) damager, damaged, true)) {
                e.setCancelled(true);
            }
        } else {
            if (!(damager instanceof Projectile)) return;

            Projectile projectile = (Projectile) damager;

            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();

                if (shooter == damaged) {
                    // Bow boosting
                    if (Config.ALLOW_BOW_BOOSTING && projectile instanceof Arrow) {
                        return;
                    }

                    // Ender pearl damage
                    if (projectile instanceof EnderPearl) return;
                }

                if (!getManager().canHit((Player) projectile.getShooter(), damaged, true)) {
                    e.setCancelled(true);
                }
            }
        }
    }
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void itemsonSpawn(PlayerMoveEvent e) {
//        Player player = e.getPlayer();
//        Team team = getManager().getClaimManager().getTeam(player.getLocation());
//
//        if (getInstance().getStaffManager().isStaffEnabled(player) ||
//                getInstance().getStaffManager().isVanished(player) ||
//                getInstance().getStaffManager().isHeadStaffEnabled(player) ||
//                getInstance().getStaffManager().isHeadVanished(player)) {
//            return;
//        }
//
//        if (!(getInstance().isKits() || getInstance().isSoup())) {
//            return;
//        }
//
//        boolean isInSpawn = team instanceof SafezoneTeam;
//
//        if (isInSpawn) {
//            if (!inSpawn.contains(player)) {
//                inSpawn.add(player);
//                player.getInventory().clear();
//                player.getInventory().setArmorContents(null);
//                giveSpawnItems(player);
//            }
//        } else {
////            getInstance().getKitManager().getKit("pvp").equip(player);
//            inSpawn.remove(player);
//        }
//    }
//
//    private void giveSpawnItems(Player player) {
//        ItemStack trip = new ItemStack(Material.TRIPWIRE_HOOK);
//        ItemMeta tripMeta = trip.getItemMeta();
//        tripMeta.setDisplayName(CC.t("&d&lKit Selector &7(Right-Click)"));
//        trip.setItemMeta(tripMeta);
//        trip.setItemMeta(tripMeta);
//
//        player.getInventory().setItem(4, trip);
//    }
//    @EventHandler
//    public void onRightClick(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//        ItemStack item = event.getItem();
//
//        if (item == null || item.getType() != Material.TRIPWIRE_HOOK) return;
//        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
//
//        String displayName = item.getItemMeta().getDisplayName();
//
//        if (displayName.equals(CC.t("&d&lKit Selector &7(Right-Click)"))) {
//            event.setCancelled(true);
//            // OPCIÓN 1: Abrir un menú
////            openSpawnMenu(player);
//
//            // OPCIÓN 2: Ejecutar comando (si querés esto en lugar del menú)
//            player.performCommand("dm open namemc");
//
//        }
//    }
}