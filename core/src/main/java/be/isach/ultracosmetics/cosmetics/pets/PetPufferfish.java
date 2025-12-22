package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.PufferFish;

/**
 * Represents an instance of a pufferfish pet summoned by a player.
 *
 * @author Chris6ix
 * @since 05-09-2022
 */
public class PetPufferfish extends Pet {
    public PetPufferfish(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        int value = -1;
        try {
            value = Integer.parseInt(customization);
        } catch (NumberFormatException ignored) {
        }
        if (value < 0 || value > 2) return false;
        ((PufferFish) entity).setPuffState(value);
        return true;
    }
}
