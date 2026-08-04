package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;

public class PetMooshroom extends Pet {
    public PetMooshroom(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        return enumCustomize(Variant.class, customization, ((MushroomCow) entity)::setVariant);
    }
}
