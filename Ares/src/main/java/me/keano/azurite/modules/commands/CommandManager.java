package me.keano.azurite.modules.commands;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.ability.command.AbilitiesCommand;
import me.keano.azurite.modules.ability.command.AbilityCommand;
import me.keano.azurite.modules.blockshop.command.BlockshopCommand;
import me.keano.azurite.modules.blockshop.command.BuyShopCommand;
import me.keano.azurite.modules.blockshop.command.SellShopCommand;
import me.keano.azurite.modules.bounty.command.BountyCommand;
import me.keano.azurite.modules.commands.type.*;
import me.keano.azurite.modules.commands.type.essential.*;
import me.keano.azurite.modules.customitems.command.CustomItemCommand;
import me.keano.azurite.modules.deathban.command.DeathbanCommand;
import me.keano.azurite.modules.events.boost.command.BoostCommand;
import me.keano.azurite.modules.events.chaos.command.ChaosCommand;
import me.keano.azurite.modules.events.conquest.command.ConquestCommand;
import me.keano.azurite.modules.events.dragon.command.DragonCommand;
import me.keano.azurite.modules.events.dtc.command.DTCCommand;
import me.keano.azurite.modules.events.eotw.command.EOTWCommand;
import me.keano.azurite.modules.events.king.command.KingCommand;
import me.keano.azurite.modules.events.koth.command.KothCommand;
import me.keano.azurite.modules.events.payload.command.PayloadCommand;
import me.keano.azurite.modules.events.purge.command.PurgeCommand;
import me.keano.azurite.modules.events.sotw.command.SOTWCommand;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.keyall.command.KeyAllCommand;
import me.keano.azurite.modules.killstreaks.command.KillstreakCommand;
import me.keano.azurite.modules.killtag.command.KilltagCommand;
import me.keano.azurite.modules.kits.commands.kit.KitCommand;
import me.keano.azurite.modules.kits.commands.kitadmin.KitAdminCommand;
import me.keano.azurite.modules.payouts.command.PayoutsCommand;
import me.keano.azurite.modules.powerups.command.PowerUpsCommand;
import me.keano.azurite.modules.pets.command.PetsCommand;
import me.keano.azurite.modules.reclaims.command.DailyCommand;
import me.keano.azurite.modules.reclaims.command.ReclaimCommand;
import me.keano.azurite.modules.reclaims.command.ResetDailyCommand;
import me.keano.azurite.modules.reclaims.command.ResetReclaimCommand;
import me.keano.azurite.modules.scheduler.command.SchedulesCommand;
import me.keano.azurite.modules.spawners.command.SpawnerCommand;
import me.keano.azurite.modules.staff.command.*;
import me.keano.azurite.modules.teams.commands.citadel.CitadelCommand;
import me.keano.azurite.modules.teams.commands.mountain.MountainCommand;
import me.keano.azurite.modules.teams.commands.systeam.SysTeamCommand;
import me.keano.azurite.modules.teams.commands.team.TeamCommand;
import me.keano.azurite.modules.teams.commands.team.args.TeamCampArg;
import me.keano.azurite.modules.teams.commands.team.args.TeamHQArg;
import me.keano.azurite.modules.timers.command.customtimer.CTimerCommand;
import me.keano.azurite.modules.timers.command.timer.TimerCommand;
import me.keano.azurite.utils.ReflectionUtils;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("unchecked")
public class CommandManager extends Manager {

    private static final Field KNOWN_COMMANDS = ReflectionUtils.accessField(SimpleCommandMap.class, "knownCommands");

    private final List<Command> commands;

    public CommandManager(HCF instance) {
        super(instance);

        this.commands = new ArrayList<>();

        this.load();
        this.checkCommands(); // Check before registering

        instance.getVersionManager().getVersion().getCommandMap().registerAll("ares", commands
                .stream()
                .map(Command::asBukkitCommand)
                .collect(Collectors.toList())
        );
    }

    private void load() {
        commands.addAll(Arrays.asList(
                // Main Azurite
                new AzuriteCommand(this),

                // Team Commands
                new TeamCommand(this),
                new SysTeamCommand(this),
                new MountainCommand(this),

                // Commands (w/ arguments)
                new TimerCommand(this),
                new CTimerCommand(this),
                new DeathbanCommand(this),
                new KitAdminCommand(this),
                new KothCommand(this),
                new KingCommand(this),
                new BoostCommand(this),
                new ChaosCommand(this),
                new DTCCommand(this),
                new CitadelCommand(this),
                new FFACommand(this),
                new MapCommand(this),
                new DragonCommand(this),
                new PayloadCommand(this),

                // Essential commands
                new PlaytimeCommand(this),
                new CraftCommand(this),
                new TopCommand(this),
                new RenameCommand(this),
                new LoreCommand(this),
                new RepairCommand(this),
                new GappleCommand(this),
                new GamemodeCommand(this),
                new GMCCommand(this),
                new GMSCommand(this),
                new PvPCommand(this),
                new BalanceCommand(this),
                new EcoManageCommand(this),
                new WorldCommand(this),
                new TLCommand(this),
                new SettingsCommand(this),
                new LogoutCommand(this),
                new TpCommand(this),
                new TpHereCommand(this),
                new TpRandomCommand(this),
                new TpLocCommand(this),
                new HealCommand(this),
                new FeedCommand(this),
                new BroadcastCommand(this),
                new MessageCommand(this),
                new ReplyCommand(this),
                new IgnoreCommand(this),
                new ClearCommand(this),
                new ClearChatCommand(this),
                new ToggleSoundsCommand(this),
                new TogglePMCommand(this),
                new LivesCommand(this),
                new PayCommand(this),
                new KillCommand(this),
                new ReclaimCommand(this),
                new ResetReclaimCommand(this),
                new DailyCommand(this),
                new ResetDailyCommand(this),
                new SpawnerCommand(this),
                new AbilityCommand(this),
                new AbilitiesCommand(this),
                new LivesManageCommand(this),
                new SOTWCommand(this),
                new SetEndCommand(this),
                new TpAllCommand(this),
                new PingCommand(this),
                new SchedulesCommand(this),
                new StaffCommand(this),
                new StaffBuildCommand(this),
                new VanishCommand(this),
                new HeadVanishCommand(this),
                new FreezeCommand(this),
                new SpawnCommand(this),
                new StatsCommand(this),
                new LeaderboardsCommand(this),
                new EOTWCommand(this),
                new KillstreakCommand(this),
                new FocusCommand(this),
                new UnfocusCommand(this),
                new CrowbarCommand(this),
                new PurgeCommand(this),
                new RedeemCommand(this),
                new ResetRedeemCommand(this),
                new FalltrapTokenCommand(this),
                new BaseTokenCommand(this),
                new SendFalltrapTokenCommand(this),
                new SendBaseTokenCommand(this),
                new ManageFalltrapTokenCommand(this),
                new ManageBaseTokenCommand(this),
                new KilltagCommand(this),
                new KeyAllCommand(this),
                new ConquestCommand(this),
                new RestoreCommand(this),
                new CobbleCommand(this),
                new EnchantCommand(this),
                new InvseeCommand(this),
                new LFFCommand(this),
                new HelpCommand(this),
                new StackCommand(this),
                new EndPlayersCommand(this),
                new NetherPlayersCommand(this),
                new NearCommand(this),
//                new StaffChatCommand(this),
                new RequestCommand(this),
                new RequestsMenuCommand(this),
                new EditModModeCommand(this),
                new ReportCommand(this),
                new ReportsMenuCommand(this),
                new EChestCommand(this),
                new CoordsCommand(this),
                new CopyInvCommand(this),
                new InvToCommand(this),
                new CheckRedeemsCommand(this),
                new HideStaffCommand(this),
                new SetSpawnCommand(this),
                new SetKillsCommand(this),
                new SetDeathsCommand(this),
                new LastKillsCommand(this),
                new LastDeathsCommand(this),
                new SetKillstreakCommand(this),
                new SetRepairCommand(this),
                new SpeedCommand(this),
                new FireResistanceCommand(this),
                new StaffOnlineCommand(this),
                new RefillSignCommand(this),
                new QuickRefillSignCommand(this),
                new KitCommand(this),
                new ManageTeamCommand(this),
                new RebootCommand(this),
                new GlintCommand(this),
                new CustomItemCommand(this),
                new FillBottleCommand(this),
                new BlockshopCommand(this),
                new SellShopCommand(this),
                new BuyShopCommand(this),
                new BountyCommand(this),
                new ArcherUpgradesCommand(this),
                new PayoutsCommand(this),
                new AutoSotwCommand(this),
                new PowerUpsCommand(this),
                new BallonCommand(this),
                new JackpotCommand(this),
                new GlowCommand(this)
                , new PetsCommand(this)
        ));

        // Alias for /f home
        commands.add(new Command(this, "hq") {
            private final TeamHQArg hqArg = new TeamHQArg(this.getManager());

            @Override
            public List<String> aliases() {
                return Collections.emptyList();
            }

            @Override
            public List<String> usage() {
                return Collections.singletonList(hqArg.usage());
            }

            @Override
            public void execute(CommandSender sender, String[] args) {
                hqArg.execute(sender, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String[] args) {
                return hqArg.tabComplete(sender, args);
            }
        });

        // Alias for /f camp
        commands.add(new Command(this, "camp") {
            private final TeamCampArg campArg = new TeamCampArg(this.getManager());

            @Override
            public List<String> aliases() {
                return Collections.emptyList();
            }

            @Override
            public List<String> usage() {
                return Collections.singletonList(campArg.usage());
            }

            @Override
            public void execute(CommandSender sender, String[] args) {
                campArg.execute(sender, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String[] args) {
                return campArg.tabComplete(sender, args);
            }
        });
    }

    // Used to check all the disabled commands and don't register them
    private void checkCommands() {
        List<String> disabled = getConfig().getStringList("DISABLED_COMMANDS.MAIN_COMMANDS");
        Iterator<Command> iterator = commands.iterator();

        while (iterator.hasNext()) {
            Command command = iterator.next();

            if (command instanceof AzuriteCommand) continue;

            if (disabled.contains(command.getName().toLowerCase())) {
                iterator.remove();
                continue;
            }

            for (String alias : command.aliases()) {
                if (!disabled.contains(alias.toLowerCase())) continue;
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public void reload() {
        CommandMap map = getInstance().getVersionManager().getVersion().getCommandMap();

        for (Command command : commands) {
            BukkitCommand bukkitCommand = command.asBukkitCommand();
            boolean isAzuriteCommand = bukkitCommand.getLabel().split(":")[0].equalsIgnoreCase("ares");
            if (!isAzuriteCommand) continue;

            // Unregister listeners so there isn't double when reloading.
            HandlerList.unregisterAll(command);

            for (Argument argument : command.getArguments().values()) {
                HandlerList.unregisterAll(argument);
            }

            bukkitCommand.unregister(map);

            try {

                Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) KNOWN_COMMANDS.get(map);
                knownCommands.values().removeIf(cmd -> cmd.getName().equals(bukkitCommand.getName()));

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        commands.clear();
        this.load();
        this.checkCommands();

        map.registerAll("ares", commands
                .stream()
                .map(Command::asBukkitCommand)
                .collect(Collectors.toList())
        );
    }
}
