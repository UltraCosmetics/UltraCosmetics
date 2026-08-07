package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;

public class PetFrog extends Pet {
    public PetFrog(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        return oldEnumCustomize(Variant.class, customization, ((Frog) entity)::setVariant);
    }
}
