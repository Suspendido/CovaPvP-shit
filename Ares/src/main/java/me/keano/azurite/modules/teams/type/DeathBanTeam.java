package me.keano.azurite.modules.teams.type;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class DeathBanTeam extends Team {

    public DeathBanTeam(TeamManager manager) {
        super(
                manager,
                "DeathBan",
                UUID.randomUUID(),
                true,
                TeamType.DEATHBAN
        );
    }

    @Override
    public String getDisplayName(Player player) {
        return Config.DISPLAY_NAME_DEATHBAN.replace("%team%", super.getDisplayName(player));
    }

    public boolean canInteract(Location location) {
        Block block = location.getBlock();
        return block.getType() == ItemUtils.getMat("WOOD_PLATE") || block.getType() == ItemUtils.getMat("STONE_PLATE") ||
                block.getType().name().contains("FENCE_GATE");
    }

}