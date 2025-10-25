package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.storage.Storage;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.configs.ConfigYML;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;

public class AzuriteCommand extends Command {

    public AzuriteCommand(CommandManager manager) {
        super(
                manager,
                "ares"
        );
        this.completions.add(new TabCompletion(Arrays.asList("reload", "deleteteams", "deleteusers", "version", "forcesave"), 0));
        this.setPermissible("azurite.reload");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("hcf", "hcfcore");
    }

    @Override
    public List<String> usage() {
        return Arrays.asList(
                CC.LINE,
                "&fThis server is running &4Ares&f. &7Fork by @RodriDevs for &4CovaPvP Network",
                "&fUse &4/ares reload &fto reload configs.",
                "&fUse &4/ares version &fto check your current ver.",
                "&fUse &4/ares forcesave &fto save data.",
                "&fUse &4/ares deleteteams &fto delete teams.",
                "&fUse &4/ares deleteusers &fto delete users.",
                CC.LINE
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(permissible)) {
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                long now = System.currentTimeMillis();

                List<ConfigYML> configs = new ArrayList<>(getInstance().getConfigs());
                List<Manager> managers = new ArrayList<>(getInstance().getManagers());

                for (ConfigYML config : configs) {
                    config.reload();
                    config.reloadCache();
                }

                for (Manager manager : managers) {
                    manager.reload();
                }

                Config.load(getInstance().getConfigsObject(), true);
                sendMessage(sender, "&dAres &fhas been reloaded in &a" + (System.currentTimeMillis() - now) + "ms&f.");
                return;

            case "version":
                sendMessage(sender, "&4Ares &fis currently on version &a" + getInstance().getDescription().getVersion() + "&f.");
                return;

            case "forcesave":
                Tasks.executeAsync(getManager(), () -> {
                    long now1 = System.currentTimeMillis();
                    Storage storage = getInstance().getStorageManager().getStorage();

                    storage.saveTimers();
                    storage.saveTeams();
                    storage.saveUsers();

                    sendMessage(sender, "&4Ares &fhas been saved in &a" + (System.currentTimeMillis() - now1) + "ms&f.");
                });
                return;

            case "deleteteams":
                TeamManager teamManager = getInstance().getTeamManager();
                Iterator<Team> iterator = teamManager.getTeams().values().iterator();
                int removedTeams = 0;

                while (iterator.hasNext()) {
                    Team team = iterator.next();
                    if (team instanceof PlayerTeam) {
                        ((PlayerTeam) team).disband();
                        iterator.remove();
                        removedTeams++;
                    }
                }

                sendMessage(sender, "&fYou have deleted &d" + removedTeams + " &fteams.");
                return;

            case "deleteusers":
                List<User> toDelete = new ArrayList<>(getInstance().getUserManager().getUsers().values());
                for (User user : toDelete) {
                    user.delete();
                }

                getInstance().getUserManager().getUsers().clear();
                getInstance().getUserManager().getUuidCache().clear();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    User user = new User(getInstance().getUserManager(), player.getUniqueId(), player.getName());
                    user.save();
                }

                sendMessage(sender, "&fYou have deleted &d" + toDelete.size() + " &fusers.");
                return;
        }

        sendUsage(sender);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProcess(PlayerCommandPreprocessEvent e) {
        Player sender = e.getPlayer();

        if (sender.hasPermission("azurite.reload")) return;

        if (e.getMessage().equals("/ares:" + name) || e.getMessage().equals("/" + name)) {
            sendMessage(sender, CC.LINE);
            sendMessage(sender, "&fThis server is running &4AresHCF&f. &fFork by &d@RodriDevs&f.");
            sendMessage(sender, "&ddiscord.gg/covapvp");
            sendMessage(sender, CC.LINE);
            return;
        }

        for (String alias : aliases()) {
            if (e.getMessage().equals("/ares:" + alias) || e.getMessage().equals("/" + alias)) {
                sendMessage(sender, CC.LINE);
                sendMessage(sender, "&fThis server is running &4AresHCF&f. &fFork by &d@RodriDevs&f.");
                sendMessage(sender, "&ddiscord.gg/covapvp");
                sendMessage(sender, CC.LINE);
                break;
            }
        }
    }
}
