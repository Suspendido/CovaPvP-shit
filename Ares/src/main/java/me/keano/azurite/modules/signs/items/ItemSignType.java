package me.keano.azurite.modules.signs.items;

import me.keano.azurite.modules.framework.Manager;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public enum ItemSignType {

    KOTH_CAPTURE_SIGN,
    DEATH_SIGN;

    public boolean isEnabled(Manager manager) {
        return manager.getConfig().getBoolean("SIGNS_CONFIG." + name() + ".ENABLED");
    }

    public String getItemName(Manager manager) {
        return manager.getConfig().getString("SIGNS_CONFIG." + name() + ".NAME");
    }

    public List<String> getLines(Manager manager) {
        return manager.getConfig().getStringList("SIGNS_CONFIG." + name() + ".LINES");
    }
}