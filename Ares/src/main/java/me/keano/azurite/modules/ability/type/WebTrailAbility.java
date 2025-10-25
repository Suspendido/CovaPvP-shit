package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WebTrailAbility extends Ability {

    private final int duration;  // segundos que dura dejando rastro
    private final int delay;     // segundos antes de que aparezca la web
    private final int interval;  // cada cuántos ticks intenta colocar web

    public WebTrailAbility(AbilityManager manager) {
        super(manager, AbilityUseType.INTERACT, "Web Trail");
        this.duration = getAbilitiesConfig().getInt("WEB_TRAIL.DURATION", 10);
        this.delay = getAbilitiesConfig().getInt("WEB_TRAIL.DELAY", 1);
        this.interval = getAbilitiesConfig().getInt("WEB_TRAIL.INTERVAL", 20);
    }

    @Override
    public void onClick(Player player) {
        // item correcto + checks globales
        if (!hasAbilityInHand(player)) return;
        if (cannotUse(player) || hasCooldown(player)) return;

        // Debe estar en un claim de PlayerTeam
        Team teamAtLoc = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        if (!(teamAtLoc instanceof PlayerTeam)) {
            for (String s : getLanguageConfig().getStringList("ABILITIES.WEB_TRAIL.CANNOT_USE")) {
                player.sendMessage(CC.t(s));
            }
            return;
        }

        takeItem(player);
        applyCooldown(player);

        // Mensaje de uso
        for (String s : getLanguageConfig().getStringList("ABILITIES.WEB_TRAIL.USED")) {
            player.sendMessage(CC.t(s.replace("%time%", String.valueOf(duration))));
        }

        // Tarea del efecto: dura exactamente DURATION segundos (en ticks)
        new BukkitRunnable() {
            int ticksLeft = duration * 20;

            @Override
            public void run() {
                if (ticksLeft <= 0 || !player.isOnline()) {
                    // Anunciar fin del efecto (no limpiamos webs: cada una dura DURATION desde que se colocó)
                    for (String s : getLanguageConfig().getStringList("ABILITIES.WEB_TRAIL.ENDED")) {
                        player.sendMessage(CC.t(s));
                    }
                    cancel();
                    return;
                }

                // Debe seguir en PlayerTeam claim para colocar
                Team tNow = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
                if (tNow instanceof PlayerTeam) {
                    Location target = groundAbove(player.getLocation());
                    if (target != null && target.getBlock().getType() == Material.AIR) {
                        // Colocar tras el DELAY (seg)
                        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                            // Chequeos al colocar
                            if (target.getBlock().getType() == Material.AIR) {
                                target.getBlock().setType(Material.WEB);

                                // Programar limpieza de ESTA web tras DURATION segs
                                Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                                    if (target.getBlock().getType() == Material.WEB) {
                                        target.getBlock().setType(Material.AIR);
                                    }
                                }, duration * 20L);
                            }
                        }, delay * 20L);
                    }
                }

                ticksLeft -= interval; // avanzamos por el intervalo configurado
            }
        }.runTaskTimer(getInstance(), 0L, interval);
    }

    /**
     * Devuelve la ubicación (x,z) del jugador ajustada a 1 bloque por encima del suelo.
     * Si el jugador está en el aire, baja hasta encontrar bloque sólido y retorna arriba de ese bloque.
     * Si ya está sobre suelo, usa su Y actual si el bloque es aire y debajo es sólido.
     */
    private Location groundAbove(Location base) {
        World w = base.getWorld();
        int x = base.getBlockX();
        int z = base.getBlockZ();
        int y = base.getBlockY();

        // Baja hasta encontrar algo sólido bajo (y-1) o hasta Y=1
        while (y > 1 && w.getBlockAt(x, y - 1, z).getType() == Material.AIR) {
            y--;
        }

        // Ahora queremos colocar en (x, y, z) si ese bloque es aire y el de abajo (y-1) es sólido
        if (w.getBlockAt(x, y - 1, z).getType().isSolid()) {
            Location place = new Location(w, x, y, z);
            if (place.getBlock().getType() == Material.AIR) {
                return place;
            }
        }
        return null; // no hay sitio válido
    }
}
