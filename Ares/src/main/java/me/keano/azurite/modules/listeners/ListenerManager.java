package me.keano.azurite.modules.listeners;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.bounty.listener.BountyListener;
import me.keano.azurite.modules.customitems.CustomItemManager;
import me.keano.azurite.modules.customitems.listener.CustomItemListener;
import me.keano.azurite.modules.deathban.listener.DeathbanListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.listeners.type.*;
import me.keano.azurite.modules.listeners.type.team.PlayerTeamListener;
import me.keano.azurite.modules.listeners.type.team.RaidListener;
import me.keano.azurite.modules.listeners.type.team.TeamListener;
import me.keano.azurite.modules.payouts.menu.PayoutsMenu;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.deco.listeners.DisableListener;
import me.keano.azurite.utils.deco.listeners.LoggerListener;
import me.keano.azurite.utils.deco.listeners.PlayerListener;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ListenerManager extends Manager {

    private final List<Listener> listeners;
    private final List<BukkitTask> tasks;

    public ListenerManager(HCF instance) {
        super(instance);
        this.listeners = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.load();
    }

    private void load() {
        listeners.addAll(Arrays.asList(
                new MainListener(this),
                new ChatListener(this),
                new DeathListener(this),
                new FixListener(this),
                new BorderListener(this),
                new GlitchListener(this),
                new WorldListener(this),
                new DiamondListener(this),
                new DropListener(this),
                new StackListener(this),
                new LimiterListener(this),
                new StrengthListener(this),
                new CrowbarListener(this),
                new SoupListener(this, this.getInstance()),
                new DisableListener(this, this.getInstance()),
                new PlayerListener(this, this.getInstance()),
                new LoggerListener(this, this.getInstance()),
                new RaidListener(this),
                new SignListener(this),
                new PayoutsMenu(this, this.getInstance()),

                new CustomItemListener(this.getInstance().getCustomItemManager()),

                new CustomItemManager(this.getInstance()),

                // Team Listeners
                new TeamListener(getInstance().getTeamManager(), getInstance()),
                new PlayerTeamListener(getInstance().getTeamManager()),

                // Bounty Listener
                new BountyListener(getInstance().getBountyManager())
        ));

        if (Utils.isModernVer()) {
            listeners.add(new PortalListener(this));
        } else {
            listeners.add(new SmeltListener(this));
            listeners.add(new PortalLegacyListener(this));
        }
    }

    @Override
    public void reload() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }

        Utils.iterate(tasks, (task) -> {
            task.cancel();
            return true;
        });

        this.load();
    }
}