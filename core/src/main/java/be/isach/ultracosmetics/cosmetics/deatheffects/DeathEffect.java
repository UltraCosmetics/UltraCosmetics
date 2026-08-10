package be.isach.ultracosmetics.cosmetics.deatheffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.DeathEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEffect extends Cosmetic<DeathEffectType> {
    protected ParticleDisplay display;
    // Location of the victim killed by the owner; refreshed on each kill event.
    protected Location targetLocation;

    public DeathEffect(UltraPlayer owner, DeathEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        display = ParticleDisplay.of(getType().getEffect()).withLocationCaller(() -> targetLocation);
    }

    @Override
    protected void onEquip() {
        // Lightning and fireworks aren't really particles so we don't need to warn about disabled particles for now
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == getPlayer()) {
            targetLocation = event.getEntity().getLocation();
            displayParticles();
        }
    }

    public void displayParticles() {
        display.spawn();
    }
}
