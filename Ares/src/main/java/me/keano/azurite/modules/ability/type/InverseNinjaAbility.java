package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.TeleportTask;
import org.bukkit.entity.Player;

public class InverseNinjaAbility extends Ability {

    private final int seconds;
    private final int hitsValid;

    public InverseNinjaAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Inverse Ninja"
        );
        this.seconds = getAbilitiesConfig().getInt("INVERSE_NINJA.SECONDS");
        this.hitsValid = getAbilitiesConfig().getInt("INVERSE_NINJA.HITS_VALID");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        // I already coded a way to get last damager for focus mode, so just use that.
        Player damager = ((FocusModeAbility) getManager().getAbility("FocusMode")).getDamager(player, hitsValid);

        if (damager == null) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.INVERSE_NINJA.NO_LAST_HIT"));
            return;
        }

        takeItem(player);
        applyCooldown(player);

        new TeleportTask(this, () -> {
            damager.teleport(player);

            for (String s : getLanguageConfig().getStringList("ABILITIES.INVERSE_NINJA.TELEPORTED_SUCCESSFULLY")) {
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                );
            }
        }, (i) -> {
            for (String s : getLanguageConfig().getStringList("ABILITIES.INVERSE_NINJA.TELEPORTING"))
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.INVERSE_NINJA.TELEPORTING_ATTACKER"))
                damager.sendMessage(s
                        .replace("%player%", player.getName())
                        .replace("%target%", damager.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );
        }, seconds);
    }
}