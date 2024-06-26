package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Represents an instance of a allay pet summoned by a player.
 *
 * @author Chris6ix
 * @since 08-06-2022
 */
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
    public boolean customize(String customization) {
        return customizeHeldItem(customization);
    }
}
