package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffect;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectBasicTrail;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectChristmas;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectHelix;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectNote;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectRainbow;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectRedstone;
import be.isach.ultracosmetics.cosmetics.projectileeffects.ProjectileEffectRedstoneHelix;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;

public class ProjectileEffectType extends CosmeticParticleType<ProjectileEffect> {

    public ProjectileEffectType(String configName, int repeatDelay, XParticle effect, XMaterial material, Class<? extends ProjectileEffect> clazz, boolean supportsParticleMultiplier) {
        this(configName, repeatDelay, effect, material, clazz);
    }

    public ProjectileEffectType(String configName, int repeatDelay, XParticle effect, XMaterial material, Class<? extends ProjectileEffect> clazz) {
        super(Category.PROJECTILE_EFFECTS, configName, repeatDelay, effect, material, clazz, false);
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".name", configName);
        }
    }

    public static void register() {
        // Rainbow Trail
        new ProjectileEffectType("Rainbow", 1, XParticle.DUST, XMaterial.LIME_WOOL, ProjectileEffectRainbow.class);

        // Christmas Trail
        new ProjectileEffectType("Christmas", 1, XParticle.DUST, XMaterial.SNOW_BLOCK, ProjectileEffectChristmas.class);

        // Helix Trails
        new ProjectileEffectType("RedstoneHelix", 1, XParticle.DUST, XMaterial.REDSTONE_BLOCK, ProjectileEffectRedstoneHelix.class);
        new ProjectileEffectType("FlameHelix", 1, XParticle.FLAME, XMaterial.FIRE_CHARGE, ProjectileEffectHelix.class);
        new ProjectileEffectType("CursedHelix", 1, XParticle.WITCH, XMaterial.PURPLE_WOOL, ProjectileEffectHelix.class);
        new ProjectileEffectType("SoulFireHelix", 1, XParticle.SOUL_FIRE_FLAME, XMaterial.SOUL_CAMPFIRE, ProjectileEffectHelix.class);

        // Basic Trails
        new ProjectileEffectType("Spark", 1, XParticle.FIREWORK, XMaterial.FIREWORK_ROCKET, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Redstone", 1, XParticle.DUST, XMaterial.REDSTONE, ProjectileEffectRedstone.class);
        new ProjectileEffectType("Flame", 1, XParticle.FLAME, XMaterial.TORCH, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Water", 1, XParticle.SPLASH, XMaterial.WATER_BUCKET, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Witch", 1, XParticle.WITCH, XMaterial.BREWING_STAND, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Emerald", 1, XParticle.HAPPY_VILLAGER, XMaterial.EMERALD, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Note", 2, XParticle.NOTE, XMaterial.NOTE_BLOCK, ProjectileEffectNote.class);
        new ProjectileEffectType("Love", 2, XParticle.HEART, XMaterial.PINK_WOOL, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Lava", 1, XParticle.LAVA, XMaterial.LAVA_BUCKET, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Snow", 1, XParticle.ITEM_SNOWBALL, XMaterial.SNOWBALL, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Slime", 1, XParticle.ITEM_SLIME, XMaterial.SLIME_BALL, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Crit", 1, XParticle.CRIT, XMaterial.IRON_SWORD, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("MagicCrit", 1, XParticle.ENCHANTED_HIT, XMaterial.DIAMOND_SWORD, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Explosion", 5, XParticle.EXPLOSION_EMITTER, XMaterial.TNT, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Cloud", 1, XParticle.CLOUD, XMaterial.WHITE_WOOL, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Smoke", 1, XParticle.SMOKE, XMaterial.GRAY_WOOL, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Portal", 1, XParticle.PORTAL, XMaterial.OBSIDIAN, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("DragonBreath", 1, XParticle.DRAGON_BREATH, XMaterial.DRAGON_BREATH, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("SquidInk", 1, XParticle.SQUID_INK, XMaterial.INK_SAC, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("SoulFireFlame", 1, XParticle.SOUL_FIRE_FLAME, XMaterial.SOUL_TORCH, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Snowflake", 1, XParticle.SNOWFLAKE, XMaterial.POWDER_SNOW_BUCKET, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("GlowSquid", 1, XParticle.GLOW, XMaterial.GLOW_SQUID_SPAWN_EGG, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("GlowSquidInk", 1, XParticle.GLOW_SQUID_INK, XMaterial.GLOW_INK_SAC, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("Scrape", 1, XParticle.SCRAPE, XMaterial.OXIDIZED_COPPER, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("WaxOff", 1, XParticle.WAX_OFF, XMaterial.WAXED_COPPER_BLOCK, ProjectileEffectBasicTrail.class);
        new ProjectileEffectType("WaxOn", 1, XParticle.WAX_ON, XMaterial.HONEYCOMB, ProjectileEffectBasicTrail.class);
        if (XParticle.SCULK_SOUL.isSupported()) {
            new ProjectileEffectType("SculkSoul", 1, XParticle.SCULK_SOUL, XMaterial.SCULK_CATALYST, ProjectileEffectBasicTrail.class);
        }
    }
}
