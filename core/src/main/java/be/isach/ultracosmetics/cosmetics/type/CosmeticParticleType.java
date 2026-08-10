package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;

public class CosmeticParticleType<T extends Cosmetic<?>> extends CosmeticType<T> {
    // The Repeat-Delay comment is only attached to the first entry auto-added to
    // config, so users see the explanation once instead of on every particle effect.
    private static boolean repeatDelayCommentWritten = false;

    private final XParticle effect;
    private final int repeatDelay;
    private final double particleMultiplier;

    public CosmeticParticleType(Category category, String configName, int repeatDelay, XParticle effect,
                                XMaterial material, Class<? extends T> clazz, boolean supportsParticleMultiplier) {
        this(category, configName, repeatDelay, effect, material, clazz, supportsParticleMultiplier, false);
    }

    public CosmeticParticleType(Category category, String configName, int repeatDelay, XParticle effect,
                                XMaterial material, Class<? extends T> clazz, boolean supportsParticleMultiplier,
                                boolean supportsRepeatDelayOverride) {
        super(category, configName, material, clazz);
        this.effect = effect;
        if (supportsRepeatDelayOverride) {
            String path = getCategory().getConfigPath() + "." + configName + ".Repeat-Delay";
            if (!SettingsManager.getConfig().isInt(path)) {
                this.repeatDelay = repeatDelay;
                if (!repeatDelayCommentWritten) {
                    SettingsManager.getConfig().set(path, repeatDelay,
                            "Ticks between particle spawns (1 tick = 1/20 s). Higher = fewer particles per",
                            "second. Minimum is 1. Applies to all particle effects. Requires server restart.");
                    repeatDelayCommentWritten = true;
                } else {
                    SettingsManager.getConfig().set(path, repeatDelay);
                }
            } else {
                this.repeatDelay = Math.max(1, SettingsManager.getConfig().getInt(path));
            }
        } else {
            this.repeatDelay = repeatDelay;
        }
        if (supportsParticleMultiplier) {
            String path = getCategory().getConfigPath() + "." + configName + ".Particle-Multiplier";
            if (!SettingsManager.getConfig().isDouble(path)) {
                particleMultiplier = 1;
                SettingsManager.getConfig().set(getCategory().getConfigPath() + "." + configName + ".Particle-Multiplier", 1.0, "A multiplier applied to the number", "of XParticle displayed. 1.0 is 100%");
            } else {
                particleMultiplier = SettingsManager.getConfig().getDouble(path);
            }
        } else {
            // particleMultiplier is final so we have to assign it a value no matter what
            particleMultiplier = 1;
        }
    }

    public XParticle getEffect() {
        return effect;
    }

    public int getRepeatDelay() {
        return repeatDelay;
    }

    public double getParticleMultiplier() {
        return particleMultiplier;
    }
}
