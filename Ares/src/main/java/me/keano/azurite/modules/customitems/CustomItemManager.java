package me.keano.azurite.modules.customitems;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.customitems.listener.CustomItemListener;
import me.keano.azurite.modules.customitems.type.EndlessOrb;
import me.keano.azurite.modules.customitems.type.SplashOfRegret;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 21/01/2025
 */

@Getter
public class CustomItemManager extends Manager implements Listener {

    private final Map<String, CustomItem> items;

    public CustomItemManager(HCF instance) {
        super(instance);

        this.items = new HashMap<>();
        this.load();

        this.registerListener(this);
        getInstance().getServer().getPluginManager().registerEvents(new CustomItemListener(this), instance);
    }

    public void registerItem(CustomItem item) {
        items.put(item.getId(), item);
    }

    public CustomItem getItemById(String id) {
        return items.get(id);
    }

    @Override
    public void disable() {
        items.clear();
    }

    @Override
    public void reload() {
        items.clear();
        this.load();
    }

    private void load() {
        new EndlessOrb(this, this.getInstance());
        new SplashOfRegret(this, this.getInstance());
    }

    public Map<String, CustomItem> getCustomItems() {
        return items;
    }
}
