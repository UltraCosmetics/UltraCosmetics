package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;

public class PetWarden extends Pet {
    private final boolean blockEffect = SettingsManager.getConfig().getBoolean(getOptionPath("Block-Effect"));

    public PetWarden(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @EventHandler
    public void onDarkness(EntityPotionEffectEvent event) {
        if (!blockEffect) {
            return;
        }
        if (event.getEntityType() == EntityType.PLAYER && event.getCause() == Cause.WARDEN
                && event.getEntity().getLocation().distanceSquared(entity.getLocation()) < 22 * 22) {
            event.setCancelled(true);
        }
    }
}
