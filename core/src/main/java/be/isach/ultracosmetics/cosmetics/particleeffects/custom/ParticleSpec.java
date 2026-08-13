package be.isach.ultracosmetics.cosmetics.particleeffects.custom;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;

import java.awt.Color;
import java.util.Map;

public class ParticleSpec {

    private final XParticle particle;
    private final Color color;
    private final int count;
    private final double extra;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    private ParticleSpec(XParticle particle, Color color, int count, double extra,
                         double offsetX, double offsetY, double offsetZ) {
        this.particle = particle;
        this.color = color;
        this.count = count;
        this.extra = extra;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public static ParticleSpec fromMap(Map<?, ?> map) {
        Object typeObj = map.get("type");
        if (typeObj == null) {
            throw new IllegalArgumentException("particle entry missing 'type'");
        }
        String typeName = String.valueOf(typeObj);
        XParticle particle = XParticle.of(typeName).orElseThrow(
                () -> new IllegalArgumentException("Unknown particle type: " + typeName));

        Color color = null;
        Object colorObj = map.get("color");
        if (colorObj != null) {
            color = parseColor(String.valueOf(colorObj));
        }
        int count = asInt(map.get("count"), 1);
        double extra = asDouble(map.get("extra"), 0);
        double ox = asDouble(map.get("offsetX"), 0);
        double oy = asDouble(map.get("offsetY"), 0);
        double oz = asDouble(map.get("offsetZ"), 0);
        return new ParticleSpec(particle, color, count, extra, ox, oy, oz);
    }

    public ParticleDisplay buildDisplay() {
        ParticleDisplay display = ParticleDisplay.of(particle)
                .withCount(count)
                .withExtra(extra)
                .offset(offsetX, offsetY, offsetZ);
        if (color != null) {
            display.withColor(color);
        }
        return display;
    }

    public XParticle getParticle() {
        return particle;
    }

    private static Color parseColor(String raw) {
        String s = raw.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        try {
            return new Color(Integer.parseInt(s, 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid color '" + raw + "', expected hex like #FF0000");
        }
    }

    private static int asInt(Object o, int def) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return def;
    }

    private static double asDouble(Object o, double def) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return def;
    }
}
