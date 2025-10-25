package me.keano.azurite.modules.hooks.ranks.type;

import me.activated.core.plugin.AquaCoreAPI;
import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;

public class AquaCoreRank implements Rank {

    @Override
    public String getRankName(Player player) {
        return AquaCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getName();
    }

    @Override
    public String getRankPrefix(Player player) {
        return AquaCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getPrefix();
    }

    @Override
    public String getRankSuffix(Player player) {
        return AquaCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getSuffix();
    }

    @Override
    public String getRankColor(Player player) {
        return AquaCoreAPI.INSTANCE.getPlayerRank(player.getUniqueId()).getColor().toString();
    }
}