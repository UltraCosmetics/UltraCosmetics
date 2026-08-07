package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ParticleEffectMagicalRods extends ParticleEffect {

    private static final List<Color> COLORS = new ArrayList<>();

    static {
        COLORS.add(Color.GREEN);
        COLORS.add(new Color(0, 128, 0));
        COLORS.add(new Color(0, 74, 0));
        COLORS.add(new Color(0, 36, 0));
    }

    private static final double RADIUS = 1.1; // radius between player and rods
    private static final double ROD_HEIGHT = 1; // Height of each height
    private static final int TOTAL_COLUMNS = 8; // Amount of rods (columns)
    private static final double BASE_HEIGHT = 0.4; // Added to avoid rods in the floor.
    private static final double MIN_HEIGHT = 0; // Min height...
    private static final double MAX_HEIGHT = 0.6; // Max height...
    private static final double HEIGHT_STEP = 0.03; // Height step...
    private static final double MAX_HEIGHT_DIFF = 0.5; // Max height diff between columns
    private static final double HEIGHT_DIFF_STEP = 0.04; // Height diff step...

    private boolean heightDirectionUp; // Indicates whether the "overall" height is going up or down
    private boolean hoveringDirectionUp;
            // Indicates whether the height diff between columns is going up or down (gives dynamism)
    private double height = 0; // Current height
    private double angle = 0; // Current angle
    private double heightDiffFactor = MAX_HEIGHT_DIFF;
            // Height diff between columns. Variates over time with hoveringDirectionUp.

    public ParticleEffectMagicalRods(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
    }

    @Override
    public void onUpdate() {
        if (heightDirectionUp) {
            if (height < MAX_HEIGHT) {
                height += HEIGHT_STEP;
            } else {
                heightDirectionUp = false;
            }
        } else {
            if (height > MIN_HEIGHT) {
                height -= HEIGHT_STEP;
            } else {
                heightDirectionUp = true;
            }
        }
        if (hoveringDirectionUp) {
            if (heightDiffFactor < MAX_HEIGHT_DIFF) {
                heightDiffFactor += HEIGHT_DIFF_STEP;
            } else {
                hoveringDirectionUp = false;
            }
        } else {
            if (heightDiffFactor > -MAX_HEIGHT_DIFF) {
                heightDiffFactor -= HEIGHT_DIFF_STEP;
            } else {
                hoveringDirectionUp = true;
            }
        }

        drawColumns(height, angle);

        angle += Math.toRadians(1);
    }

    @Override
    public void showAlternativeEffect() {
        Vector left = getPlayer().getLocation().getDirection().setY(0).crossProduct(new Vector(0, 1, 0));
        double multiplier = 0.3;
        for (Color color : COLORS) {
            display.withColor(color)
                    .spawn(getPlayer().getLocation().add(left.clone().multiply(multiplier)).add(0, 0.1, 0));
            multiplier -= 0.2;
        }
    }

    /**
     * Draws the rods around the player.
     *
     * @param height    The current height to work with.
     * @param suppAngle Angle rotation step
     */
    private void drawColumns(Double height, double suppAngle) {
        int amount = getModifiedAmount(((int) ROD_HEIGHT) * 5);
        int cycles = TOTAL_COLUMNS / COLORS.size();
        double workingSpace = 2 * Math.PI / cycles; // Each cycle has its angle span.
        double startAngle = 0; // Step angle for each cycle.
        Vector v = new Vector();
        Location loc;

        for (int i = 0; i < cycles; i++) {
            double angleStep = startAngle; // Angle for each column.
            for (int j = 0; j < COLORS.size(); j++) {
                v.setX(Math.cos(angleStep + suppAngle) * RADIUS);
                v.setZ(Math.sin(angleStep + suppAngle) * RADIUS);
                v.setY(BASE_HEIGHT +
                        Math.sin(angleStep * 3) * heightDiffFactor); // The height of the columns is a sine wave.
                loc = getPlayer().getLocation().add(v);

                drawParticleLine(loc, loc.clone().add(0, ROD_HEIGHT, 0), amount, COLORS.get(j));

                angleStep += workingSpace / COLORS.size();
                height += (i >= 3 && i <= 5) ? heightDiffFactor : -heightDiffFactor;
            }
            startAngle += workingSpace;
        }
    }

    public void drawParticleLine(Location from, Location to, int particles, Color color) {
        Location location = from.clone();
        Location target = to.clone();
        Vector link = target.toVector().subtract(location.toVector());
        float length = (float) link.length();
        link.normalize();

        float ratio = length / particles;
        Vector v = link.multiply(ratio);
        Location loc = location.clone().subtract(v);
        int step = 0;
        for (int i = 0; i < particles; i++) {
            if (step >= (double) particles) {
                step = 0;
            }
            step++;
            loc.add(v);
            display.withColor(color).spawn(loc);
        }
    }
}
