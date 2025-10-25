package me.keano.azurite.modules.users;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.modules.users.settings.ActionBar;
import me.keano.azurite.modules.users.settings.TeamChatSetting;
import me.keano.azurite.modules.users.settings.TeamListSetting;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.configs.StorageJson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class User extends Module<UserManager> {

    private UUID uniqueID;
    private UUID replied;
    private String name;
    private String killtag;
    private Deathban deathban;

    private TeamListSetting teamListSetting;
    private TeamChatSetting teamChatSetting;
    private StorageJson storageJson;
    private ActionBar actionBar;

    private Set<UUID> ignoring;
    private List<StoredInventory> inventories;
    private List<String> lastKills;
    private List<String> lastDeaths;

    private int balance;
    private int kills;
    private int deaths;
    private int diamonds;
    private int lives;
    private int killstreak;
    private int highestKillstreak;

    private int falltrapTokens;
    private int baseTokens;

    private long playtime;
    private long lastLogin;
    private long dailyCooldown;

    private boolean privateMessages;
    private boolean privateMessagesSound;
    private boolean reclaimed;
    private boolean redeemed;

    private boolean scoreboard;
    private boolean scoreboardClaim;
    private boolean publicChat;
    private boolean cobblePickup;
    private boolean foundDiamondAlerts;
    private boolean claimsShown;
    private boolean clearedNametags;
    private boolean lunarNametags;
    private boolean deathMessages;
    private Map<String, Integer> modModeSlots;

    // For deserialization
    public User(UserManager manager, Map<String, Object> map) {
        super(manager);

        this.uniqueID = UUID.fromString(String.valueOf(map.get("uniqueID")));
        this.name = String.valueOf(map.get("name"));
        this.replied = null;
        this.teamListSetting = TeamListSetting.valueOf(String.valueOf(map.get("listSetting")));
        this.teamChatSetting = TeamChatSetting.valueOf(String.valueOf(map.get("chatSetting")));
        this.storageJson = null;

        this.ignoring = Serializer.fetchUUIDs(map.get("ignoring"));
        this.inventories = Serializer.fetchInventories(map.get("inventories"));
        this.lastKills = Utils.createList(map.get("lastKills"), String.class);
        this.lastDeaths = Utils.createList(map.get("lastDeaths"), String.class);

        this.balance = coerceInt(map.get("balance"), 0);
        this.kills = coerceInt(map.get("kills"), 0);
        this.deaths = coerceInt(map.get("deaths"), 0);
        this.diamonds = coerceInt(map.get("diamonds"), 0);
        this.lives = coerceInt(map.get("lives"), 0);
        this.killstreak = coerceInt(map.get("killstreak"), 0);
        this.highestKillstreak = coerceInt(map.get("highestKillstreak"), 0);
        this.reclaimed = coerceBool(map.get("reclaimed"), false);
        this.redeemed = coerceBool(map.get("redeemed"), false);
        this.falltrapTokens = coerceInt(map.get("falltrapTokens"), 0);
        this.baseTokens = coerceInt(map.get("baseTokens"), 0);
        this.playtime = coerceLong(map.get("playtime"), 0L);
        this.dailyCooldown = coerceLong(map.get("lastDaily"), 0L);
        this.scoreboardClaim = coerceBool(map.get("scoreboardClaim"), Config.DEFAULT_CLAIM_SCOREBOARD);
        this.scoreboard = coerceBool(map.get("scoreboard"), true);
        this.publicChat = coerceBool(map.get("publicChat"), true);
        this.cobblePickup = coerceBool(map.get("cobblePickup"), true);
        this.foundDiamondAlerts = coerceBool(map.get("foundDiamondAlerts"), true);
        this.deathMessages = coerceBool(map.get("deathMessages"), Config.DEFAULT_DEATH_MESSAGES);
        this.lunarNametags = coerceBool(map.get("lunarNametags"), true);

        this.modModeSlots = new HashMap<>();
        if (map.containsKey("modModeSlots") && map.get("modModeSlots") instanceof Map) {
            Map<String, Object> stored = (Map<String, Object>) map.get("modModeSlots");
            for (Map.Entry<String, Object> entry : stored.entrySet()) {
                this.modModeSlots.put(entry.getKey(), coerceInt(entry.getValue(), 0));
            }
        }

        this.lastLogin = 0L;
        this.privateMessages = coerceBool(map.get("privateMessages"), true);
        this.privateMessagesSound = coerceBool(map.get("privateMessagesSound"), true);
        this.claimsShown = coerceBool(map.getOrDefault("claimsShown", false), false);
        this.clearedNametags = coerceBool(map.getOrDefault("clearedNametags", false), false);

        if (map.containsKey("deathban") && map.get("deathban") != null) {
            this.deathban = Serializer.deserializeDeathban(getInstance().getDeathbanManager(), String.valueOf(map.get("deathban")));
        }
        if (map.containsKey("killtag") && map.get("killtag") != null) {
            this.killtag = String.valueOf(map.get("killtag"));
        }

        manager.getUsers().put(uniqueID, this);
        manager.getUuidCache().put(name, uniqueID);
    }

    public User(UserManager manager, UUID uniqueID, String name) {
        super(manager);

        this.uniqueID = uniqueID;
        this.name = name;
        this.replied = null;
        this.deathban = null;
        this.killtag = null;

        this.teamListSetting = TeamListSetting.ONLINE_HIGH;
        this.teamChatSetting = TeamChatSetting.PUBLIC;
        this.storageJson = null;

        this.ignoring = new HashSet<>();
        this.inventories = new ArrayList<>();
        this.lastKills = new ArrayList<>();
        this.lastDeaths = new ArrayList<>();

        this.balance = 0;
        this.kills = 0;
        this.deaths = 0;
        this.diamonds = 0;
        this.lives = 0;
        this.killstreak = 0;
        this.highestKillstreak = 0;

        this.falltrapTokens = 0;
        this.baseTokens = 0;

        this.playtime = 0L;
        this.lastLogin = 0L;
        this.dailyCooldown = 0L;

        this.privateMessages = true;
        this.privateMessagesSound = true;
        this.reclaimed = false;
        this.redeemed = false;

        this.scoreboardClaim = Config.DEFAULT_CLAIM_SCOREBOARD;
        this.scoreboard = true;
        this.publicChat = true;
        this.cobblePickup = true;
        this.foundDiamondAlerts = true;
        this.claimsShown = false;
        this.lunarNametags = true;
        this.deathMessages = Config.DEFAULT_DEATH_MESSAGES;
        this.modModeSlots = new HashMap<>();

        manager.getUsers().put(uniqueID, this);
        manager.getUuidCache().put(name, uniqueID);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>(); // keep order

        map.put("name", name);
        map.put("uniqueID", uniqueID.toString());
        map.put("listSetting", teamListSetting.toString());
        map.put("chatSetting", teamChatSetting.toString());
        map.put("ignoring", Serializer.serializeUUIDs(ignoring));
        map.put("lastKills", lastKills);
        map.put("lastDeaths", lastDeaths);
        map.put("balance", String.valueOf(balance));
        map.put("kills", String.valueOf(kills));
        map.put("deaths", String.valueOf(deaths));
        map.put("diamonds", String.valueOf(diamonds));
        map.put("lives", String.valueOf(lives));
        map.put("killstreak", String.valueOf(killstreak));
        map.put("highestKillstreak", String.valueOf(highestKillstreak));
        map.put("falltrapTokens", String.valueOf(falltrapTokens));
        map.put("baseTokens", String.valueOf(baseTokens));
        map.put("playtime", String.valueOf(getUpdatedPlaytime()));
        map.put("lastDaily", String.valueOf(dailyCooldown));
        map.put("reclaimed", String.valueOf(reclaimed));
        map.put("redeemed", String.valueOf(redeemed));
        map.put("scoreboardClaim", String.valueOf(scoreboardClaim));
        map.put("scoreboard", String.valueOf(scoreboard));
        map.put("publicChat", String.valueOf(publicChat));
        map.put("cobblePickup", String.valueOf(cobblePickup));
        map.put("foundDiamondAlerts", String.valueOf(foundDiamondAlerts));
        map.put("inventories", Serializer.serializeInventories(new ArrayList<>(inventories)));
        map.put("lunarNametags", String.valueOf(lunarNametags));
        map.put("deathMessages", String.valueOf(deathMessages));

        Map<String, String> serialModSlots = new LinkedHashMap<>();
        if (modModeSlots != null) {
            for (Map.Entry<String, Integer> e : modModeSlots.entrySet()) {
                serialModSlots.put(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        map.put("modModeSlots", serialModSlots);

        if (deathban != null) map.put("deathban", Serializer.serializeDeathban(deathban));
        if (killtag != null) map.put("killtag", killtag);

        return map;
    }

    public double getKDR() {
        double kdr = (double) kills / (double) deaths;
        return (Double.isNaN(kdr) || Double.isInfinite(kdr) ? 0.0D : kdr);
    }

    public String getName() {
        Player player = Bukkit.getPlayer(uniqueID);
        if (player != null) {
            return player.getName(); // Support disguise systems
        }
        return (name == null ? "Null User" : name);
    }

    public String getKDRString() {
        return Formatter.formatKDR(getKDR());
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueID);
    }

    public long getUpdatedPlaytime() {
        // This won't actually update playtime but get the updated time
        return playtime + (lastLogin > 0L ? System.currentTimeMillis() - lastLogin : 0L);
    }

    public void updatePlaytime() {
        this.setPlaytime(getUpdatedPlaytime());
        this.setLastLogin(System.currentTimeMillis());
    }

    public void save() {
        getInstance().getStorageManager().getStorage().saveUser(this, true);
    }

    public void delete() {
        getInstance().getStorageManager().getStorage().deleteUser(this);
    }

    private static int coerceInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(s);
                return (int) Math.floor(d);
            } catch (NumberFormatException ex) {
                return def;
            }
        }
    }

    private static long coerceLong(Object o, long def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).longValue();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return def;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(s);
                return (long) Math.floor(d);
            } catch (NumberFormatException ex) {
                return def;
            }
        }
    }

    private static boolean coerceBool(Object o, boolean def) {
        if (o == null) return def;
        if (o instanceof Boolean) return (Boolean) o;
        String s = String.valueOf(o).trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return def;
        if (s.equals("true") || s.equals("t") || s.equals("yes") || s.equals("y") || s.equals("1")) return true;
        if (s.equals("false") || s.equals("f") || s.equals("no") || s.equals("n") || s.equals("0")) return false;
        return def;
    }
}
