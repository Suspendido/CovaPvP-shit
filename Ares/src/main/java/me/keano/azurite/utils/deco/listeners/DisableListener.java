package me.keano.azurite.utils.deco.listeners;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.teams.type.CitadelTeam;
import me.keano.azurite.modules.teams.type.ConquestTeam;
import me.keano.azurite.modules.teams.type.EventTeam;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.cryptomorin.xseries.messages.ActionBar.sendActionBar;


public class DisableListener extends Module<ListenerManager> {

    private final HCF hcf;


    public DisableListener(ListenerManager manager, HCF hcf) {
        super(manager);
        this.hcf = hcf;


        this.load();
    }

    private void load() {
    }


    @EventHandler
    public void onAttackWithGlobalCooldown(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        Location location = player.getLocation();

        if (hcf.getAbilityManager().getGlobalCooldown().hasTimer(player)) {

            if (hcf.getTeamManager().getClaimManager().getTeam(location) instanceof EventTeam ||
                    hcf.getTeamManager().getClaimManager().getTeam(location) instanceof CitadelTeam ||
                    hcf.getTeamManager().getClaimManager().getTeam(location) instanceof ConquestTeam) {


                event.setCancelled(true);
                String message = getLanguageConfig().getString("KOTH_EVENTS.GLOBAL_COOLDOWN_DENY_PVP");
                sendActionBar(player, message);
            }
        }
    }


    //If player enters to any EVENT zone, change to adventure gamemode
    public class EventGameMode extends BukkitRunnable {
        private Set<UUID> playersInKoth = new HashSet<>();

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location location = player.getLocation();
                if (player.hasPermission("zeus.staff.bypass")){

                } else {
                    if (hcf.getTeamManager().getClaimManager().getTeam(location) instanceof EventTeam) {
                        if (!playersInKoth.contains(player.getUniqueId())) {
                            playersInKoth.add(player.getUniqueId());
                            player.setGameMode(GameMode.ADVENTURE);
                            removeInvisibility(player);
                        }
                    } else {
                        if (playersInKoth.contains(player.getUniqueId())) {
                            playersInKoth.remove(player.getUniqueId());
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                }
            }
        }
    }

    public class EventConquestGameMode extends BukkitRunnable {
        private Set<UUID> playersInConquest = new HashSet<>();

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location location = player.getLocation();
                if (player.hasPermission("zeus.staff.bypass")){

                } else {
                    if (hcf.getTeamManager().getClaimManager().getTeam(location) instanceof ConquestTeam) {
                        if (!playersInConquest.contains(player.getUniqueId())) {
                            playersInConquest.add(player.getUniqueId());
                            player.setGameMode(GameMode.ADVENTURE);
                            removeInvisibility(player);
                        }
                    } else {
                        if (playersInConquest.contains(player.getUniqueId())) {
                            playersInConquest.remove(player.getUniqueId());
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                }
            }
        }
    }

    public class EventCitadelGameMode extends BukkitRunnable {
        private Set<UUID> playersInCitadel = new HashSet<>();

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location location = player.getLocation();
                if (player.hasPermission("zeus.staff.bypass")){

                } else {
                    if (hcf.getTeamManager().getClaimManager().getTeam(location) instanceof CitadelTeam) {
                        if (!playersInCitadel.contains(player.getUniqueId())) {
                            playersInCitadel.add(player.getUniqueId());
                            player.setGameMode(GameMode.ADVENTURE);
                            removeInvisibility(player);
                        }
                    } else {
                        if (playersInCitadel.contains(player.getUniqueId())) {
                            playersInCitadel.remove(player.getUniqueId());
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                }
            }
        }
    }

    public class RemoveInvisibilityEffect extends BukkitRunnable {
        private Set<UUID> playersWithInvisibility = new HashSet<>();

        @Override
        public void run() {
            if (!getLanguageConfig().getBoolean("REMOVE_INVIS.ENABLED", true)) {
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {

                World world = player.getWorld();
                String message = getLanguageConfig().getString("REMOVE_INVIS.MESSAGE");
                if (world.getEnvironment() == World.Environment.NETHER || world.getEnvironment() == World.Environment.THE_END) {
                    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                        playersWithInvisibility.add(player.getUniqueId());
                        player.sendMessage(message);
                    }
                } else {
                    playersWithInvisibility.remove(player.getUniqueId());
                }
            }
        }
    }


    public static void removeInvisibility(Player player){

        player.removePotionEffect(PotionEffectType.INVISIBILITY);

    }

    @EventHandler
    public void onCommandKitAndEC(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        String command = event.getMessage().split(" ")[0].toLowerCase();

        Set<String> restrictedCommands = new HashSet<>(Arrays.asList("/echest", "/ec", "/enderchest", "/enderc"));

        if (player.hasPermission("zeus.headstaff")) {
            return;
        }

        if (hcf.getTimerManager().getCombatTimer().hasTimer(player) ||
                hcf.getTeamManager().getClaimManager().getTeam(location) instanceof EventTeam ||
                hcf.getTeamManager().getClaimManager().getTeam(location) instanceof ConquestTeam ||
                hcf.getTeamManager().getClaimManager().getTeam(location) instanceof CitadelTeam) {

            if (restrictedCommands.contains(command)) {

                String m = getLanguageConfig().getString("GLOBAL_COMMANDS.COMMAND_COMBAT");
                event.setCancelled(true);
                player.sendMessage(m);
            }
        }
    }



}

