package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.ArrayList;
import java.util.List;

public class GadgetFunGun extends Gadget {
    private static final ParticleDisplay LAVA = ParticleDisplay.of(XParticle.LAVA).withCount(16).offset(1.3, 1, 1.3);
    private static final ParticleDisplay HEART = ParticleDisplay.of(XParticle.HEART).withCount(20).offset(0.8);
    private final List<Projectile> projectiles = new ArrayList<>();
    private final XSound.SoundPlayer sound;

    public GadgetFunGun(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        this.sound = XSound.ENTITY_CAT_PURREOW.record().withVolume(1.4f).withPitch(1.5f).soundPlayer()
                .forPlayers(getPlayer());
    }

    @Override
    protected void onRightClick() {
        for (int i = 0; i < 5; i++) {
            projectiles.add(getPlayer().launchProjectile(Snowball.class));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (!projectiles.contains(projectile)) {
            return;
        }

        Location location = projectile.getLocation();

        for (Projectile snowball : projectiles) {
            snowball.remove();
        }

        LAVA.spawn(location);
        HEART.spawn(location);
        sound.play();
    }
}
