package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.Fox;
import org.bukkit.entity.Fox.Type;

/**
 * Represents an instance of a fox pet summoned by a player.
 *
 * @author Chris6ix
 * @since 14-01-2022
 */
public class PetFox extends Pet {
    public PetFox(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        return enumCustomize(Type.class, customization, ((Fox) entity)::setFoxType);
    }
}
