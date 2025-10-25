package me.keano.azurite.modules.events.dtc;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.dtc.listener.DTCListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.timers.type.CustomTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 27/01/2025
 * Project: ZeusHCF
 */

public class DTCManager extends Manager {

    private final List<String> blocks;
    private String dtcBlock;
    private int blockHealth;

    private int dtcX;
    private int dtcY;
    private int dtcZ;
    private String dtcWorld;

    public DTCManager(HCF instance) {
        super(instance);
        this.blocks = getConfig().getStringList("DTC_EVENT.BLOCKS");
        this.dtcBlock = getConfig().getString("DTC_EVENT.BLOCK");
        this.blockHealth = getConfig().getInt("DTC_EVENT.HEALTH");

        this.dtcX = getInstance().getConfig().getInt("DTC_EVENT.LOCATION.X");
        this.dtcY = getInstance().getConfig().getInt("DTC_EVENT.LOCATION.Y");
        this.dtcZ = getInstance().getConfig().getInt("DTC_EVENT.LOCATION.Z");
        this.dtcWorld = getInstance().getConfig().getString("DTC_EVENT.LOCATION.world");
        this.load();

        new DTCListener(this);

        loadCoordsFromConfig();
    }

    public void load(){
        loadCoordsFromConfig();
    }

    public void reload() {
        blocks.clear();
        blocks.addAll(getConfig().getStringList("DTC_EVENT.BLOCKS"));
        this.dtcBlock = getConfig().getString("DTC_EVENT.BLOCK");
        loadCoordsFromConfig();
    }

    public void start(long time) {
        new CustomTimer(getInstance().getTimerManager(), "DTC", "DTC", time);

        if (dtcWorld == null) {
            getInstance().getLogger().warning("DTC Coords are not configured");
            return;
        }

        World world = Bukkit.getWorld(dtcWorld);
        if (world == null) {
            getInstance().getLogger().warning("World '" + dtcWorld + "' was not found.");
            return;
        }

        Block targetBlock = world.getBlockAt(dtcX, dtcY, dtcZ);
        targetBlock.setType(Material.OBSIDIAN);

        setBlockHealth(350);
    }

    public void setBlockHealth(int health) {
        this.blockHealth = health;

        getConfig().set("DTC_EVENT.HEALTH", health);
        getInstance().saveConfig();
    }

    public boolean isActive() {
        return getInstance().getTimerManager().getCustomTimer("DTC") != null;
    }

    public String getDTCBlock() {
        return this.dtcBlock;
    }

    public Material getDTCMaterial() {
        return Material.matchMaterial(this.dtcBlock);
    }

    public void setDTCBlock(String block) {
        this.dtcBlock = block;
        getConfig().set("DTC_EVENT.BLOCK", block);
        getConfig().save();
    }

    public int getBlockHealth() {
        return blockHealth;

    }

    public void setDtcCoords(Location loc) {
        this.dtcX = loc.getBlockX();
        this.dtcY = loc.getBlockY();
        this.dtcZ = loc.getBlockZ();
        this.dtcWorld = loc.getWorld().getName();

        getInstance().getConfig().set("DTC_EVENT.LOCATION.X", dtcX);
        getInstance().getConfig().set("DTC_EVENT.LOCATION.Y", dtcY);
        getInstance().getConfig().set("DTC_EVENT.LOCATION.Z", dtcZ);
        getInstance().getConfig().set("DTC_EVENT.LOCATION.world", dtcWorld);
        getInstance().saveConfig();
    }

    public void loadCoordsFromConfig() {
        dtcX = getInstance().getConfig().getInt("DTC_EVENT.LOCATION.X");
        dtcY = getInstance().getConfig().getInt("DTC_EVENT.LOCATION.Y");
        dtcZ = getInstance().getConfig().getInt("DTC_EVENT.LOCATION.Z");
        dtcWorld = getInstance().getConfig().getString("DTC_EVENT.LOCATION.world");
    }
}
