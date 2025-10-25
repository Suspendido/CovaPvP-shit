package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class HookAbility extends Ability {

    public HookAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Hook"
        );
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();

        if (e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        if (e.getCaught() == null) return;
        if (!(e.getCaught() instanceof Player)) return;

        if (!hasAbilityInHand(player)) return;
        if (cannotUse(player) || hasCooldown(player)) {
            e.setCancelled(true);
            return;
        }

        Player target = (Player) e.getCaught();
        applyCooldown(player);

        // Fuerza II por 6 segundos al caster
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INCREASE_DAMAGE,
                6 * 20,
                1,
                false,
                false
        ));

        // Sonidos
        target.playSound(target.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // ===============================
        // Vector de atracción mejorado (Hard Pull estilo HCF)
        // ===============================
        Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector());
        double distance = player.getLocation().distance(target.getLocation());

        // Normalizamos dirección
        direction.normalize();

        // Escalamos por distancia (más fuerte)
        double pullStrength = Math.max(0.8, Math.min(distance * 0.35, 3.0));

        // Aplicamos el vector multiplicado por fuerza
        Vector velocity = direction.multiply(pullStrength);

        // Ajuste vertical más bajo
        velocity.setY(0.15 + (distance * 0.02));
        if (velocity.getY() > 0.6) {
            velocity.setY(0.6); // Máximo en Y
        }

        // Limitar velocidad máxima más alta (hard pull)
        double maxVelocity = 3.2;
        if (velocity.length() > maxVelocity) {
            velocity = velocity.normalize().multiply(maxVelocity);
        }

        // Aplicamos la velocidad final
        target.setVelocity(velocity);
    }
}
