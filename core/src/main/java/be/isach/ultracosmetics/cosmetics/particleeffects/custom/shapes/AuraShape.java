package be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes;

import be.isach.ultracosmetics.cosmetics.particleeffects.custom.Shape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.ShapeParams;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class AuraShape implements Shape {

    @Override
    public void render(Player player, List<ParticleDisplay> displays, ShapeParams params, int tick) {
        double radius = Math.max(0.05, params.getDouble("radius", 0.35));
        double topY = params.getDouble("topY", 1.2);
        double bottomY = params.getDouble("bottomY", 0.2);
        int bands = Math.max(1, params.getInt("bands", 2));

        Location base = player.getLocation();
        double step = bands == 1 ? 0 : (topY - bottomY) / (bands - 1);
        for (int i = 0; i < bands; i++) {
            double y = bottomY + step * i;
            Location target = base.clone().add(0, y, 0);
            for (ParticleDisplay display : displays) {
                display.offset(radius, 0.05, radius).spawn(target);
            }
        }
    }
}
