package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.menu.buttons.ToggleMorphSelfViewButton;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.inventory.Inventory;

public class MenuMorphs extends CosmeticMenu<MorphType> {

    public MenuMorphs(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.MORPHS);
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer player, int page) {
        int slot = inventory.getSize() - (getCategory().hasGoBackArrow() ? 4 : 6);
        putItem(inventory, slot, new ToggleMorphSelfViewButton(), player);
    }
}
