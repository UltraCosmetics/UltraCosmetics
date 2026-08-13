package be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes;

import be.isach.ultracosmetics.cosmetics.particleeffects.custom.Shape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.ShapeParams;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class HaloShape implements Shape {

    @Override
    public void render(Player player, List<ParticleDisplay> displays, ShapeParams params, int tick) {
        double radius = Math.max(0.05, params.getDouble("radius", 0.4));
        int points = Math.max(1, params.getInt("points", 20));
        double yOffset = params.getDouble("yOffset", 0.7);

        Location center = player.getEyeLocation().add(0, yOffset, 0);
        double step = (2 * Math.PI) / points;
        for (int i = 0; i < points; i++) {
            double angle = i * step;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location target = center.clone().add(x, 0, z);
            for (ParticleDisplay display : displays) {
                display.spawn(target);
            }
        }
    }
}
