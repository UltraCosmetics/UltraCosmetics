package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ParticleEffectFrostLord extends ParticleEffect {
    private final XSound.SoundPlayer sound;
    private final ParticleDisplay secondaryDisplay;
    private int step = 0;
    private float stepY = 0;
    private float radius = 1.5f;

    public ParticleEffectFrostLord(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
        sound = XSound.BLOCK_SNOW_BREAK.record().withVolume(0.5f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
        secondaryDisplay = ParticleDisplay.of(getType().getEffect()).withCount(getModifiedAmount(48)).withExtra(0.3);
    }

    @Override
    public void onUpdate() {
        for (int i = 0; i < 6; i++) {
            Location location = getPlayer().getLocation();
            double inc = (2 * Math.PI) / 100;
            double angle = step * inc + stepY + i;
            Vector v = new Vector();
            v.setX(Math.cos(angle) * radius);
            v.setZ(Math.sin(angle) * radius);
            display.spawn(location.clone().add(v).add(0, stepY, 0));
            if (stepY < 3) {
                radius -= 0.022f;
                stepY += 0.045f;
            } else {
                stepY = 0;
                step = 0;
                radius = 1.5f;
                sound.play();
                secondaryDisplay.spawn(location.clone().add(0, 3, 0));
            }
        }
    }
}
