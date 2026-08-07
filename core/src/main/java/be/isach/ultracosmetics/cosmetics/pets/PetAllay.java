package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PetAllay extends Pet {
    public PetAllay(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == entity) {
            event.setCancelled(true);
        }
    }

    @Override
    protected boolean customize(String customization) {
        return customizeHeldItem(customization);
    }
}
