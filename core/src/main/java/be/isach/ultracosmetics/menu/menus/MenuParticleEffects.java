package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.menu.CosmeticMenu;

public class MenuParticleEffects extends CosmeticMenu<ParticleEffectType> {

    public MenuParticleEffects(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.EFFECTS);
    }
}
