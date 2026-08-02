package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;

public class ParticleEffectEnderAura extends ParticleEffect {
    private final ParticleDisplay displayA = ParticleDisplay.of(XParticle.PORTAL)
            .offset(0.35, 0.05, 0.35)
            .withExtra(0.1)
            .withCount(getModifiedAmount(5))
            .withLocationCaller(() -> getPlayer().getLocation().add(0, 1.2, 0));
    private final ParticleDisplay displayB =
            displayA.copy().withLocationCaller(() -> getPlayer().getLocation().add(0, 0.2, 0));

    public ParticleEffectEnderAura(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
        displayA.spawn();
        displayB.spawn();
    }
}
