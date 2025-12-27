package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.particleeffects.*;
import be.isach.ultracosmetics.version.ServerVersion;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;

/**
 * Particle effect types.
 *
 * @author iSach
 * @since 12-18-2015
 */
public class ParticleEffectType extends CosmeticParticleType<ParticleEffect> {

    private ParticleEffectType(String configName, int repeatDelay, XParticle effect, XMaterial material, Class<? extends ParticleEffect> clazz, boolean supportsParticleMultiplier) {
        super(Category.EFFECTS, configName, repeatDelay, effect, material, clazz, supportsParticleMultiplier);
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".name", configName);
        }
    }

    public static void register(ServerVersion version) {
        new ParticleEffectType("SnowCloud", 1, XParticle.ITEM_SNOWBALL, XMaterial.SNOWBALL, ParticleEffectCloud.class, true);
        new ParticleEffectType("BloodHelix", 1, XParticle.DUST, XMaterial.REDSTONE, ParticleEffectBloodHelix.class, true);
        new ParticleEffectType("FrostLord", 1, XParticle.ITEM_SNOWBALL, XMaterial.PACKED_ICE, ParticleEffectFrostLord.class, true);
        new ParticleEffectType("FlameRings", 1, XParticle.FLAME, XMaterial.BLAZE_POWDER, ParticleEffectFlameRings.class, false);
        new ParticleEffectType("GreenSparks", 1, XParticle.HAPPY_VILLAGER, XMaterial.EMERALD, ParticleEffectGreenSparks.class, false);
        new ParticleEffectType("FrozenWalk", 1, XParticle.ITEM_SNOWBALL, XMaterial.SNOWBALL, ParticleEffectFrozenWalk.class, false);
        new ParticleEffectType("Music", 4, XParticle.NOTE, XMaterial.MUSIC_DISC_MALL, ParticleEffectMusic.class, true);
        new ParticleEffectType("Enchanted", 1, XParticle.ENCHANT, XMaterial.BOOK, ParticleEffectEnchanted.class, true);
        new ParticleEffectType("Inferno", 1, XParticle.FLAME, XMaterial.NETHER_WART, ParticleEffectInferno.class, true);
        new ParticleEffectType("AngelWings", 2, XParticle.DUST, XMaterial.FEATHER, ParticleEffectAngelWings.class, true);
        new ParticleEffectType("RainbowWings", 2, XParticle.DUST, XMaterial.YELLOW_DYE, ParticleEffectRainbowWings.class, true);
        new ParticleEffectType("SuperHero", 2, XParticle.DUST, XMaterial.GLOWSTONE_DUST, ParticleEffectSuperHero.class, true);
        new ParticleEffectType("SantaHat", 2, XParticle.DUST, XMaterial.BEACON, ParticleEffectSantaHat.class, true);
        new ParticleEffectType("EnderAura", 1, XParticle.PORTAL, XMaterial.ENDER_EYE, ParticleEffectEnderAura.class, true);
        new ParticleEffectType("FlameFairy", 1, XParticle.FLAME, XMaterial.BLAZE_POWDER, ParticleEffectFlameFairy.class, false);
        new ParticleEffectType("RainCloud", 1, XParticle.DRIPPING_WATER, XMaterial.LAPIS_LAZULI, ParticleEffectCloud.class, true);
        new ParticleEffectType("CrushedCandyCane", 1, XParticle.ITEM, XMaterial.WHITE_DYE, ParticleEffectCrushedCandyCane.class, true);
        new ParticleEffectType("InLove", 1, XParticle.HEART, XMaterial.RED_DYE, ParticleEffectInLove.class, true);
        new ParticleEffectType("MagicalRods", 3, XParticle.DUST, XMaterial.BLAZE_ROD, ParticleEffectMagicalRods.class, true);
        new ParticleEffectType("FireWaves", 4, XParticle.FLAME, XMaterial.GOLD_NUGGET, ParticleEffectFireWaves.class, true);
        new ParticleEffectType("CursedHalo", 2, XParticle.WITCH, XMaterial.PURPLE_DYE, ParticleEffectCursedHalo.class, false);
        new ParticleEffectType("VolcanicHalo", 2, XParticle.FLAME, XMaterial.MAGMA_CREAM, ParticleEffectHalo.class, false);
        new ParticleEffectType("CursedFootprints", 1, XParticle.WITCH, XMaterial.JACK_O_LANTERN, ParticleEffectFootprints.class, false);
        new ParticleEffectType("SpringFootprints", 1, XParticle.HAPPY_VILLAGER, XMaterial.POPPY, ParticleEffectFootprints.class, false);
        new ParticleEffectType("ShadowFootprints", 2, XParticle.LARGE_SMOKE, XMaterial.BLACK_WOOL, ParticleEffectFootprints.class, false);
        new ParticleEffectType("Notes", 6, XParticle.NOTE, XMaterial.NOTE_BLOCK, ParticleEffectAboveHead.class, false);
        new ParticleEffectType("Hearts", 6, XParticle.HEART, XMaterial.RED_WOOL, ParticleEffectAboveHead.class, false);
        new ParticleEffectType("EnderFootprints", 1, XParticle.DRAGON_BREATH, XMaterial.DRAGON_BREATH, ParticleEffectFootprints.class, false);
        new ParticleEffectType("ArcaneFlame", 3, XParticle.SOUL_FIRE_FLAME, XMaterial.SOUL_TORCH, ParticleEffectAboveHead.class, false);
        new ParticleEffectType("DivineHalo", 2, XParticle.WAX_OFF, XMaterial.GLOWSTONE, ParticleEffectHalo.class, false);
        new ParticleEffectType("SnowFootprints", 1, XParticle.SNOWFLAKE, XMaterial.POWDER_SNOW_BUCKET, ParticleEffectFootprints.class, false);
    }
}
