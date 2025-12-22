package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.Creeper;

/**
 * Represents an instance of a creeper pet summoned by a player.
 *
 * @author Chris6ix
 * @since 12-04-2022
 */
public class PetCreeper extends Pet {
    public PetCreeper(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        if (customization.equalsIgnoreCase("true")) {
            ((Creeper) entity).setPowered(true);
        }
        return true;
    }
}
