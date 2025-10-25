package me.keano.azurite.utils.deco;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class ApolloTags extends Module<ListenerManager> {

    private final HCF hcf;

    public ApolloTags(ListenerManager manager, HCF hcf) {
        super(manager);
        this.hcf = hcf;


        this.load();
    }

    private void load() {

    }

    @EventHandler
    public void onJoinCheckPvPTimer(PlayerJoinEvent event){

        Player player = event.getPlayer();

        if(hcf.getTimerManager().getPvpTimer().hasTimer(player)){



        }

    }

}

