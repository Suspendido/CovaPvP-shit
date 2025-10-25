package me.keano.azurite.modules.pvpclass.type.archer.upgrades;

import com.google.common.collect.Maps;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.pvpclass.type.archer.upgrades.archers.Poison;
import me.keano.azurite.modules.pvpclass.type.archer.upgrades.archers.Slowness;
import me.keano.azurite.modules.pvpclass.type.archer.upgrades.archers.Wither;
import me.keano.azurite.utils.CC;
import org.bukkit.entity.Player;

import java.util.Map;

public class ArcherUpgradeMenu extends Menu {
    public ArcherUpgradeMenu(MenuManager manager, Player player) {
        super(manager, player, CC.t("&5Archer Upgrades"), 27, false);

        this.fillEnabled = true;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        buttons.put(12, new Poison());
        buttons.put(14, new Wither());
        buttons.put(16, new Slowness());

        return buttons;
    }
}
