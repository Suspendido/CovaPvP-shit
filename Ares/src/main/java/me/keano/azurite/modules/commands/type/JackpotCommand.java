package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.*;

public class JackpotCommand extends Command {

    // ======= CONFIG =======
    private static final boolean DEBUG = false;           // pon true para ver logs de animación
    private static final double VIEW_RANGE = 40.0;        // radio de jugadores que ven la animación
    private static final int TICK_LOG_EVERY = 20;         // cada cuántos ticks log si DEBUG
    // ======================

    private static final Set<UUID> RUNNING = new HashSet<>();
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public JackpotCommand(CommandManager manager) {
        super(manager, "jackpot");
    }

    @Override public List<String> aliases() { return Arrays.asList("jp"); }
    @Override public List<String> usage() { return Collections.singletonList("&e/jackpot &7- Gira la ruleta de premios"); }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo jugadores pueden usar este comando.");
            return;
        }
        Player player = (Player) sender;

        if (RUNNING.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Ya tienes un jackpot en curso.");
            return;
        }

        startJackpot(player);
    }

    private void startJackpot(Player player) {
        RUNNING.add(player.getUniqueId());

        // Base
        final World bWorld = player.getWorld();
        final WorldServer nmsWorld = ((CraftWorld) bWorld).getHandle();
        final Location center = player.getLocation().clone();
        final double radius = 2.0;
        final double yOffset = 1.5;
        final int amount = 12;

        // Ítems visibles/ganables
        final ItemStack[] pool = {
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.GOLD_INGOT),
                new ItemStack(Material.EMERALD),
                new ItemStack(Material.IRON_INGOT),
                new ItemStack(Material.NETHER_STAR)
        };

        final Random rng = new Random();

        // "Entidades" client-side: EntityItem + su stack
        final List<EntityItem> items = new ArrayList<>(amount);
        final List<ItemStack> itemStacks = new ArrayList<>(amount);

        // Viewers (a quién mandar paquetes)
        final List<Player> viewers = getViewers(player, VIEW_RANGE);
        if (DEBUG) player.sendMessage(ChatColor.AQUA + "[JP/DEBUG] Viewers=" + viewers.size());

        if (DEBUG) {
            player.sendMessage(ChatColor.AQUA + "[JP/DEBUG] Centro: "
                    + fmt(center.getX()) + ", " + fmt(center.getY() + yOffset) + ", " + fmt(center.getZ())
                    + " | radio=" + radius + " | amount=" + amount);
        }

        // ---- SPAWN (ITEMS SOLO POR PAQUETE) ----
        for (int i = 0; i < amount; i++) {
            double baseAngle = (2 * Math.PI / amount) * i;
            double x = center.getX() + radius * Math.cos(baseAngle);
            double z = center.getZ() + radius * Math.sin(baseAngle);
            double y = center.getY() + yOffset;

            ItemStack bukkitStack = pool[rng.nextInt(pool.length)].clone();
            EntityItem ent = new EntityItem(nmsWorld, x, y, z, CraftItemStack.asNMSCopy(bukkitStack));
            ent.motX = ent.motY = ent.motZ = 0; // cero velocidad client-side
            ent.setPositionRotation(x, y, z, 0, 0);

            // Spawn del "objeto" (tipo 2 = item)
            // Firma 1.8_R3: PacketPlayOutSpawnEntity(Entity e, int type, int data)
            sendPacket(viewers, new PacketPlayOutSpawnEntity(ent, 2, 0));

            // Metadata para que el cliente sepa qué ItemStack es
            sendPacket(viewers, new PacketPlayOutEntityMetadata(ent.getId(), ent.getDataWatcher(), true));

            if (DEBUG) {
                player.sendMessage(ChatColor.AQUA + "[JP/DEBUG] Spawned item #" + i + " "
                        + bukkitStack.getType().name() + " en "
                        + fmt(x) + ", " + fmt(y) + ", " + fmt(z));
            }

            items.add(ent);
            itemStacks.add(bukkitStack);
        }

        if (items.size() < amount) {
            player.sendMessage(ChatColor.RED + "[JP/DEBUG] ¡No se spawnearon todos los items! " + items.size() + "/" + amount);
        } else if (DEBUG) {
            player.sendMessage(ChatColor.GREEN + "[JP/DEBUG] Spawneados " + items.size() + " items client-side.");
        }

        // ---- ANIMACIÓN (teleports por paquete; no hay caída) ----
        new BukkitRunnable() {
            double angleOffset = 0.0;
            double speed = 0.30;        // rad/tick
            int ticks = 0;
            final int maxTicks = 200;

            @Override
            public void run() {
                if (!player.isOnline() || viewers.isEmpty() || items.isEmpty()) {
                    if (DEBUG) player.sendMessage(ChatColor.RED + "[JP/DEBUG] Cancelando animación (player offline/viewers/items vacíos).");
                    destroyAll();
                    cleanup();
                    cancel();
                    return;
                }

                ticks++;
                if (ticks > maxTicks / 2) {
                    speed *= 0.97; // desacelerar
                    if (speed < 0.01) speed = 0.01;
                }

                for (int i = 0; i < items.size(); i++) {
                    EntityItem ent = items.get(i);

                    double angle = (2 * Math.PI / items.size()) * i + angleOffset;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    double y = center.getY() + yOffset;

                    ent.setPositionRotation(x, y, z, 0, 0);

                    // Teleport estable (1.8): ctor con Entity
                    sendPacket(viewers, new PacketPlayOutEntityTeleport(ent));

                    // Opcional: asegura vel=0 para que no "flote" por inercia visual
                    sendPacket(viewers, new PacketPlayOutEntityVelocity(ent));

                    if (DEBUG && i == 0 && ticks % TICK_LOG_EVERY == 0) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "[JP/DEBUG] tick=" + ticks
                                + " | speed=" + fmt(speed)
                                + " | angleOffset=" + fmt(angleOffset)
                                + " | pos#0=" + fmt(x) + "," + fmt(y) + "," + fmt(z));
                        // traza de partículas para confirmar posición
                        bWorld.spigot().playEffect(new Location(bWorld, x, y, z),
                                Effect.HAPPY_VILLAGER, 0, 0, 0, 0, 0, 0.01f, 1, 32);
                    }
                }

                angleOffset += speed;

                // Parada: suficientemente lento
                if (speed <= 0.02) {
                    int winIndex = rng.nextInt(items.size());
                    ItemStack won = itemStacks.get(winIndex).clone();

                    Location winLoc = new Location(bWorld,
                            center.getX() + radius * Math.cos((2 * Math.PI / items.size()) * winIndex + angleOffset),
                            center.getY() + yOffset,
                            center.getZ() + radius * Math.sin((2 * Math.PI / items.size()) * winIndex + angleOffset)
                    );

                    bWorld.playSound(winLoc, Sound.LEVEL_UP, 1f, 1f);
                    bWorld.playEffect(winLoc, Effect.MOBSPAWNER_FLAMES, 1, 20);

                    Map<Integer, ItemStack> overflow = player.getInventory().addItem(won);
                    if (!overflow.isEmpty()) {
                        bWorld.dropItemNaturally(player.getLocation(), won);
                    }
                    player.sendMessage(ChatColor.GREEN + "¡Ganaste: " + won.getType().name() + "!");

                    destroyAll();
                    cleanup();
                    cancel();
                }
            }

            private void destroyAll() {
                for (EntityItem ent : items) {
                    try {
                        sendPacket(viewers, new PacketPlayOutEntityDestroy(ent.getId()));
                    } catch (Throwable ignored) {}
                }
            }

            private void cleanup() {
                RUNNING.remove(player.getUniqueId());
            }
        }.runTaskTimer(getInstance(), 0L, 1L);
    }

    /* ======================= helpers ======================= */

    private List<Player> getViewers(Player center, double range) {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().equals(center.getWorld())) continue;
            if (p.getLocation().distanceSquared(center.getLocation()) <= range * range) {
                list.add(p);
            }
        }
        if (!list.contains(center)) list.add(center);
        return list;
    }

    private void sendPacket(Collection<Player> players, Packet<?> packet) {
        for (Player p : players) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private static String fmt(double d) { return DF.format(d); }
}
