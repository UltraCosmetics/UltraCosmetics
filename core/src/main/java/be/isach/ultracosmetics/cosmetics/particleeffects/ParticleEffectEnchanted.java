package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;

public class ParticleEffectEnchanted extends ParticleEffect {
    private final ParticleDisplay display = ParticleDisplay.of(XParticle.ENCHANT)
            .withLocationCaller(() -> getPlayer().getLocation().add(0, MathUtils.randomDouble(0.1, 2), 0))
            .withCount(getModifiedAmount(60))
            .withExtra(8f);

    public ParticleEffectEnchanted(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
        display.spawn();
    }
}
