package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.awt.*;

/**
 * Represents an instance of super villain particles summoned by a player.
 *
 * @author iSach
 * @since 11-11-2015
 */
public class ParticleEffectSuperVillain extends ParticleEffect {

    private final ParticleDisplay secondary;

    public ParticleEffectSuperVillain(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        display.withColor(Color.BLACK).withCount(getModifiedAmount(3));
        secondary = ParticleDisplay.of(XParticle.ELECTRIC_SPARK).offset(0.15, 0.1, 0.15)
                .withEntity(getPlayer()).withCount(getModifiedAmount(4));
    }

    private final boolean[][] shape = {
            {false, true,  true,  true,  false,},
            {false, true,  true,  true,  false,},
            {true,  true,  true,  true,  true,},
            {true,  true,  true,  true,  true,},
            {true,  true,  true,  true,  true,},
            {true,  true,  true,  true,  true,},
            {true,  true,  false, true,  true,},
            {true,  false, false, false, true,},
    };

    @Override
    public void onUpdate() {
        drawParticles(getPlayer().getLocation());
        secondary.spawn();
    }


    private void drawParticles(Location location) {
        double space = 0.2;
        double defX = location.getX() - (space * shape[0].length / 2) + space / 2;
        double x = defX;
        double defY = location.getY() + 1.5;
        double y = defY;
        double angle = -((location.getYaw() + 180) / 60);
        angle += (location.getYaw() < -180 ? 3.25 : 2.985);
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j]) {
                    Location target = location.clone();
                    target.setX(x);
                    target.setY(y);

                    Vector v = target.toVector().subtract(location.toVector());
                    Vector v2 = getBackVector(location);
                    v = rotateAroundAxisY(v, angle);
                    double iT = ((double) i) / 10;
                    v2.setY(0).multiply(-0.2 - iT);

                    Location loc = location.clone();

                    loc.add(v);
                    loc.add(v2);
                    if (isMoving()) {
                        loc.setY(defY);
                    }

                    display.spawn(loc);
                    loc.subtract(v2);
                    loc.subtract(v);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }

    public static Vector rotateAroundAxisY(Vector v, double angle) {
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static final Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    public static final Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    private static Vector getBackVector(Location loc) {
        final double rads = Math.toRadians(loc.getYaw() + 90);
        return new Vector(Math.cos(rads), 0, Math.sin(rads));
    }
}
