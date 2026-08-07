package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.menu.CosmeticMenu;

public class MenuMounts extends CosmeticMenu<MountType> {

    public MenuMounts(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.MOUNTS);
    }

}
