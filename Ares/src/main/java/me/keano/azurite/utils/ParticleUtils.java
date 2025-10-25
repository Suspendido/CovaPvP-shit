package me.keano.azurite.utils;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ParticleUtils implements Listener {

    public static void spawnParticle(Player player, EnumParticle particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                particle,
                true,
                (float) x, (float) y, (float) z,
                offsetX, offsetY, offsetZ,
                speed,
                count
        );
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}