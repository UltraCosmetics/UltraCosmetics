package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.awt.Color;

/**
 * Represents an instance of blood helix particles summoned by a player.
 *
 * @author iSach
 * @since 08-12-2015
 */
public class ParticleEffectBloodHelix extends ParticleEffect {
    private double i = 0;

    public ParticleEffectBloodHelix(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
        display.withColor(Color.RED);
        alternativeEffect.withColor(Color.RED);
    }

    @Override
    public void onUpdate() {
        Location location = getPlayer().getLocation();
        double radius = 1.1d;
        int steps = getModifiedAmount(100);
        double interval = (2 * Math.PI) / steps;
        for (int step = 0; step < steps; step += 4) {
            double angle = step * interval + i;
            Vector v1 = new Vector();
            Vector v2 = new Vector();
            v1.setX(Math.cos(angle) * radius);
            v1.setZ(Math.sin(angle) * radius);
            v2.setX(Math.cos(angle + 3.5) * radius);
            v2.setZ(Math.sin(angle + 3.5) * radius);
            display.spawn(location.clone().add(v1));
            display.spawn(location.clone().add(v2));
            location.add(0, 0.12d, 0);
            radius -= 4.4f / steps;
        }
        i += 0.05;
    }
}
