package me.keano.azurite.modules.pvpclass.type.bomber;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.ParticleUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Getter
public class BomberClass extends PvPClass {

    private Material tntitem;
    private Material minecartitem;
    private CustomCooldown tntCooldown;
    private CustomCooldown minecraftCooldown;

    public BomberClass(PvPClassManager manager) {
        super(manager, "Bomber");
        this.load();
    }

    @Override
    public void load() {
        this.tntCooldown = new CustomCooldown(this, getScoreboardConfig().getString("BOMBER_CLASS.TNT_COOLDOWN"));
        this.tntitem = ItemUtils.getMat(getClassesConfig().getString("BOMBER_CLASS.TNT_ITEM"));
        this.minecartitem = ItemUtils.getMat(getClassesConfig().getString("BOMBER_CLASS.MINECART_ITEM"));
        this.minecraftCooldown = new CustomCooldown(this, getScoreboardConfig().getString("BOMBER_CLASS.MINECART_COOLDOWN"));
    }

    @Override
    public void handleEquip(Player player) { }

    @Override
    public void handleUnequip(Player player) { }

    @Override
    public void reload() {
        this.loadEffectsArmor();
        this.load();
    }

    @EventHandler
    public void onTNT(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);
        if (hand == null || hand.getType() != tntitem) return;
        if (!players.contains(player.getUniqueId())) return;
        if (tntCooldown.hasCooldown(player)) {
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BOMBER_CLASS.TNT_COOLDOWN")
                    .replace("%seconds%", tntCooldown.getRemaining(player)));
            return;
        }
        if (checkBomber(player, true)) return;

        getInstance().getTimerManager().getCombatTimer().applyTimer(player);
        tntCooldown.applyCooldown(player, getClassesConfig().getInt("BOMBER_CLASS.TNT_COOLDOWN"));

        double throwForce = getClassesConfig().getDouble("BOMBER_CLASS.THROW_FORCE", 0.8);
        spawnBomberTnt(player, throwForce);
        sendParticles(player);

        // 🔥 Sonido: FIRE_IGNITE (TNT normal)
        try {
            Sound sound = Sound.valueOf("FIRE_IGNITE");
            player.playSound(player.getLocation(), sound, 0.8f, 1.0f);
        } catch (IllegalArgumentException ex) {
            player.playSound(player.getLocation(), "fire.ignite", 0.8f, 1.0f);
        }
    }

    @EventHandler
    public void onMinecart(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);
        if (hand == null || hand.getType() != minecartitem) return;
        if (!players.contains(player.getUniqueId())) return;
        if (minecraftCooldown.hasCooldown(player)) {
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BOMBER_CLASS.MINECART_COOLDOWN")
                    .replace("%seconds%", minecraftCooldown.getRemaining(player)));
            return;
        }
        if (checkBomber(player, true)) return;

        getInstance().getTimerManager().getCombatTimer().applyTimer(player);
        minecraftCooldown.applyCooldown(player, getClassesConfig().getInt("BOMBER_CLASS.MINECART_COOLDOWN"));
        sendParticles(player);
        spawnTntMinecart(player);

        // 🔥 Sonido: FIRE_IGNITE (TNT Minecart, más grave)
        try {
            Sound sound = Sound.valueOf("FIRE_IGNITE");
            player.playSound(player.getLocation(), sound, 0.8f, 0.6f);
        } catch (IllegalArgumentException ex) {
            player.playSound(player.getLocation(), "fire.ignite", 0.8f, 0.6f);
        }
    }

    /**
     * Spawnea una Minecart con TNT que:
     * - Tiene una explosión de TNT cuando toca el suelo o un bloque.
     * - Explota automáticamente al tocar cualquier superficie (suelo, árbol, casa, jugador, etc.).
     * - Tiene un fusible corto (por defecto en Minecraft: ~4 segundos).
     */
    private void spawnTntMinecart(Player player) {
        Location spawnLocation = player.getLocation().add(0, 20, 0);

        // Spawnea la minecart
        Minecart minecart = (Minecart) player.getWorld().spawn(spawnLocation, ExplosiveMinecart.class);

        // Obtiene la entidad NMS
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftMinecart) minecart).getHandle();

        if (nmsEntity instanceof net.minecraft.server.v1_8_R3.EntityMinecartTNT) {
            EntityMinecartTNT tntCart = (EntityMinecartTNT) nmsEntity;

            // Intenta con varios nombres posibles del campo "fuseTicks"
            String[] possibleFields = {"fuseTicks", "b", "c", "a", "f", "fuse", "ticks"};

            for (String fieldName : possibleFields) {
                try {
                    java.lang.reflect.Field field = EntityMinecartTNT.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(tntCart, 80); // Establece el fusible a 80 ticks (~4 segundos)
                    break;
                } catch (Exception e) {
                    // Ignorar y probar con el siguiente
                }
            }
        }

        // Metadata
        minecart.setMetadata("BomberMinecart", new FixedMetadataValue(getInstance(), true));
        minecart.setMetadata("Owner", new FixedMetadataValue(getInstance(), player.getUniqueId()));
    }

    private void spawnBomberTnt(Player player, double throwForce) {
        Location spawnLocation = player.getLocation().add(0, 1.5, 0);
        TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(spawnLocation, EntityType.PRIMED_TNT);
        tnt.setVelocity(player.getLocation().getDirection().multiply(throwForce));

        int fuseTicks = getClassesConfig().getInt("BOMBER_CLASS.TNT_FUSE_TICKS", 40);
        tnt.setFuseTicks(fuseTicks);
        tnt.setMetadata("BomberTNT", new FixedMetadataValue(getInstance(), true));

        double holoOffset = getClassesConfig().getDouble("BOMBER_CLASS.HOLOGRAM_OFFSET", 0.4);

        EntityArmorStand holo = new EntityArmorStand(((CraftPlayer) player).getHandle().getWorld());
        holo.setLocation(spawnLocation.getX(), spawnLocation.getY() + holoOffset, spawnLocation.getZ(), 0, 0);
        holo.setInvisible(true);
        holo.setSmall(true);
        holo.setCustomNameVisible(true);
        holo.setCustomName("§4" + String.format("%.1f", tnt.getFuseTicks() / 20.0) + "s");
        holo.setBasePlate(false);
        holo.setGravity(false);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(holo));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!tnt.isValid()) {
                    PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(holo.getId());
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);
                    cancel();
                    return;
                }

                Location tntLoc = tnt.getLocation().clone().add(0, holoOffset, 0);
                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(
                        holo.getId(),
                        (int) (tntLoc.getX() * 32),
                        (int) (tntLoc.getY() * 32),
                        (int) (tntLoc.getZ() * 32),
                        (byte) 0,
                        (byte) 0,
                        false
                );
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);

                String text = "§4" + String.format("%.1f", tnt.getFuseTicks() / 20.0) + "s";
                holo.setCustomName(text);
                DataWatcher watcher = holo.getDataWatcher();
                watcher.watch(2, text);
                PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(holo.getId(), watcher, true);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metaPacket);
            }
        }.runTaskTimer(getInstance(), 0L, 1L);
    }

    @EventHandler
    public void onTntDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                for (Entity ent : e.getEntity().getNearbyEntities(6, 6, 6)) {
                    if (ent instanceof TNTPrimed && ent.hasMetadata("BomberTNT")) {
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTntExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) e.getEntity();
            if (tnt.hasMetadata("BomberTNT")) {

                double horizontalKb = getClassesConfig().getDouble("BOMBER_CLASS.KNOCKBACK_HORIZONTAL", 1.2);
                double verticalKb = getClassesConfig().getDouble("BOMBER_CLASS.KNOCKBACK_VERTICAL", 0.5);

                for (Player player : tnt.getWorld().getPlayers()) {
                    if (player.getLocation().distance(tnt.getLocation()) <= 6) {
                        Vector direction = player.getLocation().toVector().subtract(tnt.getLocation().toVector()).normalize();
                        Vector velocity = direction.multiply(horizontalKb).setY(verticalKb);
                        player.setVelocity(velocity);
                    }
                }
            }
        }
    }

    private void sendParticles(Player player) {
        Location location = player.getLocation();
        EnumParticle particleffect = EnumParticle.FLAME;
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
            double x = Math.cos(angle) * 1;
            double z = Math.sin(angle) * 1;
            Location particleLocation = location.clone().add(x, 0.5, z);
            ParticleUtils.spawnParticle(player, particleffect,
                    particleLocation.getX(), particleLocation.getY(), particleLocation.getZ(),
                    0.1F, 0.1F, 0.1F, 0.05F, 5);
        }
    }

    public boolean checkBomber(Player player, boolean message) {
        if (getInstance().getTimerManager().getPvpTimer().hasTimer(player) ||
                getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player) ||
                getInstance().getSotwManager().isActive()) {
            if (message) player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BOMBER_CLASS.CANNOT_TNT_PVPTIMER"));
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof SafezoneTeam) {
            if (message) player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BOMBER_CLASS.CANNOT_TNT_SAFEZONE"));
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof EventTeam) {
            if (message) player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BOMBER_CLASS.CANNOT_TNT_EVENT"));
            return true;
        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof CitadelTeam ||
                getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof ConquestTeam ||
                getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof DTCTeam) {
            if (message) player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BOMBER_CLASS.CANNOT_TNT_EVENT"));
            return true;
        }
        return false;
    }
}