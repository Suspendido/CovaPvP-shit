package me.keano.azurite.modules.hooks.tags.type;

import me.keano.azurite.modules.hooks.tags.Tag;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class NoneTag implements Tag {

    @Override
    public String getTag(Player player) {
        return "";
    }
}