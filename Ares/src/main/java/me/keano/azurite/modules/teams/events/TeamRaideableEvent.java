package me.keano.azurite.modules.teams.events;

import lombok.Getter;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class TeamRaideableEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final PlayerTeam team;

    public TeamRaideableEvent(Player player, PlayerTeam team) {
        this.player = player;
        this.team = team;

        Bukkit.getPluginManager().callEvent(this);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
