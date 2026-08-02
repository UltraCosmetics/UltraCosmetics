package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;

public class ParticleEffectCloud extends ParticleEffect {
    private final ParticleDisplay secondary;

    public ParticleEffectCloud(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
        display.offset(0.25, 0.05, 0.25).withLocationCaller(() -> getPlayer().getLocation().add(0, 3, 0));
        secondary = ParticleDisplay.of(XParticle.CLOUD).offset(0.5, 0.1, 0.5).withCount(getModifiedAmount(10))
                .withLocationCaller(() -> getPlayer().getLocation().add(0, 3, 0));
    }

    @Override
    public void onUpdate() {
        display.spawn();
        secondary.spawn();
    }
}
