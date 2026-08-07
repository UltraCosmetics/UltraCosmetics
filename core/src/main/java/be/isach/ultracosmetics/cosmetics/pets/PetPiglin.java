package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PiglinBarterEvent;

public class PetPiglin extends Pet {
    public PetPiglin(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        Piglin piglin = (Piglin) entity;
        piglin.setImmuneToZombification(true);
        for (Material interest : piglin.getInterestList()) {
            piglin.removeMaterialOfInterest(interest);
        }
    }

    @EventHandler
    public void onBarter(PiglinBarterEvent event) {
        if (event.getEntity() == entity) {
            event.setCancelled(true);
        }
    }
}
