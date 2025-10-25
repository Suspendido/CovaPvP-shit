package me.keano.azurite.modules.hooks.placeholder;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface Placeholder {

    String replace(Player player, String string);

    List<String> replace(Player player, List<String> list);
}