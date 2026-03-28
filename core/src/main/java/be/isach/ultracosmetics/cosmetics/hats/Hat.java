package be.isach.ultracosmetics.cosmetics.hats;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.ArmorCosmetic;
import be.isach.ultracosmetics.cosmetics.suits.ArmorSlot;
import be.isach.ultracosmetics.cosmetics.type.HatType;
import be.isach.ultracosmetics.player.UltraPlayer;

/**
 * Represents an instance of a hat summoned by a player.
 *
 * @author iSach
 * @since 08-23-2016
 */
public class Hat extends ArmorCosmetic<HatType> {

    public Hat(UltraPlayer owner, HatType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        setItemStack(type.getItemStack());
    }

    @Override
    protected void onEquip() {
        // Setting the item is done in ArmorCosmetic#tryEquip
    }

    @Override
    protected ArmorSlot getArmorSlot() {
        return ArmorSlot.HELMET;
    }
}
