package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.util.Vector;

public class ParticleEffectGreenSparks extends ParticleEffect {

    private boolean up;
    private double height;
    private int step;

    public ParticleEffectGreenSparks(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
    }

    @Override
    public void onUpdate() {
        if (up) {
            if (height < 2) {
                height += 0.05f;
            } else {
                up = false;
            }
        } else {
            if (height > 0) {
                height -= 0.05;
            } else {
                up = true;
            }
        }
        double inc = (2 * Math.PI) / 100;
        double angle = step * inc;
        Vector v = new Vector();
        v.setX(Math.cos(angle) * 1.1);
        v.setZ(Math.sin(angle) * 1.1);
        display.spawn(getPlayer().getLocation().add(v).add(0, height, 0));
        step += 4;
    }
}
