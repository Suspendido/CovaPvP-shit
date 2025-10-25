package me.keano.azurite.utils.deco.listeners;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.utils.CC;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class LoggerListener extends Module<ListenerManager> {

    private final HCF hcf;
    private final Map<Player, Map<PotionEffectType, Integer>> playerEffects = new HashMap<>();

    public LoggerListener(ListenerManager manager, HCF hcf) {
        super(manager);
        this.hcf = hcf;


        this.load();
    }

    public void load(){

    }

    @EventHandler
    public void onAttackWithSharpness(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damaged = (Player) event.getEntity();

            int sharpnessLevel = damager.getItemInHand().getEnchantments().getOrDefault(Enchantment.DAMAGE_ALL, 0);
            if (sharpnessLevel >= 7) {
                for (Player onlinePlayer : hcf.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("zeus.headstaff")) {
                        for (String s : getLanguageConfig().getStringList("STAFF_LOGS.ATTACK_SHARPNESS")) {
                            s = s
                                    .replace("%player%", damager.getName())
                                    .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(damager)))
                                    .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(damager)))
                                    .replace("%item%", String.valueOf(sharpnessLevel));

                            onlinePlayer.sendMessage(s);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (damager instanceof Player && target instanceof Player) {
            Player attacked = (Player) target;
            checkEffects(attacked);
        }
    }

    private void checkEffects(Player player) {
        Map<PotionEffectType, Integer> currentEffects = new HashMap<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            currentEffects.put(effect.getType(), effect.getAmplifier());
        }

        Map<PotionEffectType, Integer> previousEffects = playerEffects.getOrDefault(player, new HashMap<>());

        for (Map.Entry<PotionEffectType, Integer> entry : currentEffects.entrySet()) {
            PotionEffectType type = entry.getKey();
            int amplifier = entry.getValue();

            if (!previousEffects.containsKey(type) || previousEffects.get(type) < amplifier) {
                if (amplifier >= 4) {
                    alertStaff(player, type.getName(), amplifier + 1);
                }
            }
        }

        playerEffects.put(player, currentEffects);
    }

    private void alertStaff(Player player, String effectName, int level) {
        for (Player onlinePlayer : hcf.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("zeus.headstaff")) {
                for (String s : getLanguageConfig().getStringList("STAFF_LOGS.RECEIVED_EFFECT")) {
                    s = s
                            .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(player)))
                            .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(player)))
                            .replace("%player%", player.getName())
                            .replace("%effect%", effectName)
                            .replace("%level%", String.valueOf(level));

                    onlinePlayer.sendMessage(s);
                }
            }
        }
    }



}


