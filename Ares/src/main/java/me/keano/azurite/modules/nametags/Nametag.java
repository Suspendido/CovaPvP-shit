package me.keano.azurite.modules.nametags;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.nametags.packet.NametagPacket;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Nametag extends Module<NametagManager> {

    private final Player player;
    private final NametagPacket packet;
    private int protocolVersion;

    public Nametag(NametagManager manager, Player player) {
        super(manager);
        this.player = player;
        this.packet = manager.createPacket(player);
        this.protocolVersion = Utils.getProtocolVersion(player);
        Tasks.executeLater(getManager(), 20L, () -> protocolVersion = Utils.getProtocolVersion(player));
    }

    public void delete() {
        packet.delete();
    }
}