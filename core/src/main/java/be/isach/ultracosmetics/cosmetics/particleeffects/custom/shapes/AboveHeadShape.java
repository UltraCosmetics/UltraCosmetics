package be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes;

import be.isach.ultracosmetics.cosmetics.particleeffects.custom.Shape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.ShapeParams;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class AboveHeadShape implements Shape {

    @Override
    public void render(Player player, List<ParticleDisplay> displays, ShapeParams params, int tick) {
        double yOffset = params.getDouble("yOffset", 0.8);
        Location target = player.getEyeLocation().add(0, yOffset, 0);
        for (ParticleDisplay display : displays) {
            display.spawn(target);
        }
    }
}
