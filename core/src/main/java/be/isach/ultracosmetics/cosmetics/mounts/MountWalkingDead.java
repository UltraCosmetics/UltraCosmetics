package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;

import java.awt.*;

public class MountWalkingDead extends MountAbstractHorse {
    private final ParticleDisplay enchantedDisplay;
    private final ParticleDisplay entityEffectDisplay;

    public MountWalkingDead(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        enchantedDisplay = ParticleDisplay.of(XParticle.ENCHANTED_HIT).offset(0.4, 0.2, 0.4).withCount(5)
                .withLocationCaller(() -> entity.getLocation().add(0, 1, 0));
        entityEffectDisplay = enchantedDisplay.copy().withParticle(XParticle.ENTITY_EFFECT);
    }

    @Override
    public void onUpdate() {
        enchantedDisplay.spawn();
        entityEffectDisplay.withColor(new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256))).spawn();
    }
}
