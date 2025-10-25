package me.keano.azurite.modules.ability;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.ability.extra.GlobalCooldown;
import me.keano.azurite.modules.ability.listener.AbilityListener;
import me.keano.azurite.modules.ability.type.*;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Utils;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AbilityManager extends Manager {

    private final Map<String, Ability> abilities;
    private final List<BukkitTask> tasks;
    private final GlobalCooldown globalCooldown;

    public AbilityManager(HCF instance) {
        super(instance);

        this.abilities = new HashMap<>();
        this.tasks = new ArrayList<>();
        this.globalCooldown = new GlobalCooldown(instance.getTimerManager());

        new AbilityListener(this);
        this.load();
    }

    @Override
    public void disable() {
        PortableBardAbility ability = (PortableBardAbility) getAbility("PortableBard");
        ability.disable();
    }

    @Override
    public void reload() {
        for (Ability ability : abilities.values()) {
            HandlerList.unregisterAll(ability);
        }

        Utils.iterate(tasks, (task) -> {
            task.cancel();
            return true;
        });

        abilities.clear();
        this.load();
    }

    private void load() {
        new SwitcherAbility(this);
        new AntiBuildAbility(this);
        new TimeWarpAbility(this);
        new PocketBardAbility(this);
        new InvisibilityAbility(this);
        new PortableArcherAbility(this);
        new LightningAbility(this);
        new RageBallAbility(this);
        new CraftingChaosAbility(this);
        new EffectStealerAbility(this);
        new ComboAbility(this);
        new FocusModeAbility(this);
        new LuckyModeAbility(this);
        new NinjaAbility(this);
        new InverseNinjaAbility(this);
        new BerserkAbility(this);
        new CloseCallAbility(this);
        new SwitchStickAbility(this);
        new TeleportEyeAbility(this);
        new SamuraiAbility(this);
        new ScramblerAbility(this);
        new MagicRockAbility(this);
        new WebTrailAbility(this);
        new AntiTrapStarAbility(this);
        new MedKitAbility(this);
        new RocketAbility(this);
        new AntiTrapBeaconAbility(this);
        new AntiPearlAbility(this);
        new PotionCounterAbility(this);
        new UltimateAbility(this);
        new GrappleAbility(this);
        new GuardianAngelAbility(this);
        new HelmetRemoverAbility(this);
        new HookAbility(this);
        new KnifeAbility(this);
        new LifeStealAbility(this);
        new TankIngotAbility(this);
        new TeleportBowAbility(this);
        new ReflectorAbility(this);
        new FreezerAbility(this);
        new ArmorDecayAbility(this);
        new ExplosiveEggAbility(this);
        new PortableBardAbility(this);
        new PortableRogueAbility(this);
        new AntiTrapHaloAbility(this);
        new AbilityDisablerAbility(this);
        new XRayModeAbility(this);
    }

    public Ability getAbility(String name) {
        return abilities.get(name.toUpperCase());
    }
}