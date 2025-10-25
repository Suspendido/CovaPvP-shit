package me.keano.azurite.modules.hooks.ranks.type;

import me.keano.azurite.modules.hooks.ranks.Rank;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsRank implements Rank {

    private LuckPerms luckPerms;

    public LuckPermsRank() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
    }
    private User getUser(Player player) {
        return luckPerms.getUserManager().getUser(player.getUniqueId());
    }

    @Override
    public String getRankName(Player player) {
        User user = getUser(player);
        if (user == null) {
            return "Default";
        }
        return user.getPrimaryGroup();
    }

    @Override
    public String getRankPrefix(Player player) {
        User user = getUser(player);
        if (user == null) {
            return "";
        }
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Override
    public String getRankSuffix(Player player) {
        User user = getUser(player);
        if (user == null) {
            return "";
        }
        return user.getCachedData().getMetaData().getSuffix();
    }

    @Override
    public String getRankColor(Player player) {
        User user = getUser(player);
        if (user == null) {
            return "";
        }
        String prefix = user.getCachedData().getMetaData().getPrefix();
        if (prefix != null && prefix.length() > 0) {
            if (prefix.startsWith("&") && prefix.length() > 1) {
                return prefix.substring(0, 2);
            }
        }
        return (prefix);
    }
}