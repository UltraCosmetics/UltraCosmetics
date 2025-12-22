package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.MagmaCube;

/**
 * Represents an instance of a magma cube pet summoned by a player.
 *
 * @author Chris6ix
 * @since 25-09-2022
 */
public class PetMagmaCube extends Pet {
    public PetMagmaCube(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        ((MagmaCube) entity).setSize(1);
    }

    @Override
    protected boolean customize(String customization) {
        int size;
        try {
            size = Integer.parseInt(customization);
        } catch (NumberFormatException e) {
            return false;
        }
        ((MagmaCube) entity).setSize(size);
        return true;
    }
}
