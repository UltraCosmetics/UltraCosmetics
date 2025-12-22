package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;

import com.cryptomorin.xseries.XTag;

/**
 * Represents an instance of a sheep pet summoned by a player.
 *
 * @author iSach
 * @since 08-12-2015
 */
public class PetSheep extends Pet {
    public PetSheep(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
        dropItem = ItemFactory.randomItemFromTag(XTag.WOOL);
        super.onUpdate();
    }

    @Override
    protected boolean customize(String customization) {
        return enumCustomize(DyeColor.class, customization, ((Sheep) entity)::setColor);
    }
}
