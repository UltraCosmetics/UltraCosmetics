package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.EmoteType;
import be.isach.ultracosmetics.menu.CosmeticMenu;

public class MenuEmotes extends CosmeticMenu<EmoteType> {

    public MenuEmotes(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.EMOTES);
    }
}
