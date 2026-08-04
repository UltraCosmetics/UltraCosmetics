package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.EntityBlockFormEvent;

public class PetSnowman extends Pet {
    public PetSnowman(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @EventHandler
    public void onTrail(EntityBlockFormEvent event) {
        if (event.getEntity() == entity) {
            event.setCancelled(true);
        }
    }

    @Override
    protected boolean customize(String customization) {
        if (customization.equalsIgnoreCase("true")) {
            ((Snowman) entity).setDerp(true);
        }
        return true;
    }
}
