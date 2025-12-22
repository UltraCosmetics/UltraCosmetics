package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.reflection.XReflection;

public class ParticleEffectEnderFootprints extends ParticleEffectFootprints {
    public ParticleEffectEnderFootprints(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        if (XReflection.supports(21, 9)) {
            display.withRawData(0f); // idk
        }
    }
}
