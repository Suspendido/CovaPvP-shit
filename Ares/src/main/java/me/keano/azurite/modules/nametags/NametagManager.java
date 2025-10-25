package me.keano.azurite.modules.nametags;

import lombok.Getter;
import lombok.SneakyThrows;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.bounty.BountyData;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.nametags.adapter.AzuriteNametags;
import me.keano.azurite.modules.nametags.listener.NametagListener;
import me.keano.azurite.modules.nametags.packet.NametagPacket;
import me.keano.azurite.modules.nametags.task.NametagTask;
import me.keano.azurite.modules.teams.extra.TeamPosition;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.extra.NameThreadFactory;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.FastReplaceString;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class NametagManager extends Manager {

    private final Map<UUID, Nametag> nametags;
    private final NametagAdapter adapter;
    private final ScheduledExecutorService executor;

    private final Map<UUID, Long> frozenStartTimes = new ConcurrentHashMap<>();

    public NametagManager(HCF instance) {
        super(instance);

        this.nametags = new ConcurrentHashMap<>();
        this.adapter = new AzuriteNametags(this);
        this.executor = Executors.newScheduledThreadPool(1, new NameThreadFactory("Azurite - NametagThread"));
        this.executor.scheduleAtFixedRate(new NametagTask(this), 0L, 300L, TimeUnit.MILLISECONDS);

        new NametagListener(this);
    }

    @Override
    public void disable() {
        for (Nametag nametag : nametags.values()) {
            nametag.delete();
        }
        executor.shutdownNow();
    }

    public void handleUpdate(Player viewer, Player target) {
        if (viewer == null || target == null) return; // Possibly?
        String prefix = getAdapter().getAndUpdate(viewer, target);

        updateLunarTags(viewer, target, prefix);
    }

    public void updateLunarTags(Player viewer, Player target, String prefix) {
        if (getInstance().getClientHook().getClients().isEmpty()) return;
        if (!nametags.containsKey(viewer.getUniqueId())) return;

        User user = getInstance().getUserManager().getByUUID(viewer.getUniqueId());

        if (!user.isLunarNametags()) {
            if (user.isClearedNametags()) {
                return;
            }
            user.setClearedNametags(true);
            getInstance().getClientHook().clearNametags(viewer);
            return;
        }
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(target.getUniqueId());
        String pos = getInstance().getUserManager().getPrefix(target);
        String name = prefix + target.getName();
        String killTag = (pos != null ? pos : "");
        String teamTag = getTeamTag(pt, viewer);
        BountyData bountyData = getInstance().getBountyManager().getBountyData(target.getUniqueId());
        boolean vanished = getInstance().getStaffManager().isVanished(target);
        user.setClearedNametags(false);

        List<String> format = new ArrayList<>();

        if (getInstance().getStaffManager().isStaffEnabled(target) || getInstance().getStaffManager().isHeadStaffEnabled(target)) {
            for (String s : Config.NAMETAG_MOD_MODE) {
                format.add(s
                        .replace("%name%", name)
                        .replace("%killtag%", killTag)
                        .replace("%teamtag%", teamTag)
                        .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                        .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                        .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                        .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                        .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                );
            }
        } else {
            if (getInstance().getTimerManager().getAntiCleanTimer().hasTimer(target)) {
                for (String s : Config.NAMETAG_ANTICLEAN) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%anticlean-team%", getInstance().getTimerManager().getAntiCleanTimer().getTeam(target))
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                    );
                }
            }
            if (getInstance().getTimerManager().getArcherTagTimer().hasTimer(target)) {
                for (String s : Config.NAMETAG_ARCHERTAG) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                    );
                }
            }

            if (bountyData != null && bountyData.getAmount() > 0) {
                for (String s : Config.NAMETAG_BOUNTY) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                            .replace("%amount%", String.valueOf(bountyData.getAmount()))
                    );
                }
            }

            if (getInstance().getStaffManager().isFrozen(target)) {
                long frozenStartTime = frozenStartTimes.computeIfAbsent(target.getUniqueId(), k -> System.currentTimeMillis());
                long elapsedTime = System.currentTimeMillis() - frozenStartTime;
                long minutes = (elapsedTime / 1000) / 60;
                long seconds = (elapsedTime / 1000) % 60;
                String formattedTime = String.format("%02d:%02d", minutes, seconds);

                for (String s : Config.NAMETAG_FROZEN) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%time%", formattedTime)
                    );
                }
            } else {
                frozenStartTimes.remove(target.getUniqueId());
            }

            if (getInstance().getTimerManager().getPvpTimer().hasTimer(target) || getInstance().getTimerManager().getInvincibilityTimer().hasTimer(target)) {
                for (String s : Config.NAMETAG_INVINCIBILITY_TIMER) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                    );
                }
            }

            if (pt != null) {
                for (String s : Config.NAMETAG_IN_TEAM) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : ""))
                            .replace("%health%", String.valueOf((int) target.getHealth() / 2))
                            .replace("%disqualified%", String.valueOf(pt.isDisqualified() ? getLanguageConfig().getString("ADMIN_TEAM_COMMAND.STATUS_IF_DISQUALIFIED") : ""))
                    );
                }
            } else {
                for (String s : Config.NAMETAG_NO_TEAM) {
                    format.add(s
                            .replace("%name%", name)
                            .replace("%killtag%", killTag)
                            .replace("%teamtag%", teamTag)
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                            .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target)))
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                            .replace("%rank-suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                            .replace("%health%", String.valueOf((int) target.getHealth() / 2))
                            .replace("%vanishsymbol%", (vanished ? Config.VANISHED_SYMBOL : "")));
                }
            }
        }

        handleLunar(target, viewer, format);
    }

    private void handleLunar(Player target, Player viewer, List<String> format) {
        // you can't send the packet asynchronously in modern versions
        if (Utils.isModernVer()) {
            Tasks.execute(this, () -> getInstance().getClientHook().overrideNametags(target, viewer, format));
            return;
        }

        getInstance().getClientHook().overrideNametags(target, viewer, format);
    }

    private String getTeamTag(PlayerTeam pt, Player viewer) {
        if (pt == null) {
            return "";
        }

        TeamPosition pos = pt.getTeamPosition();

        if (pos != null) {
            return new FastReplaceString(Config.NAMETAGS_TEAM_TOP)
                    .replaceAll("%name%", (Config.NAMETAGS_TEAM_TOP.contains("%pos-color%") ? pt.getName() : pt.getDisplayName(viewer)))
                    .replaceAll("%dtr%", pt.getDtrString())
                    .replaceAll("%dtr-color%", pt.getDtrColor())
                    .replaceAll("%dtr-symbol%", pt.getDtrSymbol())
                    .replaceAll("%pos%", pos.getPrefix())
                    .replaceAll("%pos-color%", pos.getColor())
                    .endResult();

        } else {
            return new FastReplaceString(Config.NAMETAGS_NORMAL)
                    .replaceAll("%name%", pt.getDisplayName(viewer))
                    .replaceAll("%dtr%", pt.getDtrString())
                    .replaceAll("%dtr-color%", pt.getDtrColor())
                    .replaceAll("%dtr-symbol%", pt.getDtrSymbol())
                    .endResult();
        }
    }

    @SneakyThrows
    public NametagPacket createPacket(Player player) {
        String path = "me.keano.azurite.modules.nametags.packet.type.NametagPacketV" + Utils.getNMSVer();
        return (NametagPacket) Class.forName(path).getConstructor(NametagManager.class, Player.class).newInstance(this, player);
    }
}