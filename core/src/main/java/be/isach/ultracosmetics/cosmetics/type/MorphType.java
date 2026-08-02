package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.morphs.*;
import be.isach.ultracosmetics.version.VersionManager;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;

public class MorphType extends CosmeticEntType<Morph> {

    private final boolean doesSkillExist;

    private MorphType(String configName, XMaterial material, XEntityType disguiseType, Class<? extends Morph> clazz,
                      boolean doesSkillExist) {
        super(Category.MORPHS, configName, material, disguiseType, clazz);
        this.doesSkillExist = doesSkillExist;
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".name", configName);
            if (doesSkillExist) {
                MessageManager.addMessage(getConfigPath() + ".skill", "Skill");
            }
        }
    }

    /**
     * Get the skill message.
     *
     * @return The skill message of the morph.
     */
    public Component getSkill() {
        return MessageManager.getMessage(getConfigPath() + ".skill");
    }

    public boolean canUseSkill() {
        return doesSkillExist && SettingsManager.getConfig().getBoolean(getConfigPath() + ".Skill-Enabled", true);
    }

    /**
     * Get the morph Disguise Type.
     *
     * @return disguise type
     */
    public EntityType getDisguiseType() {
        return getEntityType();
    }

    @Override
    public void setupConfig(CustomConfiguration config, String path) {
        super.setupConfig(config, path);
        if (doesSkillExist) {
            config.addDefault(path + ".Skill-Enabled", true, "Whether this morph's skill should be enabled.");
        }
        if (MorphNoFall.class.isAssignableFrom(getClazz())) {
            config.addDefault(path + ".No-Fall-Damage", true,
                    "Whether to disable fall damage while this morph is equipped.",
                    "(Normal use of the morph may cause fall damage otherwise.)");
        }
    }

    public static void registerMorph(String configName, XMaterial material, XEntityType disguiseType) {
        registerMorph(configName, material, disguiseType, MorphBasic.class, false);
    }

    public static void registerMorph(String configName, XMaterial material, XEntityType disguiseType,
                                     Class<? extends Morph> clazz) {
        registerMorph(configName, material, disguiseType, clazz, true);
    }

    public static void registerMorph(String configName, XMaterial material, XEntityType disguiseType,
                                     Class<? extends Morph> clazz, boolean skillEnabled) {
        if (!disguiseType.isSupported()) {
            return;
        }
        new MorphType(configName, material, disguiseType, clazz, skillEnabled);
    }

    public static void register() {
        VersionManager vm = UltraCosmeticsData.get().getVersionManager();

        registerMorph("Bat", XMaterial.COAL, XEntityType.BAT, MorphBat.class);
        registerMorph("Blaze", XMaterial.BLAZE_POWDER, XEntityType.BLAZE, MorphBlaze.class);
        registerMorph("Chicken", XMaterial.EGG, XEntityType.CHICKEN, MorphChicken.class);
        registerMorph("Pig", XMaterial.PORKCHOP, XEntityType.PIG, MorphPig.class);
        new MorphType("Enderman", XMaterial.ENDER_PEARL, XEntityType.ENDERMAN, MorphEnderman.class, true) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Mode", "Ray trace",
                        "Changes how the enderman morph teleports.",
                        "'Ray trace' (default) teleports a player to the block they're looking at,",
                        "up to 16 blocks away. Doesn't perform as well as the other options.",
                        "'Fast', teleports player 16 blocks in the direction they are looking,",
                        "while trying to avoid suffocation. Players will be able to teleport through walls.",
                        "'Enderpearl' simply launches an ender pearl in the direction the player is looking.");
            }
        };
        new MorphType("Slime", XMaterial.SLIME_BALL, XEntityType.SLIME, MorphSlime.class, true) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Jump-Speed", 2.3, "The jump speed of the slime morph ability");
            }
        };
        registerMorph("Creeper", XMaterial.GUNPOWDER, XEntityType.CREEPER, MorphCreeper.class);
        registerMorph("Snowman", XMaterial.SNOWBALL, XEntityType.SNOW_GOLEM, MorphSnowman.class);
        registerMorph("Cow", XMaterial.MILK_BUCKET, XEntityType.COW, MorphCow.class);
        registerMorph("Mooshroom", XMaterial.RED_MUSHROOM, XEntityType.MOOSHROOM, MorphMooshroom.class);
        registerMorph("Villager", XMaterial.EMERALD, XEntityType.VILLAGER, MorphVillager.class);
        registerMorph("Witch", XMaterial.POISONOUS_POTATO, XEntityType.WITCH, MorphWitch.class);
        registerMorph("Sheep", XMaterial.WHITE_WOOL, XEntityType.SHEEP, MorphSheep.class);
        registerMorph("CaveSpider", XMaterial.SPIDER_EYE, XEntityType.CAVE_SPIDER, MorphCaveSpider.class, false);
        registerMorph("Endermite", XMaterial.ENDER_EYE, XEntityType.ENDERMITE, MorphEndermite.class, false);
        registerMorph("Guardian", XMaterial.PRISMARINE_SHARD, XEntityType.GUARDIAN, MorphGuardian.class, false);
        registerMorph("Wolf", XMaterial.BONE, XEntityType.WOLF);
        registerMorph("IronGolem", XMaterial.IRON_INGOT, XEntityType.IRON_GOLEM);
        registerMorph("Wither", XMaterial.WITHER_SKELETON_SKULL, XEntityType.WITHER);
        registerMorph("Skeleton", XMaterial.BONE, XEntityType.SKELETON);
        registerMorph("Zombie", XMaterial.ROTTEN_FLESH, XEntityType.ZOMBIE);
        registerMorph("Squid", XMaterial.INK_SAC, XEntityType.SQUID);
        registerMorph("Spider", XMaterial.STRING, XEntityType.SPIDER);
        registerMorph("Ghast", XMaterial.GHAST_TEAR, XEntityType.GHAST);
        registerMorph("MagmaCube", XMaterial.MAGMA_CREAM, XEntityType.MAGMA_CUBE);
        registerMorph("Horse", XMaterial.SADDLE, XEntityType.HORSE);

        registerMorph("Sniffer", XMaterial.SNIFFER_EGG, XEntityType.SNIFFER, MorphSniffer.class, false);
        registerMorph("Allay", XMaterial.ALLAY_SPAWN_EGG, XEntityType.ALLAY, MorphAllay.class);
        registerMorph("Frog", XMaterial.FROG_SPAWN_EGG, XEntityType.FROG, MorphFrog.class, false);
        registerMorph("Warden", XMaterial.SCULK_SHRIEKER, XEntityType.WARDEN);
        registerMorph("Tadpole", XMaterial.TADPOLE_BUCKET, XEntityType.TADPOLE);
        registerMorph("Goat", XMaterial.GOAT_HORN.or(XMaterial.WHEAT), XEntityType.GOAT, MorphGoat.class, false);
        registerMorph("Axolotl", XMaterial.AXOLOTL_BUCKET, XEntityType.AXOLOTL, MorphAxolotl.class);
        registerMorph("GlowSquid", XMaterial.GLOW_INK_SAC, XEntityType.GLOW_SQUID, MorphGlowSquid.class, false);
        registerMorph("Piglin", XMaterial.GOLD_INGOT, XEntityType.PIGLIN);
        registerMorph("Strider", XMaterial.WARPED_FUNGUS_ON_A_STICK, XEntityType.STRIDER);
        registerMorph("Hoglin", XMaterial.COOKED_PORKCHOP, XEntityType.HOGLIN);
        registerMorph("PiglinBrute", XMaterial.GOLDEN_AXE, XEntityType.PIGLIN_BRUTE);
        registerMorph("Zoglin", XMaterial.ZOGLIN_SPAWN_EGG, XEntityType.ZOGLIN);
        registerMorph("ZombifiedPiglin", XMaterial.GOLDEN_SWORD, XEntityType.ZOMBIFIED_PIGLIN);
        registerMorph("Bee", XMaterial.HONEYCOMB, XEntityType.BEE, MorphBee.class);
        registerMorph("Cat", XMaterial.TROPICAL_FISH, XEntityType.CAT, MorphCat.class);
        registerMorph("Fox", XMaterial.SWEET_BERRIES, XEntityType.FOX, MorphFox.class, false);
        registerMorph("Panda", XMaterial.BAMBOO, XEntityType.PANDA);
        registerMorph("Ocelot", XMaterial.COD, XEntityType.OCELOT);
        registerMorph("WanderingTrader", XMaterial.WANDERING_TRADER_SPAWN_EGG, XEntityType.WANDERING_TRADER);
        registerMorph("Pillager", XMaterial.CROSSBOW, XEntityType.PILLAGER);
        registerMorph("Ravager", XMaterial.RAVAGER_SPAWN_EGG, XEntityType.RAVAGER);
        registerMorph("Cod", XMaterial.COD_BUCKET, XEntityType.COD, MorphCod.class, false);
        registerMorph("Dolphin", XMaterial.DOLPHIN_SPAWN_EGG, XEntityType.DOLPHIN, MorphDolphin.class, false);
        registerMorph("Drowned", XMaterial.TRIDENT, XEntityType.DROWNED, MorphDrowned.class, false);
        registerMorph("Pufferfish", XMaterial.PUFFERFISH, XEntityType.PUFFERFISH);
        registerMorph("Salmon", XMaterial.SALMON_BUCKET, XEntityType.SALMON);
        registerMorph("TropicalFish", XMaterial.TROPICAL_FISH_BUCKET, XEntityType.TROPICAL_FISH);
        registerMorph("Turtle", XMaterial.TURTLE_HELMET, XEntityType.TURTLE);
        registerMorph("Phantom", XMaterial.PHANTOM_MEMBRANE, XEntityType.PHANTOM);
        registerMorph("Parrot", XMaterial.COOKIE, XEntityType.PARROT, MorphParrot.class);
        registerMorph("Illusioner", XMaterial.COMMAND_BLOCK, XEntityType.ILLUSIONER);
        registerMorph("Llama", XMaterial.RED_WOOL, XEntityType.LLAMA, MorphLlama.class);
        registerMorph("Evoker", XMaterial.TOTEM_OF_UNDYING, XEntityType.EVOKER, MorphEvoker.class, false);
        registerMorph("WitherSkeleton", XMaterial.WITHER_SKELETON_SKULL, XEntityType.WITHER_SKELETON,
                MorphWitherSkeleton.class);
        registerMorph("Donkey", XMaterial.CHEST, XEntityType.DONKEY, MorphDonkey.class, false);
        registerMorph("Vex", XMaterial.IRON_SWORD, XEntityType.VEX);
        registerMorph("Vindicator", XMaterial.IRON_AXE, XEntityType.VINDICATOR);
        registerMorph("Mule", XMaterial.SADDLE, XEntityType.MULE);
        registerMorph("SkeletonHorse", XMaterial.BONE_BLOCK, XEntityType.SKELETON_HORSE);
        registerMorph("ZombieHorse", XMaterial.ZOMBIE_HORSE_SPAWN_EGG, XEntityType.ZOMBIE_HORSE);
        if (vm.getModule().getElderGuardianClass() != null) {
            registerMorph("ElderGuardian", XMaterial.PRISMARINE_CRYSTALS, XEntityType.ELDER_GUARDIAN,
                    vm.getModule().getElderGuardianClass());
        }
        registerMorph("PolarBear", XMaterial.SNOW_BLOCK, XEntityType.POLAR_BEAR, MorphPolarBear.class, vm.isUsingNMS());
        registerMorph("Shulker", XMaterial.SHULKER_BOX, XEntityType.SHULKER);
    }
}
