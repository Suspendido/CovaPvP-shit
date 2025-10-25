package me.keano.azurite.modules.teams.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 27/01/2025
 * Project: ZeusHCF
 */

public class DTCTeam extends Team {

    public DTCTeam(TeamManager manager, Map<String, Object> map) {

        super(
                manager,
                map,
                true,
                TeamType.DTC
        );
    }

    public DTCTeam(TeamManager manager, String name) {
        super(
                manager,
                name,
                UUID.randomUUID(),
                true,
                TeamType.DTC
        );
    }

    @Override
    public String getDisplayName(Player player) {
        return Config.DISPLAY_NAME_DTC.replace("%team%", super.getDisplayName(player));
    }

}
