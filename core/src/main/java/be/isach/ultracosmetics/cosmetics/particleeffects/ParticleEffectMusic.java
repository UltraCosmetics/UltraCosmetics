package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Location;

public class ParticleEffectMusic extends ParticleEffect {

    public ParticleEffectMusic(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        display.offset(0.2).withExtra(1).withLocationCaller(() -> getPlayer().getLocation().add(0, 1.25, 0));
    }

    @Override
    public void onUpdate() {
        Location loc = getPlayer().getLocation().add(0, 1.25, 0);
        for (int i = 0; i < getModifiedAmount(12); i++) {
            display.withNoteColor(RANDOM.nextInt(25)).spawn();
        }
    }
}
