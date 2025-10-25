package me.keano.azurite.modules.payouts;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.payouts.menu.PayoutsMenu;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * This source can't be redistributed without
 * authorization of the owner
 *
 * @author RodriDevs © 2025
 * Date: 27/02/2025
 * Project: Zeus
 */

@Getter
@Setter
public class PayoutsManager extends Manager {

    private PayoutsMenu payoutsMenu;
    private List<ItemStack> payouts;

    public PayoutsManager(HCF instance) {
        super(instance);
        this.payouts = new ArrayList<>();
        this.payoutsMenu = new PayoutsMenu(instance.getListenerManager(), instance);
        loadPayoutsFromConfig();
    }

    public void reload() {
        loadPayoutsFromConfig();
        payoutsMenu.reloadItems();
    }

    private void loadPayoutsFromConfig() {
        FileConfiguration config = getMenusConfig();
        payouts = (List<ItemStack>) config.getList("PAYOUTS_MENU.ITEMS", new ArrayList<>());
    }
}