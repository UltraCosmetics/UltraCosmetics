package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.Donkey;

/**
 * Represents an instance of a donkey pet summoned by a player.
 *
 * @author Chris6ix
 * @since 05-09-2022
 */
public class PetDonkey extends Pet {
    public PetDonkey(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        ((Donkey) entity).setCarryingChest(customization.equalsIgnoreCase("true"));
        return true;
    }
}
