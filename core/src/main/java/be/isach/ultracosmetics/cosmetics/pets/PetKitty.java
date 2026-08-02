package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Cat.Type;

public class PetKitty extends Pet {

    public PetKitty(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        return oldEnumCustomize(Type.class, customization, ((Cat) entity)::setCatType);
    }
}
