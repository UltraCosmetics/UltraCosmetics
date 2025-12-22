package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

import java.util.Locale;

/**
 * Represents an instance of a dog pet summoned by a player.
 *
 * @author iSach
 * @since 08-12-2015
 */
public class PetDog extends Pet {
    private boolean fixedColor = false;

    public PetDog(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!fixedColor) {
            ((Wolf) entity).setCollarColor(DyeColor.values()[RANDOM.nextInt(16)]);
        }
    }

    @Override
    protected boolean customize(String customization) {
        String[] parts = customization.split(":", 2);
        Wolf wolf = (Wolf) entity;
        fixedColor = enumCustomize(DyeColor.class, parts[0], wolf::setCollarColor);
        if (fixedColor && parts.length > 1) {
            fixedColor = oldEnumCustomize(Wolf.Variant.class, parts[1].toUpperCase(Locale.ROOT), wolf::setVariant);
        }
        return fixedColor;
    }
}
