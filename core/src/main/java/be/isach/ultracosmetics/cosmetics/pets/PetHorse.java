package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;

import java.util.Locale;

/**
 * Represents an instance of a horse pet summoned by a player.
 *
 * @author Chris6ix
 * @since 06-04-2022
 */
public class PetHorse extends Pet {
    public PetHorse(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        String[] parts = customization.split(":");
        Color color;
        Style style = Style.NONE;
        try {
            color = Color.valueOf(parts[0].toUpperCase(Locale.ROOT));
            if (parts.length > 1) {
                style = Style.valueOf(parts[1].toUpperCase(Locale.ROOT));
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        Horse horse = (Horse) entity;
        horse.setColor(color);
        horse.setStyle(style);
        return true;
    }
}
