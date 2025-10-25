package me.keano.azurite.modules.listeners.type.team;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.teams.events.TeamRaideableEvent;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class RaidListener extends Module<ListenerManager> implements Listener {

    private boolean factionRaidEnabled;
    private List<FancyMessage> factionRaidMessage;

    public RaidListener(ListenerManager manager) {
        super(manager);

        this.load();
    }

    public void load() {
        factionRaidEnabled = getLanguageConfig().getBoolean("RAID_ANNOUNCER.ENABLED");
        factionRaidMessage = Serializer.loadFancyMessages(getLanguageConfig().getStringList("RAID_ANNOUNCER.MESSAGE"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeathRaid(TeamRaideableEvent event) {
        Player player = event.getPlayer();
        PlayerTeam playerTeam = event.getTeam();

        if (!factionRaidEnabled) return;

        factionRaidMessage = factionRaidMessage.stream().map(FancyMessage::clone).collect(Collectors.toList());

        UnaryOperator<String> replacer = s -> s
                .replace("%player%", player.getName())
                .replace("%faction%", playerTeam.getName())
                .replace("%dtr%", String.format("%.2f", playerTeam.getDtr()));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            for (FancyMessage fancyMessage : factionRaidMessage) {
                fancyMessage.send(onlinePlayer, replacer);
            }
        }
    }
}
