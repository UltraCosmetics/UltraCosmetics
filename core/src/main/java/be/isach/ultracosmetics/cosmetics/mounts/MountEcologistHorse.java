package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.Horse;

public class MountEcologistHorse extends MountAbstractHorse {
    private final ParticleDisplay display;

    public MountEcologistHorse(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        display = ParticleDisplay.of(XParticle.HAPPY_VILLAGER).offset(0.4, 0.2, 0.4).withCount(5)
                .withLocationCaller(() -> entity.getLocation().add(0, 1, 0));
    }

    @Override
    public void setupEntity() {
        super.setupEntity();
        ((Horse) entity).setColor(Horse.Color.CHESTNUT);
    }

    @Override
    public void onUpdate() {
        display.spawn();
    }
}
