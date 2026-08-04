package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.menu.buttons.ToggleGadgetsButton;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.inventory.Inventory;

public class MenuGadgets extends CosmeticMenu<GadgetType> {

    public MenuGadgets(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.GADGETS);
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer player, int page) {
        if (SettingsManager.getConfig().getBoolean("Categories.Gadgets.Allow-Disable-Gadgets", true)) {
            int slot = inventory.getSize() - (getCategory().hasGoBackArrow() ? 4 : 6);
            putItem(inventory, slot, new ToggleGadgetsButton(), player);
        }
    }
}
