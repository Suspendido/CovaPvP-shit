package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.pvpclass.type.archer.upgrades.ArcherUpgradeMenu;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArcherUpgradesCommand extends Command {

    public ArcherUpgradesCommand(CommandManager manager) {
        super(manager, "archerupgrades");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.t("&cOnly players can execute this command."));
            return;
        }

        Player player = (Player) sender;
        Team tm = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (!(tm instanceof SafezoneTeam)) {
            sendMessage(sender, CC.t("&cYou can only use this on spawn."));
        } else {
            new ArcherUpgradeMenu(getInstance().getMenuManager(), player).open();
        }

    }
}
