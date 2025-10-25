package me.keano.azurite.modules.bounty;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.bounty.menu.BountyMenu;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.CC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class BountyManager extends Manager {

    private final Map<UUID, BountyData> bounties;
    private final Economy economy;
    private final File bountyFile;
    private final FileConfiguration bountyConfig;

    public BountyManager(HCF instance) {
        super(instance);
        this.bounties = new HashMap<>();
        this.economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        this.bountyFile = new File(instance.getDataFolder(), "bounties.yml");
        this.bountyConfig = YamlConfiguration.loadConfiguration(bountyFile);
        loadBountyData();
    }

    private void loadBountyData() {
        ConfigurationSection section = bountyConfig.getConfigurationSection("BOUNTIES");
        if (section == null) return;

        for (String targetUuid : section.getKeys(false)) {
            UUID target = UUID.fromString(targetUuid);
            String targetName = section.getString(targetUuid + ".name");
            Map<UUID, Integer> appliers = new HashMap<>();
            Set<UUID> players = new HashSet<>();

            ConfigurationSection applierSection = section.getConfigurationSection(targetUuid + ".appliers");
            if (applierSection != null) {
                for (String applierUuid : applierSection.getKeys(false)) {
                    appliers.put(UUID.fromString(applierUuid), applierSection.getInt(applierUuid));
                }
            }

            if (section.getConfigurationSection(targetUuid + ".players") != null) {
                for (String playerUuid : section.getStringList(targetUuid + ".players")) {
                    players.add(UUID.fromString(playerUuid));
                }
            }

            BountyData data = new BountyData(target, targetName, appliers, players);
            this.bounties.put(target, data);
        }
    }

    public void addBounty(Player target, Player applier, int bounty) {
        if (applier == target) {
            applier.sendMessage(getLanguageConfig().getString("BOUNTY.CANNOT_PUT_YOURSELF"));
            return;
        }

        int minAmount = getConfig().getInt("BOUNTY.MIN_AMOUNT", 100);
        if (bounty < minAmount) {
            applier.sendMessage(getLanguageConfig().getString("BOUNTY.MINIMAL_AMOUNT_REQUIRED")
                    .replace("%amount%", String.valueOf(minAmount)));
            return;
        }

        if (!economy.has(applier, bounty)) {
            applier.sendMessage(getLanguageConfig().getString("BOUNTY.NO_ENOUGH_MONEY"));
            return;
        }

        economy.withdrawPlayer(applier, bounty);
        this.bounties.putIfAbsent(target.getUniqueId(), new BountyData(target.getUniqueId(),
                target.getName(), applier.getUniqueId(), 0));

        BountyData data = bounties.get(target.getUniqueId());
        data.addApplier(applier.getUniqueId(), bounty);

        String message = getLanguageConfig().getString("BOUNTY.ADDED_PLAYER.PLAYER")
                .replace("%amount%", String.valueOf(bounty))
                .replace("%player%", target.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)));
        applier.sendMessage(message);

        String global = getLanguageConfig().getString("BOUNTY.ADDED_PLAYER.GLOBAL")
                .replace("%amount%", String.valueOf(bounty))
                .replace("%applier%", applier.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(applier)))
                .replace("%target%", target.getName())
                .replace("%targetPrefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)));
        Bukkit.broadcastMessage(global);

        saveBountyData();
    }

    public void removeBounty(Player target, Player applier) {
        BountyData data = bounties.get(target.getUniqueId());

        if ((data == null) || !data.containsApplier(applier.getUniqueId())) {
            applier.sendMessage(getLanguageConfig().getString("BOUNTY.NOT_SET_YET")
                    .replace("%target%", target.getName()));
            return;
        }

        int bounty = data.removeApplier(applier.getUniqueId());
        economy.depositPlayer(applier, bounty);

        applier.sendMessage(getLanguageConfig().getString("BOUNTY.REMOVED")
                .replace("%player%", target.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target))));

        if (data.getAppliers().isEmpty()) {
            this.bounties.remove(target.getUniqueId());
        }

        saveBountyData();
    }

    public void onTargetDeath(Player target, Player killer) {
        BountyData data = bounties.remove(target.getUniqueId());
        if (data == null || killer == null) return;

        int totalBounty = data.getAmount();
        economy.depositPlayer(killer, totalBounty);

        killer.sendMessage(getLanguageConfig().getString("BOUNTY.DEATH.PLAYER")
                .replace("%amount%", String.valueOf(totalBounty))
                .replace("%player%", target.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target))));

        String global = getLanguageConfig().getString("BOUNTY.DEATH.GLOBAL")
                .replace("%amount%", String.valueOf(totalBounty))
                .replace("%player%", killer.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(killer)))
                .replace("%target%", target.getName())
                .replace("%targetPrefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)));
        Bukkit.broadcastMessage(global);

        saveBountyData();
    }

    public BountyData getBountyData(UUID target) {
        return this.bounties.get(target);
    }

    private void saveBountyData() {
        bountyConfig.set("BOUNTIES", null);
        for (Map.Entry<UUID, BountyData> entry : bounties.entrySet()) {
            UUID target = entry.getKey();
            BountyData data = entry.getValue();
            bountyConfig.set("BOUNTIES." + target + ".name", data.getTargetName());
            for (Map.Entry<UUID, Integer> applierEntry : data.getAppliers().entrySet()) {
                bountyConfig.set("BOUNTIES." + target + ".appliers." + applierEntry.getKey(), applierEntry.getValue());
            }

            bountyConfig.set("BOUNTIES." + target + ".players",
                    data.getPlayers().stream().map(UUID::toString).collect(Collectors.toList()));
        }
        try {
            bountyConfig.save(bountyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listBounties(Player player) {
        if (bounties.isEmpty()) {
            player.sendMessage(getLanguageConfig().getString("BOUNTY.LIST_EMPTY"));
            return;
        }
        new BountyMenu(getInstance().getMenuManager(), player).open();
    }
}
