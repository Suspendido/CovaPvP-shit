package me.keano.azurite.modules.board.adapter;

import me.keano.azurite.modules.ability.extra.GlobalCooldown;
import me.keano.azurite.modules.board.BoardAdapter;
import me.keano.azurite.modules.board.BoardManager;
import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.events.boost.BoostManager;
import me.keano.azurite.modules.events.chaos.ChaosManager;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.events.conquest.extra.ConquestType;
import me.keano.azurite.modules.events.dragon.DragonManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.events.payload.PayloadManager;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.pvpclass.type.bard.BardClass;
import me.keano.azurite.modules.pvpclass.type.bomber.BomberClass;
import me.keano.azurite.modules.pvpclass.type.ghost.GhostClass;
import me.keano.azurite.modules.pvpclass.type.ghost.GhostData;
import me.keano.azurite.modules.pvpclass.type.mage.MageClass;
import me.keano.azurite.modules.pvpclass.type.miner.MinerClass;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.listeners.playertimers.AbilityTimer;
import me.keano.azurite.modules.timers.listeners.playertimers.AppleTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.AntiRaidTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.RebootTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.anticlean.AntiCleanTask;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.cuboid.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AzuriteBoard extends Module<BoardManager> implements BoardAdapter {

    private final SOTWManager sotwManager;
    private final BoostManager boostManager;
    private final ChaosManager chaosManager;
    private final DragonManager dragonManager;
    private final PayloadManager payloadManager;
    private final StaffManager staffManager;
    private final boolean linesEnabled;
    private final boolean lastLineEnabled;

    private final List<String> kingLines;
    private final List<String> boostLines;
    private final List<String> chaosLines;
    private final List<String> payloadLines;
    private final List<String> dragonLines;
    private final List<String> focusLines;
    private final List<String> focusSystemLines;
    private final List<String> conquestLines;
    private final List<String> kitsLines;
    private final List<String> soupLines;
    private final List<String> noHModMode;
    private final List<String> hModMode;
    private final List<String> noModMode;
    private final List<String> modMode;
    private final List<String> footerLines;
    private final List<String> claim;
    private final List<String> kothLines;
    private final List<String> antiCleanLines;

    private final String line;
    private final String raidTimer;
    private final String sotwOff;
    private final String sotw;
    private final String appleLimit;
    private final String className;
    private final String eotw;
    private final String purge;
    private final String dtc;
    private final String bardEnergy;
    private final String mageEnergy;
    private final String minerInvis;
    private final String minerDiamonds;
    private final String ghostMode;
    private final String globalAbilities;
    private final String customTimerFormat;
    private final String deathbanInfo;
    private final String deathbanLives;

    private final boolean footerEnabled;
    private final boolean anticleanEnabled;
    private final boolean focusEnabled;
    private final boolean boostEnabled;
    private final boolean chaosEnabled;
    private final boolean payloadEnabled;
    private final boolean dragonEnabled;
    private final boolean kingEnabled;
    private final boolean staffEnabled;
    private final boolean headstaffEnabled;
    private final boolean SoupInfoOnlySpawn;
    private final boolean kitsInfoOnlySpawn;
    private final boolean conquestOnlyTeam;
    private final boolean claimEnabled;
    private final boolean kothEnabled;
    private final boolean onlyPositiveKillstreak;

    private final String actionBarSeparator;
    private final boolean actionBarStatsOnlySpawn;

    public AzuriteBoard(BoardManager manager) {
        super(manager);

        this.sotwManager = getInstance().getSotwManager();
        this.boostManager = getInstance().getBoostManager();
        this.chaosManager = getInstance().getChaosManager();
        this.payloadManager = getInstance().getPayloadManager();
        this.dragonManager = getInstance().getDragonManager();
        this.staffManager = getInstance().getStaffManager();
        this.linesEnabled = getScoreboardConfig().getBoolean("SCOREBOARD_INFO.LINES_ENABLED");
        this.lastLineEnabled = getScoreboardConfig().getBoolean("SCOREBOARD_INFO.LAST_LINE_ENABLED");

        this.kingLines = getScoreboardConfig().getStringList("KILL_THE_KING.LINES");
        this.antiCleanLines = getScoreboardConfig().getStringList("ANTI_CLEAN.LINES");
        this.boostLines = getScoreboardConfig().getStringList("BOOST.LINES");
        this.chaosLines = getScoreboardConfig().getStringList("CHAOS_EVENT.LINES");
        this.payloadLines = getScoreboardConfig().getStringList("PAYLOAD.LINES");

        this.dragonLines = getScoreboardConfig().getStringList("DRAGON_EVENT.LINES");
        this.focusLines = getScoreboardConfig().getStringList("TEAM_FOCUS.LINES_PLAYER");
        this.focusSystemLines = getScoreboardConfig().getStringList("TEAM_FOCUS.LINES_SYSTEM");
        this.conquestLines = getScoreboardConfig().getStringList("CONQUEST.LINES");
        this.kitsLines = getScoreboardConfig().getStringList("KITS_INFO");
        this.soupLines = getScoreboardConfig().getStringList("SOUP_INFO");
        this.noHModMode = getScoreboardConfig().getStringList("HSTAFF_MODE.VANISH_NO_MODMODE");
        this.hModMode = getScoreboardConfig().getStringList("HSTAFF_MODE.MOD_MODE");
        this.noModMode = getScoreboardConfig().getStringList("STAFF_MODE.VANISH_NO_MODMODE");
        this.modMode = getScoreboardConfig().getStringList("STAFF_MODE.MOD_MODE");
        this.footerLines = getScoreboardConfig().getStringList("FOOTER.LINES");
        this.claim = getScoreboardConfig().getStringList("CLAIM.LINES");
        this.kothLines = getScoreboardConfig().getStringList("KOTH.LINES");

        this.line = getScoreboardConfig().getString("SCOREBOARD_INFO.LINES");
        this.raidTimer = getString("PLAYER_TIMERS.RAID_TIMER");
        this.sotwOff = getString("PLAYER_TIMERS.SOTW_OFF");
        this.sotw = getString("PLAYER_TIMERS.SOTW");
        this.appleLimit = getString("PLAYER_TIMERS.APPLE_LIMIT");
        this.className = getString("PLAYER_TIMERS.ACTIVE_CLASS");
        this.eotw = getString("PLAYER_TIMERS.PRE_EOTW");
        this.purge = getString("PLAYER_TIMERS.PURGE");
        this.dtc = getString("PLAYER_TIMERS.DTC");
        this.bardEnergy = getString("BARD_CLASS.BARD_ENERGY");
        this.mageEnergy = getString("MAGE_CLASS.MAGE_ENERGY");
        this.minerInvis = getString("MINER_CLASS.INVIS");
        this.minerDiamonds = getString("MINER_CLASS.DIAMONDS");
        this.ghostMode = getString("GHOST_CLASS.MODE");
        this.globalAbilities = getString("PLAYER_TIMERS.GLOBAL_ABILITIES");
        this.customTimerFormat = getString("CUSTOM_TIMERS.FORMAT");
        this.deathbanInfo = getString("DEATHBAN_INFO.TIME");
        this.deathbanLives = getString("DEATHBAN_INFO.LIVES");

        this.footerEnabled = getScoreboardConfig().getBoolean("FOOTER.ENABLED");
        this.focusEnabled = getScoreboardConfig().getBoolean("TEAM_FOCUS.ENABLED");
        this.anticleanEnabled = getScoreboardConfig().getBoolean("ANTI_CLEAN.ENABLED");
        this.kingEnabled = getScoreboardConfig().getBoolean("KILL_THE_KING.ENABLED");
        this.boostEnabled = getScoreboardConfig().getBoolean("BOOST.ENABLED");
        this.chaosEnabled = getScoreboardConfig().getBoolean("CHAOS_EVENT.ENABLED");
        this.payloadEnabled = getScoreboardConfig().getBoolean("PAYLOAD.ENABLED");
        this.dragonEnabled = getScoreboardConfig().getBoolean("DRAGON_EVENT.ENABLED");
        this.staffEnabled = getScoreboardConfig().getBoolean("STAFF_MODE.ENABLED");
        this.headstaffEnabled = getScoreboardConfig().getBoolean("HSTAFF_MODE.ENABLED");
        this.kitsInfoOnlySpawn = getConfig().getBoolean("KITS_INFO_ONLY_SPAWN");
        this.SoupInfoOnlySpawn = getConfig().getBoolean("SOUP_INFO_ONLY_SPAWN");
        this.conquestOnlyTeam = getConfig().getBoolean("CONQUEST.SCOREBOARD_ONLY_CONQUEST_CLAIM");
        this.claimEnabled = getScoreboardConfig().getBoolean("CLAIM.ENABLED");
        this.kothEnabled = getScoreboardConfig().getBoolean("KOTH.ENABLED");
        this.onlyPositiveKillstreak = getScoreboardConfig().getBoolean("SCOREBOARD_INFO.SHOW_KILLSTREAK_ONLY_POSITIVE");

        this.actionBarSeparator = getScoreboardConfig().getString("ACTION_BAR_CONFIG.SEPARATOR");
        this.actionBarStatsOnlySpawn = getScoreboardConfig().getBoolean("ACTION_BAR_CONFIG.STATS_ONLY_SPAWN");
    }

    @Override
    public String getTitle(Player player) {
        return Config.SCOREBOARD_TITLE;
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        GlobalCooldown globalCooldown = getInstance().getAbilityManager().getGlobalCooldown();
        Conquest conquest = getInstance().getConquestManager().getConquest();
        Deathban deathban = user.getDeathban();
        RebootTimer rebootTimer = getInstance().getTimerManager().getRebootTimer();
        Team atPlayer = null;
        int footer = (footerEnabled ? footerLines.size() : 0);
        int numberOfLines = (lastLineEnabled ? 2 : 1);

        if (headstaffEnabled && staffManager.isHeadStaffEnabled(player)) {
            // Si está en Head Staff Mode, ignoramos el Staff Mode normal
            boolean hvanished = staffManager.isHeadVanished(player);
            boolean vanished = staffManager.isVanished(player);

            for (String s : hModMode)
                lines.add(s
                        .replace("%vanished%", (vanished ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER))
                        .replace("%hvanish%", (hvanished ? Config.HEAD_STAFF_TRUE_PLACEHOLDER : Config.HEAD_STAFF_FALSE_PLACEHOLDER))
                        .replace("%staffbuild%", staffManager.isStaffBuild(player) ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER)
                        .replace("%hidestaff%", staffManager.isHideStaff(player) ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER)
                        .replace("%players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("%staff%", String.valueOf(staffManager.getStaffMembers().size()))
                        .replace("%tps%", getInstance().getVersionManager().getVersion().getTPSColored())
                );

        } else if (staffEnabled) {
            // Solo entrará aquí si NO está en Head Staff Mode
            boolean vanished = staffManager.isVanished(player);
            boolean headvanish = staffManager.isHeadVanished(player);
            boolean staffActive = staffManager.isStaffEnabled(player);

            if (vanished && !staffActive) {
                for (String s : noModMode)
                    lines.add(s
                            .replace("%vanished%", Config.STAFF_TRUE_PLACEHOLDER)
                            .replace("%hvanish%", Config.HEAD_STAFF_TRUE_PLACEHOLDER)
                    );

            } else if (staffActive) {
                for (String s : modMode)
                    lines.add(s
                            .replace("%vanished%", (vanished ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER))
                            .replace("%hvanish%", (headvanish ? Config.HEAD_STAFF_TRUE_PLACEHOLDER : Config.HEAD_STAFF_FALSE_PLACEHOLDER))
                            .replace("%staffbuild%", staffManager.isStaffBuild(player) ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER)
                            .replace("%hidestaff%", staffManager.isHideStaff(player) ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER)
                            .replace("%players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                            .replace("%staff%", String.valueOf(staffManager.getStaffMembers().size()))
                            .replace("%tps%", getInstance().getVersionManager().getVersion().getTPSColored())
                    );
            }
        }


        if (pt != null && antiCleanLines != null && !antiCleanLines.isEmpty()) {
            AntiCleanTask task = pt.getAntiCleanTask();

            if (task != null && task.getRemaining() > 0L) {

                for (String s : antiCleanLines) {
                    lines.add(s
                            .replace("%team%", getInstance().getTimerManager().getAntiCleanTimer().getTeam(player))
                            .replace("%rem%", Formatter.getRemaining(task.getRemaining(), false))
                    );
                }
            }
        }

        if (deathban != null) {
            if (linesEnabled) lines.add(line);

            lines.add(deathbanInfo + Formatter.getRemaining(deathban.getTime(), false));
            lines.add(deathbanLives + user.getLives());

            if (footerEnabled) {
                for (String s : footerLines)
                    lines.add(s
                            .replace("%footer%", getManager().getFooter().getCurrent())
                    );
            }

            if (linesEnabled) lines.add(line);
            return getInstance().getPlaceholderHook().replace(player, lines);
        }

        if (!user.isScoreboard()) return null;

        if (claimEnabled && user.isScoreboardClaim()) {
            atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

            for (String s : claim)
                lines.add(s
                        .replace("%claim%", atPlayer.getDisplayName(player))
                );
        }

        if (rebootTimer.isActive() && rebootTimer.getScoreboard() != null) {
            lines.add(rebootTimer.getScoreboard() + Formatter.getRemaining(rebootTimer.getTask().getRemaining(), false));
        }

        if (getInstance().isKits()) {
            boolean add = false;

            if (kitsInfoOnlySpawn) {
                if (atPlayer == null) {
                    atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
                }

                if (atPlayer.getType() == TeamType.SAFEZONE) {
                    add = true;
                }

            } else {
                add = true;
            }

            if (add) {
                for (String s : kitsLines) {
                    if (onlyPositiveKillstreak && s.contains("%killstreak%") && user.getKillstreak() <= 0) {
                        continue;
                    }

                    lines.add(s
                            .replace("%kills%", String.valueOf(user.getKills()))
                            .replace("%deaths%", String.valueOf(user.getDeaths()))
                            .replace("%killstreak%", String.valueOf(user.getKillstreak()))
                            .replace("%balance%", String.valueOf(user.getBalance()))
                            .replace("%kdr%", user.getKDRString())
                    );
                }
            }
        }
        if (getInstance().isSoup()) {
            boolean add = false;

            if (SoupInfoOnlySpawn) {
                if (atPlayer == null) {
                    atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
                }

                if (atPlayer.getType() == TeamType.SAFEZONE) {
                    add = true;
                }

            } else {
                add = true;
            }

            if (add) {
                for (String s : soupLines) {
                    if (onlyPositiveKillstreak && s.contains("%killstreak%") && user.getKillstreak() <= 0) {
                        continue;
                    }

                    lines.add(s
                            .replace("%kills%", String.valueOf(user.getKills()))
                            .replace("%deaths%", String.valueOf(user.getDeaths()))
                            .replace("%killstreak%", String.valueOf(user.getKillstreak()))
                            .replace("%credits%", String.valueOf(user.getBalance()))
                            .replace("%kdr%", user.getKDRString())
                    );
                }
            }
        }

        if (kingEnabled && getInstance().getKingManager().isActive()) {
            Player king = getInstance().getKingManager().getKing();
            double currentHealth = king.getHealth() / 2;

            for (String s : kingLines)
                lines.add(s
                        .replace("%king%", king.getName())
                        .replace("%loc%", Utils.formatLocation(king.getLocation()))
                        .replace("%reward%", getInstance().getKingManager().getReward())
                        .replace("%health%", Formatter.formatHealth(currentHealth))
                );
        }

        if(boostEnabled && getInstance().getBoostManager().isActive()){

            for(String s : boostLines){
                lines.add(s
                        .replace("%multiplier%", String.valueOf(getInstance().getBoostManager().getMultiplier()))
                        .replace("%rem%", getInstance().getBoostManager().getRemainingString())
                );
            }
        }

        if(chaosEnabled && getInstance().getChaosManager().isActive()){

            for(String s : chaosLines){
                lines.add(s.replace("%rem%", getInstance().getChaosManager().getRemainingString()));
            }

        }

        if(payloadEnabled && getInstance().getPayloadManager().isActive()){

            for (String s : payloadLines) {
                lines.add(s
                        .replace("%rem%", getInstance().getPayloadManager().getRemainingString())
                        .replace("%distance%", getInstance().getPayloadManager().getRemainingDistance())
                        .replace("%speed%", getInstance().getPayloadManager().getPayloadSpeed())
                        .replace("%direction%", getInstance().getPayloadManager().getPayloadDirection())
                );

            }

        }

        if (dragonEnabled && getInstance().getDragonManager().isActive()) {
            int maxHealth = getInstance().getDragonManager().getMaxHealth();
            int currentHealth = getInstance().getDragonManager().getCurrentHealth();
            String remainingTime = getInstance().getDragonManager().getRemainingString();

            for (String s : dragonLines) {
                String line = s.replace("%rem%", remainingTime)
                        .replace("%max_health%", String.valueOf(maxHealth))
                        .replace("%current_health%", String.valueOf(currentHealth));
                lines.add(line);
            }
        }

        if (kothEnabled) {
            for (Koth activeKoth : getInstance().getKothManager().getKoths().values()) {
                if (!activeKoth.isActive()) continue;

                Cuboid capzone = activeKoth.getCaptureZone();

                for (String s : kothLines)
                    lines.add(s
                            .replace("%color%", activeKoth.getColor())
                            .replace("%koth%", activeKoth.getName())
                            .replace("%rem%", Formatter.getRemaining(activeKoth.getRemaining(), false))
                            .replace("%loc%", (capzone == null ? "None" : Utils.formatLocation(capzone.getCenter())))
                    );
            }
        }

        if (raidTimer != null) {
            if (atPlayer == null) {
                atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
            }

            AntiRaidTimer antiRaidTimer = getInstance().getTimerManager().getAntiRaidTimer();
            PlayerTeam checkRaidable;

            if (atPlayer instanceof PlayerTeam && !antiRaidTimer.canBreak((checkRaidable = (PlayerTeam) atPlayer), pt)) {
                lines.add(raidTimer + antiRaidTimer.getRemainingString(checkRaidable, pt));
            }
        }

        if (sotwManager.isActive()) {
            if (sotwManager.isEnabled(player)) {
                if (sotwOff != null) lines.add(sotwOff + sotwManager.getRemainingString());

            } else {
                if (sotw != null) lines.add(sotw + sotwManager.getRemainingString());
            }
        }

        AppleTimer appleTimer = getInstance().getTimerManager().getAppleTimer();
        World.Environment environment = player.getWorld().getEnvironment();

        if (appleTimer.isLimited(environment)) {
            if (appleLimit != null)
                lines.add(appleLimit
                        .replace("%amount%", String.valueOf(appleTimer.getLimit(player)))
                        .replace("%max-amount%", String.valueOf(appleTimer.getMaxLimit(environment)))
                );
        }

        for (CustomTimer timer : getInstance().getTimerManager().getCustomTimers().values()) {
            if (timer.getName().equals("EOTW")) {
                if (eotw != null) lines.add(eotw + timer.getRemainingString());
                continue;
            }

            if (timer.getName().equals("Purge")) {
                if (purge != null) lines.add(purge + timer.getRemainingString());
                continue;
            }

            if (timer.getName().equals("DTC")) {
                if (dtc != null) lines.add(dtc + timer.getRemainingString());
                continue;
            }

            lines.add(customTimerFormat
                    .replace("%displayName%", timer.getDisplayName()) + timer.getRemainingString()
            );
        }

        PvPClass activeClass = getInstance().getClassManager().getActiveClasses().get(player.getUniqueId());

        if (activeClass != null) {
            if (className != null) lines.add(className + activeClass.getName());

            if (activeClass instanceof BardClass) {
                BardClass bardClass = (BardClass) activeClass;

                if (bardEnergy != null) {
                    lines.add(bardEnergy + Formatter.formatBardEnergy(bardClass.getEnergyCooldown(player).getEnergy()));
                }

            } else if (activeClass instanceof MageClass) {
                MageClass mageClass = (MageClass) activeClass;

                if (mageEnergy != null) {
                    lines.add(mageEnergy + Formatter.formatBardEnergy(mageClass.getEnergyCooldown(player).getEnergy()));
                }

            } else if (activeClass instanceof MinerClass) {
                MinerClass minerClass = (MinerClass) activeClass;

                if (minerInvis != null) {
                    lines.add(minerInvis + (minerClass.getInvisible().contains(player.getUniqueId()) ? "true" : "false"));
                }

                if (minerDiamonds != null) {
                    lines.add(minerDiamonds + user.getDiamonds());
                }

            } else if (activeClass instanceof GhostClass) {
                GhostClass ghostClass = (GhostClass) activeClass;
                GhostData data = ghostClass.getData().get(player.getUniqueId());

                if (ghostMode != null) {
                    lines.add(ghostMode + data.getMode());
                }
            } else if (activeClass instanceof BomberClass) {
                BomberClass bomberClass = (BomberClass) activeClass;

//                if (bomberEnergy != null) {
//                    lines.add(bomberEnergy + Formatter.formatBomberEnergy(bomberClass.getEnergyCooldown(player).getEnergy()));
//                }
            }

            // basically all your speeds, jump
            for (CustomCooldown customCooldown : activeClass.getCustomCooldowns()) {
                String name = customCooldown.getDisplayName();

                if (name == null) continue;
                if (!customCooldown.hasCooldown(player)) continue;

                lines.add(name + customCooldown.getRemainingOld(player));
            }
        }

        if (globalCooldown.hasTimer(player)) {
            if (globalAbilities != null) lines.add(globalAbilities + globalCooldown.getRemainingString(player));
        }

        for (PlayerTimer timer : getInstance().getTimerManager().getPlayerTimers().values()) {
            if (timer instanceof GlobalCooldown) continue;

            String name = timer.getScoreboard();

            if (!timer.hasTimer(player)) continue;
            if (name == null) continue;

            // These need to be replaced, so handle differently
            if (timer instanceof AbilityTimer) {
                AbilityTimer abilityTimer = (AbilityTimer) timer;
                lines.add(name.replace("%ability%", abilityTimer.getAbility().getDisplayName()) + timer.getRemainingStringBoard(player));
                continue;
            }

            lines.add(name + timer.getRemainingStringBoard(player));
        }

        lines.addAll(getInstance().getAbilitiesHook().getScoreboardLines(player));

        if (conquest.isActive()) {
            if (atPlayer == null) {
                atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
            }

            if (!conquestOnlyTeam || atPlayer.getType() == TeamType.CONQUEST) {
                Map<ConquestType, Capzone> conquests = conquest.getCapzones();

                for (String conquestLine : conquestLines)
                    lines.add(conquestLine
                            .replace("%points-1%", conquest.getPoints(1))
                            .replace("%points-2%", conquest.getPoints(2))
                            .replace("%points-3%", conquest.getPoints(3))
                            .replace("%red%", conquests.get(ConquestType.RED).getRemainingString())
                            .replace("%yellow%", conquests.get(ConquestType.YELLOW).getRemainingString())
                            .replace("%green%", conquests.get(ConquestType.GREEN).getRemainingString())
                            .replace("%blue%", conquests.get(ConquestType.BLUE).getRemainingString())
                    );
            }
        }

        if (focusEnabled && pt != null && pt.getFocus() != null) {
            Team focusedTeam = pt.getFocusedTeam();

            if (focusedTeam != null) {
                if (focusedTeam.getType() == TeamType.PLAYER) {
                    PlayerTeam focusPT = (PlayerTeam) focusedTeam;

                    for (String focusLine : focusLines)
                        lines.add(focusLine
                                .replace("%team%", focusPT.getName())
                                .replace("%hq%", focusPT.getHQFormatted())
                                .replace("%online%", String.valueOf(focusPT.getOnlinePlayersSize(false)))
                                .replace("%max-online%", String.valueOf(focusPT.getPlayers().size()))
                                .replace("%dtr-color%", focusPT.getDtrColor())
                                .replace("%dtr%", focusPT.getDtrString())
                                .replace("%dtr-symbol%", focusPT.getDtrSymbol())
                        );

                } else {
                    for (String s : focusSystemLines)
                        lines.add(s
                                .replace("%team%", focusedTeam.getName())
                                .replace("%hq%", focusedTeam.getHQFormatted())
                        );
                }
            }
        }

        if (footerEnabled) {
            for (String s : footerLines)
                lines.add(s
                        .replace("%footer%", getManager().getFooter().getCurrent())
                );
        }

        if (lines.isEmpty()) {
            return null;
        }

        if (linesEnabled) {
            List<String> clone = new ArrayList<>();

            if (!lines.get(0).equals(line)) {
                clone.add(line);
            }

            clone.addAll(lines);

            if (!lines.get(lines.size() - 1).equals(line) && lastLineEnabled) {
                clone.add(line);
            }

            lines = clone;

            if (lines.size() == numberOfLines + footer) return null;
        }

        if (!linesEnabled && footerEnabled && lines.size() == footerLines.size()) {
            return null;
        }

        if (lines.size() >= footer + numberOfLines) {
            int index = lines.size() - (footer + (lastLineEnabled ? 1 : 0));
            String doubleLine = lines.get(index - 1);

            if (index != lines.size() && doubleLine.equals(line)) {
                lines.remove(index - 1);
            }
        }

        return getInstance().getPlaceholderHook().replace(player, lines);
    }

    @Override
    public String getActionBar(Player player) {
        List<String> lines = new ArrayList<>();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        Team at = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        TimerManager timerManager = getInstance().getTimerManager();
        PvPClass pvpClass = getInstance().getClassManager().getActiveClass(player);

        if (ActionBarConfig.STATS.isEnabled()) {
            boolean add = (!actionBarStatsOnlySpawn || at instanceof SafezoneTeam);

            if (add) {
                User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
                lines.add(ActionBarConfig.STATS.getLine()
                        .replace("%kills%", String.valueOf(user.getKills()))
                        .replace("%deaths%", String.valueOf(user.getDeaths()))
                        .replace("%killstreak%", String.valueOf(user.getKillstreak()))
                );
            }
        }

        if (ActionBarConfig.REBOOT.isEnabled()) {
            RebootTimer rebootTimer = getInstance().getTimerManager().getRebootTimer();

            if (rebootTimer.isActive()) {
                lines.add(ActionBarConfig.REBOOT.getLine()
                        .replace("%time%", Formatter.getRemaining(rebootTimer.getTask().getRemaining(), false))
                );
            }
        }

        if (sotwManager.isActive()) {
            boolean enabled = sotwManager.isEnabled(player);

            if (enabled && ActionBarConfig.SOTW_TIMER_ENABLED.isEnabled()) {
                lines.add(ActionBarConfig.SOTW_TIMER_ENABLED.getLine()
                        .replace("%time%", sotwManager.getRemainingString())
                );

            } else if (!enabled && ActionBarConfig.SOTW_TIMER.isEnabled()) {
                lines.add(ActionBarConfig.SOTW_TIMER.getLine()
                        .replace("%time%", sotwManager.getRemainingString())
                );
            }
        }

        if (ActionBarConfig.CLAIM.isEnabled()) {
            lines.add(ActionBarConfig.CLAIM.getLine()
                    .replace("%claim%", at.getDisplayName(player))
            );
        }

        if (ActionBarConfig.ANTI_CLEAN.isEnabled() && pt != null) {
            AntiCleanTask task = pt.getAntiCleanTask();

            if (task != null && task.getRemaining() > 0L) {
                lines.add(ActionBarConfig.ANTI_CLEAN.getLine()
                        .replace("%time%", Formatter.getRemaining(task.getRemaining(), false))
                );
            }
        }

        if (pvpClass != null) {
            if (ActionBarConfig.CLASS_NAME.isEnabled()) {
                lines.add(ActionBarConfig.CLASS_NAME.getLine()
                        .replace("%class%", pvpClass.getName())
                );
            }

            if (pvpClass instanceof BardClass) {
                BardClass bardClass = (BardClass) pvpClass;

                if (ActionBarConfig.BARD_ENERGY.isEnabled()) {
                    lines.add(ActionBarConfig.BARD_ENERGY.getLine()
                            .replace("%energy%", Formatter.formatBardEnergy(bardClass.getEnergyCooldown(player).getEnergy()))
                    );
                }

                if (ActionBarConfig.BARD_EFFECT.isEnabled() && bardClass.getBardEffectCooldown().hasCooldown(player)) {
                    lines.add(ActionBarConfig.BARD_EFFECT.getLine()
                            .replace("%time%", bardClass.getBardEffectCooldown().getRemainingActionBar(player))
                    );
                }
            }

            if (pvpClass instanceof MageClass) {
                MageClass mageClass = (MageClass) pvpClass;

                if (ActionBarConfig.MAGE_ENERGY.isEnabled()) {
                    lines.add(ActionBarConfig.MAGE_ENERGY.getLine()
                            .replace("%energy%", Formatter.formatBardEnergy(mageClass.getEnergyCooldown(player).getEnergy()))
                    );
                }

                if (ActionBarConfig.MAGE_EFFECT.isEnabled() && mageClass.getMageEffectCooldown().hasCooldown(player)) {
                    lines.add(ActionBarConfig.MAGE_EFFECT.getLine()
                            .replace("%time%", mageClass.getMageEffectCooldown().getRemainingActionBar(player))
                    );
                }
            }
        }

        if (ActionBarConfig.CLASS_NAME.isEnabled() && pvpClass != null) {
            lines.add(ActionBarConfig.CLASS_NAME.getLine()
                    .replace("%class%", pvpClass.getName())
            );
        }

        for (PlayerTimer playerTimer : timerManager.getPlayerTimers().values()) {
            if (playerTimer.getActionBar() == null) continue;
            if (!playerTimer.getActionBar().isEnabled()) continue;
            if (!playerTimer.hasTimer(player)) continue;

            lines.add(playerTimer.getActionBar().getLine()
                    .replace("%time%", playerTimer.getRemainingActionBar(player))
            );
        }

        return String.join(actionBarSeparator, lines);
    }

    public String getString(String path) {
        String string = getScoreboardConfig().getString(path);
        return (string.isEmpty() ? null : string);
    }
}