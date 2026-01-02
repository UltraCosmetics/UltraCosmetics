package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.deatheffects.*;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;

public class DeathEffectType extends CosmeticParticleType<DeathEffect> {

    public DeathEffectType(String configName, XParticle effect, XMaterial material, Class<? extends DeathEffect> clazz, boolean supportsParticleMultiplier) {
        super(Category.DEATH_EFFECTS, configName, 0, effect, material, clazz, supportsParticleMultiplier);
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".name", configName);
        }
    }

    public static void register() {
        new DeathEffectType("Explosion", XParticle.EXPLOSION_EMITTER, XMaterial.TNT, DeathEffect.class, false);
        new DeathEffectType("Firework", XParticle.FIREWORK, XMaterial.FIREWORK_ROCKET, DeathEffectFirework.class, false);
        new DeathEffectType("Lightning", XParticle.CRIT, XMaterial.DAYLIGHT_DETECTOR, DeathEffectLightning.class, false);
        new DeathEffectType("PuffOfSmoke", XParticle.LARGE_SMOKE, XMaterial.SMOKER, DeathEffectPuffOfSmoke.class, true);
        new DeathEffectType("DragonBreath", XParticle.DRAGON_BREATH, XMaterial.DRAGON_BREATH, DeathEffectDragonBreath.class, false);
        new DeathEffectType("Flames", XParticle.FLAME, XMaterial.BLAZE_POWDER, DeathEffectFlames.class,false);
        new DeathEffectType("ItemExplode", XParticle.CRIT, XMaterial.FERMENTED_SPIDER_EYE, DeathEffectItemExplode.class,false);
    }
}
