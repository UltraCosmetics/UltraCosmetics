package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;

import java.util.Locale;

/**
 * Represents an instance of a tropical fish pet summoned by a player.
 *
 * @author Chris6ix
 * @since 14-09-2022
 */
public class PetTropicalFish extends Pet {
    public PetTropicalFish(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        Pattern pattern;
        DyeColor bodyColor;
        DyeColor patternColor;
        String[] parts = customization.toUpperCase(Locale.ROOT).split(":");
        if (parts.length != 3) return false;
        try {
            pattern = Pattern.valueOf(parts[0]);
            bodyColor = DyeColor.valueOf(parts[1]);
            patternColor = DyeColor.valueOf(parts[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        TropicalFish fish = (TropicalFish) entity;
        fish.setPattern(pattern);
        fish.setBodyColor(bodyColor);
        fish.setPatternColor(patternColor);
        return true;
    }
}
