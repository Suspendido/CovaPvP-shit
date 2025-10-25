package me.keano.azurite.modules.bounty.menu;

import me.keano.azurite.modules.bounty.BountyManager;
import me.keano.azurite.modules.bounty.menu.buttons.BountyTargetButton;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BountyMenu extends PaginatedMenu {

    private final BountyManager bountyManager;

    public BountyMenu(MenuManager manager, Player player) {
        super(manager, player, CC.t("&eBounty Menu"), 36, false);

        this.fillEnabled = true;
        this.bountyManager = getInstance().getBountyManager();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();
        int i = 1;
        List<Player> onlineBounty = bountyManager.getBounties().keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (Player bounty : onlineBounty) {
            map.put(i, new BountyTargetButton(bountyManager, player, bounty, bountyManager.getBounties().get(bounty.getUniqueId())));
            i++;
        }

        return map;
    }
}