package me.keano.azurite.modules.hooks.ranks.type;

import me.keano.azurite.modules.hooks.ranks.Rank;
import me.zowpy.core.api.CoreAPI;
import me.zowpy.core.api.profile.Profile;
import org.bukkit.entity.Player;

public class KupRank implements Rank {

    @Override
    public String getRankName(Player player) {
        Profile profile = CoreAPI.getInstance().getProfileManager().getByUUID(player.getUniqueId());
        return profile.getDisplayRank().getDisplayName();
    }

    @Override
    public String getRankPrefix(Player player) {
        Profile profile = CoreAPI.getInstance().getProfileManager().getByUUID(player.getUniqueId());
        return profile.getDisplayRank().getPrefix();
    }

    @Override
    public String getRankSuffix(Player player) {
        Profile profile = CoreAPI.getInstance().getProfileManager().getByUUID(player.getUniqueId());
        return profile.getDisplayRank().getSuffix();
    }

    @Override
    public String getRankColor(Player player) {
        Profile profile = CoreAPI.getInstance().getProfileManager().getByUUID(player.getUniqueId());
        return profile.getDisplayRank().getDisplayColor();
    }
}
