package me.keano.azurite.utils;

import com.cryptomorin.xseries.XSound;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class PlayerUtil {

    public void playSound(Player player, String sound) {
        if (!sound.isEmpty()) {
            player.playSound(player.getLocation(), XSound.matchXSound(sound).get().parseSound(), 1.0F, 1.0F);
        }
    }
}