package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.Mule;

/**
 * Represents an instance of a mule pet summoned by a player.
 *
 * @author Chris6ix
 * @since 05-09-2022
 */
public class PetMule extends Pet {
    public PetMule(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        if (customization.equalsIgnoreCase("true")) {
            ((Mule) entity).setCarryingChest(true);
        }
        return true;
    }
}
