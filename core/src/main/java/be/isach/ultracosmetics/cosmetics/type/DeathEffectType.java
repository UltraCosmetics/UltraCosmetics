package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.deatheffects.DeathEffect;
import be.isach.ultracosmetics.cosmetics.deatheffects.DeathEffectExplosion;
import be.isach.ultracosmetics.util.Particles;

import com.cryptomorin.xseries.XMaterial;

public class DeathEffectType extends CosmeticParticleType<DeathEffect> {

    public DeathEffectType(String configName, Particles effect, XMaterial material, Class<? extends DeathEffect> clazz, boolean supportsParticleMultiplier) {
        super(Category.DEATH_EFFECTS, configName, 0, effect, material, clazz, supportsParticleMultiplier);
    }

    public static void register() {
        new DeathEffectType("Explosion", Particles.EXPLOSION_HUGE, XMaterial.TNT, DeathEffectExplosion.class, false);
    }
}