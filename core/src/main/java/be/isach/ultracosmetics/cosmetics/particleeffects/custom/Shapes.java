package be.isach.ultracosmetics.cosmetics.particleeffects.custom;

import be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes.AboveHeadShape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes.AuraShape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes.HaloShape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.shapes.OrbitShape;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Shapes {

    private static final Map<String, Shape> REGISTRY = new HashMap<>();

    static {
        register("halo", new HaloShape());
        register("orbit", new OrbitShape());
        register("above-head", new AboveHeadShape());
        register("aura", new AuraShape());
    }

    private Shapes() {}

    public static Shape byName(String name) {
        if (name == null) {
            return null;
        }
        return REGISTRY.get(name.toLowerCase(Locale.ROOT));
    }

    private static void register(String name, Shape shape) {
        REGISTRY.put(name, shape);
    }
}
