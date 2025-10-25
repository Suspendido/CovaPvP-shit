package me.keano.azurite.modules.events.boost.listener;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.boost.BoostManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.type.PlayerTeam;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoostListener extends Module<BoostManager> {

    private final Map<UUID, PlayerTeam> playerTeams;
    private final BoostManager boostManager;

    public BoostListener(BoostManager manager, HCF instance) {
        super(manager);
        this.boostManager = manager;
        this.playerTeams = new ConcurrentHashMap<>();
    }

}
