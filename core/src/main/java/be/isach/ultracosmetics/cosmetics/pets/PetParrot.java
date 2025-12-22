package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;

/**
 * Represents an instance of a parrot pet summoned by a player.
 *
 * @author RadBuilder
 * @since 07-02-2017
 */
public class PetParrot extends Pet {
    public PetParrot(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        return enumCustomize(Variant.class, customization, ((Parrot) entity)::setVariant);
    }
}
