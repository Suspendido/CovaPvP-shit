package me.keano.azurite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.balance.BalanceManager;
import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.board.BoardManager;
import me.keano.azurite.modules.bounty.BountyManager;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.customitems.CustomItemManager;
import me.keano.azurite.modules.deathban.DeathbanManager;
import me.keano.azurite.modules.events.boost.BoostManager;
import me.keano.azurite.modules.events.chaos.ChaosManager;
import me.keano.azurite.modules.events.conquest.ConquestManager;
import me.keano.azurite.modules.events.dragon.DragonManager;
import me.keano.azurite.modules.events.dtc.DTCManager;
import me.keano.azurite.modules.events.eotw.EOTWManager;
import me.keano.azurite.modules.events.king.KingManager;
import me.keano.azurite.modules.events.koth.KothManager;
import me.keano.azurite.modules.events.purge.PurgeManager;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.events.payload.PayloadManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.extra.Configs;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.hooks.abilities.AbilitiesHook;
import me.keano.azurite.modules.hooks.clients.ClientHook;
import me.keano.azurite.modules.hooks.pearls.PearlHook;
import me.keano.azurite.modules.hooks.placeholder.PlaceholderHook;
import me.keano.azurite.modules.hooks.ranks.RankHook;
import me.keano.azurite.modules.hooks.tags.TagHook;
import me.keano.azurite.modules.keyall.KeyAllManager;
import me.keano.azurite.modules.killstreaks.KillstreakManager;
import me.keano.azurite.modules.killtag.KilltagManager;
import me.keano.azurite.modules.kits.KitManager;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.listeners.type.GlitchListener;
import me.keano.azurite.modules.loggers.LoggerManager;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.payouts.PayoutsManager;
import me.keano.azurite.modules.powerups.PowerUpsManager;
import me.keano.azurite.modules.pets.PetManager;
import me.keano.azurite.modules.cpscapper.CpsCapperManager;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.reclaims.ReclaimManager;
import me.keano.azurite.modules.scheduler.ScheduleManager;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.spawners.SpawnerManager;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.storage.StorageManager;
import me.keano.azurite.modules.tablist.TablistManager;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.tips.TipManager;
import me.keano.azurite.modules.users.UserManager;
import me.keano.azurite.modules.versions.VersionManager;
import me.keano.azurite.modules.walls.WallManager;
import me.keano.azurite.modules.waypoints.WaypointManager;
import me.keano.azurite.utils.Logger;
import me.keano.azurite.utils.configs.ConfigYML;
import me.keano.azurite.utils.deco.listeners.DisableListener;
import me.keano.azurite.utils.extra.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public final class HCF extends JavaPlugin {

    private List<Manager> managers;
    private List<ConfigYML> configs;

    private List<Cooldown> cooldowns;
    private List<TeamCooldown> teamCooldowns;
    private List<AzuriteDecimalFormat> decimalFormats;
    private List<AzuriteDateFormat> dateFormats;

    private Configs configsObject;

    private Gson gson; // used for serialization / deserialization
    private GlitchListener glitchListener; // Used to check glitch cooldown

    // Managers
    private UserManager userManager;
    private VersionManager versionManager;
    private TimerManager timerManager;
    private BalanceManager balanceManager;
    private WaypointManager waypointManager;
    private TeamManager teamManager;
    private WallManager wallManager;
    private SpawnerManager spawnerManager;
    private DeathbanManager deathbanManager;
    private BountyManager bountyManager;
    private ReclaimManager reclaimManager;
    private KitManager kitManager;
    private RankHook rankHook;
    private TagHook tagHook;
    private PlaceholderHook placeholderHook;
    private ClientHook clientHook;
    private AbilitiesHook abilitiesHook;
    private PvPClassManager classManager;
    private LoggerManager loggerManager;
    private AbilityManager abilityManager;
    private CustomItemManager customItemManager;
    private StorageManager storageManager;
    private ScheduleManager scheduleManager;
    private StaffManager staffManager;
    private NametagManager nametagManager;
    private KillstreakManager killstreakManager;
    private KilltagManager killtagManager;
    private KeyAllManager keyAllManager;
    private CustomSignManager customSignManager;
    private MenuManager menuManager;
    private ListenerManager listenerManager;
    private BlockshopManager blockshopManager;
    private PayoutsManager payoutsManager;
    private PowerUpsManager powerUpsManager;
    private PetManager petManager;
    private CpsCapperManager cpsCapperManager;

    // Events
    private KothManager kothManager;
    private KingManager kingManager;
    private SOTWManager sotwManager;
    private EOTWManager eotwManager;
    private PurgeManager purgeManager;
    private ConquestManager conquestManager;
    private BoostManager boostManager;
    private ChaosManager chaosManager;
    private DTCManager dtcManager;
    private DragonManager dragonManager;
    private PayloadManager payloadManager;

    private boolean loaded = true;
    private boolean kits;
    private boolean soup;
    public String load;

    @Override
    public void onEnable() {
        this.managers = new ArrayList<>();
        this.configs = new ArrayList<>();
        this.cooldowns = new ArrayList<>();
        this.teamCooldowns = new ArrayList<>();
        this.decimalFormats = new ArrayList<>();
        this.dateFormats = new ArrayList<>();

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        (this.configsObject = new Configs()).load(this);

        this.userManager = new UserManager(this);
        this.versionManager = new VersionManager(this);
        this.timerManager = new TimerManager(this);
        this.balanceManager = new BalanceManager(this);
        this.waypointManager = new WaypointManager(this);
        this.teamManager = new TeamManager(this);
        this.wallManager = new WallManager(this);
        this.spawnerManager = new SpawnerManager(this);
        this.deathbanManager = new DeathbanManager(this);
        this.bountyManager = new BountyManager(this);
        this.reclaimManager = new ReclaimManager(this);
        this.kitManager = new KitManager(this);
        this.rankHook = new RankHook(this);
        this.tagHook = new TagHook(this);
        this.abilitiesHook = new AbilitiesHook(this);
        this.placeholderHook = new PlaceholderHook(this);
        this.clientHook = new ClientHook(this);
        this.classManager = new PvPClassManager(this);
        this.loggerManager = new LoggerManager(this);
        this.abilityManager = new AbilityManager(this);
        this.customItemManager = new CustomItemManager(this);
        this.storageManager = new StorageManager(this);
        this.scheduleManager = new ScheduleManager(this);
        this.staffManager = new StaffManager(this);
        this.nametagManager = new NametagManager(this);
        this.killstreakManager = new KillstreakManager(this);
        this.killtagManager = new KilltagManager(this);
        this.keyAllManager = new KeyAllManager(this);
        this.customSignManager = new CustomSignManager(this);
        this.menuManager = new MenuManager(this);
        this.listenerManager = new ListenerManager(this);
        this.blockshopManager = new BlockshopManager(this);
        this.payoutsManager = new PayoutsManager(this);
        this.powerUpsManager = new PowerUpsManager(this);
        this.petManager = new PetManager(this);
        this.cpsCapperManager = new CpsCapperManager(this);

        //Events
        this.kothManager = new KothManager(this);
        this.kingManager = new KingManager(this);
        this.sotwManager = new SOTWManager(this);
        this.eotwManager = new EOTWManager(this);
        this.purgeManager = new PurgeManager(this);
        this.conquestManager = new ConquestManager(this);
        this.boostManager = new BoostManager(this);
        this.chaosManager = new ChaosManager(this);
        this.dtcManager = new DTCManager(this);
        this.dragonManager = new DragonManager(this);
        this.payloadManager = new PayloadManager(this);

        this.glitchListener = new GlitchListener(listenerManager);
        this.kits = getConfig().getBoolean("KITMAP_MODE");
        this.soup = getConfig().getBoolean("SOUP_MODE");

        new PearlHook(this);
        new CommandManager(this);
        new TipManager(this);
        new BoardManager(this);
        new MemoryClearTask(this);

        //Newest methods
        this.registerRunnable();

        if (Config.TABLIST_ENABLED) new TablistManager(this);

        this.managers.forEach(Manager::enable);
        this.loaded = true;

        Logger.state("Enabled", managers.size(),
                teamManager.getTeams().size(), userManager.getUsers().size(),
                kitManager.getKits().size(), kothManager.getKoths().size());
    }

    @Override
    public void onDisable() {
        if (teamManager != null && userManager != null) {
            this.managers.forEach(Manager::disable);
            Logger.state("Disabled", managers.size(),
                    teamManager.getTeams().size(), userManager.getUsers().size(),
                    kitManager.getKits().size(), kothManager.getKoths().size());
        }
    }

    //Register all runnable
    private void registerRunnable(){
        DisableListener conquestGameMode = new DisableListener(getListenerManager(), this);
        conquestGameMode. new EventConquestGameMode().runTaskTimer(this, 0L, 20L);

        DisableListener citadelGameMode = new DisableListener(getListenerManager(), this);
        citadelGameMode. new EventCitadelGameMode().runTaskTimer(this, 0L, 20L);

        DisableListener eventGameMode = new DisableListener(getListenerManager(),this);
        eventGameMode. new EventGameMode().runTaskTimer(this, 0L, 20L);

        DisableListener removeInvis = new DisableListener(getListenerManager(), this);
        removeInvis. new RemoveInvisibilityEffect().runTaskTimer(this, 0L, 20L);
    }
}
