package be.isach.ultracosmetics.cosmetics.particleeffects.custom;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.entity.Player;

import java.util.List;

public interface Shape {
    void render(Player player, List<ParticleDisplay> displays, ShapeParams params, int tick);
}
