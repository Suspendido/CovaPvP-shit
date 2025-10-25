package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;

import java.lang.reflect.Field;
import java.util.*;

public class BallonCommand extends Command {

    private static final String BASE64_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgxNGIzMzc3Njc1YmMxZGVhM2M0YzI2NmNkNTVlZGRjNzJlOGE0MTlhMmNiZGI4NWE2NmUzYWM0ZjNjYjI4NyJ9fX0=";

    public BallonCommand(CommandManager manager) {
        super(manager, "ballon");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("balloon", "globito");
    }

    @Override
    public List<String> usage() {
        return Collections.singletonList("&e/ballon &7- Alterna tu globo");
    }

    private static final Map<UUID, BalloonCtx> BALLOONS = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendUsage(sender);
            return;
        }
        Player p = (Player) sender;
        if (BALLOONS.containsKey(p.getUniqueId())) {
            removeBalloon(p, true);
        } else {
            spawnBalloon(p);
        }
    }

    private void spawnBalloon(Player player) {
        WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();

        Location eye = player.getEyeLocation();
        double x = eye.getX();
        double y = eye.getY() + 1.05; // 🔹 Un poco por encima de la cabeza
        double z = eye.getZ();

        EntityArmorStand anchor = new EntityArmorStand(nmsWorld);
        anchor.setLocation(x, y, z, eye.getYaw(), 0f);
        anchor.setInvisible(true);
        anchor.setSmall(true);

        EntityArmorStand display = new EntityArmorStand(nmsWorld);
        display.setLocation(x, y, z, eye.getYaw(), 0f);
        display.setInvisible(true);
        display.setSmall(true);

        send(player, new PacketPlayOutSpawnEntityLiving(anchor));
        send(player, new PacketPlayOutSpawnEntityLiving(display));

        ItemStack skull = createCustomSkull(BASE64_TEXTURE);
        send(player, new PacketPlayOutEntityEquipment(display.getId(), 4, CraftItemStack.asNMSCopy(skull)));

        send(player, new PacketPlayOutAttachEntity(1, anchor, ((CraftPlayer) player).getHandle()));

        BalloonCtx ctx = new BalloonCtx(anchor, display);
        ctx.px = x; ctx.py = y; ctx.pz = z;
        ctx.vx = ctx.vy = ctx.vz = 0;
        ctx.baseX = eye.getX(); ctx.baseY = eye.getY(); ctx.baseZ = eye.getZ(); ctx.baseYaw = eye.getYaw();
        ctx.lastMoveTime = System.currentTimeMillis();
        ctx.currentYaw = eye.getYaw();
        ctx.currentPitch = 0f;

        BukkitTask task = new BukkitRunnable() {
            int t = 0;
            double afkAngle = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    removeBalloon(player, false);
                    return;
                }

                Location eyeL = player.getEyeLocation();
                double playerX = eyeL.getX();
                double playerY = eyeL.getY();
                double playerZ = eyeL.getZ();
                float playerYaw = eyeL.getYaw();

                // Detectar movimiento
                double dx = playerX - ctx.baseX;
                double dy = playerY - ctx.baseY;
                double dz = playerZ - ctx.baseZ;
                float dyaw = Math.abs(playerYaw - ctx.baseYaw);
                boolean moved = (dx * dx + dy * dy + dz * dz) > 0.001 || dyaw > 1.0;

                if (moved) {
                    ctx.lastMoveTime = System.currentTimeMillis();
                    if (ctx.afkAnimation) {
                        ctx.afkAnimation = false;
                        ctx.vx = ctx.vy = ctx.vz = 0;
                    }
                    ctx.baseX = playerX;
                    ctx.baseY = playerY;
                    ctx.baseZ = playerZ;
                    ctx.baseYaw = playerYaw;
                }

                long now = System.currentTimeMillis();
                if (!ctx.afkAnimation && (now - ctx.lastMoveTime > 5000)) {
                    ctx.afkAnimation = true;
                    afkAngle = 0;
                }

                if (ctx.afkAnimation) {
                    // 🔹 Animación AFK lenta y fluida
                    afkAngle += 0.035;
                    double radius = 0.35 + Math.sin(afkAngle * 1.5) * 0.05;
                    double targetX = playerX + radius * Math.cos(afkAngle);
                    double targetZ = playerZ + radius * Math.sin(afkAngle);
                    double targetY = playerY + 1.05 + Math.sin(afkAngle * 2.2) * 0.12; // nueva altura

                    final double k = 0.25, d = 0.15;
                    double ax = (targetX - ctx.px) * k - ctx.vx * d;
                    double ay = (targetY - ctx.py) * k - ctx.vy * d;
                    double az = (targetZ - ctx.pz) * k - ctx.vz * d;

                    ctx.vx += ax; ctx.vy += ay; ctx.vz += az;
                    ctx.px += ctx.vx; ctx.py += ctx.vy; ctx.pz += ctx.vz;

                    double dxLook = playerX - ctx.px + Math.sin(afkAngle * 0.5) * 0.3;
                    double dzLook = playerZ - ctx.pz + Math.cos(afkAngle * 0.5) * 0.3;
                    float targetYaw = (float) Math.toDegrees(Math.atan2(-dxLook, dzLook));
                    float targetPitch = (float) Math.toDegrees(Math.atan2(targetY - ctx.py, Math.sqrt(dxLook * dxLook + dzLook * dzLook)));

                    float yawSpeed = 0.05f;
                    float pitchSpeed = 0.04f;
                    ctx.currentYaw = lerpAngle(ctx.currentYaw, targetYaw, yawSpeed);
                    ctx.currentPitch = lerpAngle(ctx.currentPitch, targetPitch, pitchSpeed);

                    ctx.display.setLocation(ctx.px, ctx.py, ctx.pz, ctx.currentYaw, ctx.currentPitch);
                    ctx.anchor.setLocation(ctx.px, ctx.py, ctx.pz, ctx.currentYaw, ctx.currentPitch);

                    send(player, new PacketPlayOutEntityTeleport(ctx.anchor));
                    send(player, new PacketPlayOutEntityTeleport(ctx.display));

                } else {
                    // 🔹 Animación normal ajustada
                    org.bukkit.util.Vector dir = eyeL.getDirection().normalize();
                    org.bukkit.util.Vector right = new org.bukkit.util.Vector(-dir.getZ(), 0, dir.getX()).normalize();
                    double bob = Math.sin(t / 6.0) * 0.20;
                    double side = Math.sin(t / 12.0) * 0.25;
                    double offUp = 1.05 + bob; // nueva altura
                    double offBack = -0.65;
                    org.bukkit.util.Vector offset = dir.clone().multiply(offBack).add(new org.bukkit.util.Vector(0, offUp, 0)).add(right.multiply(side));
                    double tx = eyeL.getX() + offset.getX();
                    double ty = eyeL.getY() + offset.getY();
                    double tz = eyeL.getZ() + offset.getZ();

                    final double k = 0.35, d = 0.18;
                    double ax = (tx - ctx.px) * k - ctx.vx * d;
                    double ay = (ty - ctx.py) * k - ctx.vy * d;
                    double az = (tz - ctx.pz) * k - ctx.vz * d;

                    ctx.vx += ax; ctx.vy += ay; ctx.vz += az;
                    ctx.px += ctx.vx; ctx.py += ctx.vy; ctx.pz += ctx.vz;

                    float yaw = eyeL.getYaw();
                    ctx.display.setLocation(ctx.px, ctx.py, ctx.pz, yaw, 0f);
                    ctx.anchor.setLocation(ctx.px, ctx.py, ctx.pz, yaw, 0f);

                    send(player, new PacketPlayOutEntityTeleport(ctx.anchor));
                    send(player, new PacketPlayOutEntityTeleport(ctx.display));

                    double speedMag = Math.sqrt(ctx.vx * ctx.vx + ctx.vy * ctx.vy + ctx.vz * ctx.vz);
                    final double SPEED_THRESHOLD = 0.06;
                    if (speedMag > SPEED_THRESHOLD && (t % 4 == 0)) { // partículas esporádicas
                        double inv = 1.0 / (speedMag + 1e-6);
                        double trailBack = Math.min(0.35, 0.12 + speedMag * 0.40);
                        double pxTrail = ctx.px - ctx.vx * inv * trailBack;
                        double pyTrail = ctx.py - 0.05;
                        double pzTrail = ctx.pz - ctx.vz * inv * trailBack;
                        sendRedstoneParticle(player, pxTrail, pyTrail, pzTrail, 0.0001f, 1.0f, 0.0001f, 1.0f);
                    }

                    if (t % 40 == 0) {
                        send(player, new PacketPlayOutAttachEntity(1, ctx.anchor, ((CraftPlayer) player).getHandle()));
                    }
                }

                t++;
            }
        }.runTaskTimer(getInstance(), 1L, 1L);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, true, false));
        ctx.task = task;
        BALLOONS.put(player.getUniqueId(), ctx);
        sendMessage(player, "&aGlobo &cactivado&a. &eFuerza II &aaplicada.");
    }

    private void removeBalloon(Player player, boolean notify) {
        BalloonCtx ctx = BALLOONS.remove(player.getUniqueId());
        if (ctx != null) {
            try { ctx.task.cancel(); } catch (Throwable ignored) {}
            try { send(player, new PacketPlayOutAttachEntity(1, ctx.anchor, null)); } catch (Throwable ignored) {}
            send(player, new PacketPlayOutEntityDestroy(new int[]{ctx.anchor.getId(), ctx.display.getId()}));
        }
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        if (notify) sendMessage(player, "&cGlobo desactivado&7. Efectos removidos y entidades eliminadas.");
    }

    private void send(Player p, Packet<?> packet) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    private void sendRedstoneParticle(Player p, double x, double y, double z, float r, float g, float b, float size) {
        PacketPlayOutWorldParticles pkt = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float) x, (float) y, (float) z, r, g, b, size, 0, new int[0]);
        send(p, pkt);
    }

    private ItemStack createCustomSkull(String base64Texture) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64Texture));
        try { Field f = meta.getClass().getDeclaredField("profile"); f.setAccessible(true); f.set(meta, profile); } catch (Exception ignored) {}
        skull.setItemMeta(meta);
        return skull;
    }

    private float lerpAngle(float current, float target, float speed) {
        float diff = wrapDegrees(target - current);
        return current + diff * speed;
    }

    private float wrapDegrees(float value) {
        value = value % 360.0f;
        if (value >= 180.0f) value -= 360.0f;
        if (value < -180.0f) value += 360.0f;
        return value;
    }

    private static final class BalloonCtx {
        final EntityArmorStand anchor, display;
        BukkitTask task;
        double px, py, pz;
        double vx, vy, vz;
        long lastMoveTime = System.currentTimeMillis();
        boolean afkAnimation = false;
        float baseYaw = 0;
        double baseX = 0, baseY = 0, baseZ = 0;
        float currentYaw = 0;
        float currentPitch = 0;
        BalloonCtx(EntityArmorStand anchor, EntityArmorStand display) {
            this.anchor = anchor; this.display = display;
        }
    }
}
