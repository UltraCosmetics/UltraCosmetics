package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.util.Vector;

public class ParticleEffectInferno extends ParticleEffect {

    private final float[] height = {0, 0, 2, 2};
    private final boolean[] up = {true, false, true, false};
    private final int[] steps = {0, 0, 0, 0};

    public ParticleEffectInferno(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
        display.offset(0.15).withCount(getModifiedAmount(4));
    }

    @Override
    public void onUpdate() {
        for (int i = 0; i < 4; i++) {
            if (up[i]) {
                if (height[i] < 2) {
                    height[i] += 0.05f;
                } else {
                    up[i] = false;
                }
            } else {
                if (height[i] > 0) {
                    height[i] -= 0.05f;
                } else {
                    up[i] = true;
                }
            }
            double inc = (2 * Math.PI) / 100;
            double angle = steps[i] * inc + ((i + 1) % 2 == 0 ? 45 : 0);
            Vector v = new Vector(Math.cos(angle) * 1.1, height[i], Math.sin(angle) * 1.1);
            display.spawn(getPlayer().getLocation().add(v));
            if (i == 0 || i == 3) {
                steps[i] -= 4;
            } else {
                steps[i] += 4;
            }
        }
    }
}
