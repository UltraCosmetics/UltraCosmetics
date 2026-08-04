package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import org.bukkit.Location;

import java.awt.*;

public class ParticleEffectSantaHat extends ParticleEffect {

    public ParticleEffectSantaHat(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
        Location location = getPlayer().getEyeLocation().add(0, 0.3, 0);
        float radius = 0.25f;
        drawCircle(radius + 0.1f, -0.05f, location, Color.WHITE);
        for (int i = 0; i < 5; i++) {
            double x = MathUtils.randomDouble(-0.05, 0.05);
            double z = MathUtils.randomDouble(-0.05, 0.05);
            location.add(x, 0.46f, z);
            display.withColor(Color.WHITE).spawn(location);
            location.subtract(x, 0.46f, z);
        }
        for (float f = 0; f <= 0.4f; f += 0.1f) {
            if (radius >= 0) {
                drawCircle(radius, f, location, Color.RED);
                radius -= 0.09f;
            }
        }
    }

    private void drawCircle(float radius, float height, Location location, Color color) {
        int particles = getModifiedAmount(12);
        for (int i = 0; i < particles; i++) {
            double inc = (2 * Math.PI) / particles;
            float angle = (float) (i * inc);
            float x = MathUtils.cos(angle) * radius;
            float z = MathUtils.sin(angle) * radius;
            location.add(x, height, z);
            display.withColor(color).spawn(location);
            location.subtract(x, height, z);
        }
    }
}
