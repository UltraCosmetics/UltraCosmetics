package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.WanderingTrader;

public class PetWanderingTrader extends Pet {
    public PetWanderingTrader(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        ((WanderingTrader) entity).setDespawnDelay(0);
    }

    @Override
    public boolean useArmorStandNameTag() {
        return true;
    }

    @Override
    public boolean useMarkerArmorStand() {
        // See https://github.com/UltraCosmetics/UltraCosmetics/issues/88
        // Wandering trader hitbox is messed up I guess?
        // I couldn't find a related bug on the MC Java issue tracker.
        return false;
    }
}
