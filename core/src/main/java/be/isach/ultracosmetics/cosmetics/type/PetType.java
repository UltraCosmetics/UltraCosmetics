package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.pets.*;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.SmartLogger;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Pet types.
 *
 * @author iSach
 * @since 12-20-2015
 */
public class PetType extends CosmeticEntType<Pet> {

    private static final Map<XEntityType, Class<? extends Pet>> PET_MAP = new HashMap<>();

    private final String customization;

    private PetType(String configName, XMaterial material, XEntityType entityType, Class<? extends Pet> clazz, String customization) {
        super(Category.PETS, configName, material, entityType, clazz);
        this.customization = customization;

        PET_MAP.putIfAbsent(entityType, clazz);

        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".menu-name", configName);
            MessageManager.addMessage(getConfigPath() + ".entity-displayname", "<bold><playername>'s " + configName);
        }
    }

    private PetType(String configName, XMaterial material, XEntityType entityType, Class<? extends Pet> clazz) {
        this(configName, material, entityType, clazz, null);
    }

    public Component getEntityName(Player player) {
        return MessageManager.getMessage("Pets." + getConfigName() + ".entity-displayname",
                Placeholder.unparsed("playername", player.getName())
        );
    }

    @Override
    public Component getName() {
        return MessageManager.getMessage("Pets." + getConfigName() + ".menu-name");
    }

    @Override
    public Pet equip(UltraPlayer player, UltraCosmetics ultraCosmetics) {
        Pet pet = super.equip(player, ultraCosmetics);
        if (pet != null && customization != null) {
            if (!pet.setCustomization(customization)) {
                UltraCosmeticsData.get().getPlugin().getSmartLogger().write(LogLevel.WARNING, "Invalid customization string for pet " + getConfigName());
                player.sendMessage(ChatColor.RED + "Invalid customization string, please contact an admin.");
            }
        }
        return pet;
    }

    public static void registerPet(String configName, XMaterial material, XEntityType entityType, Class<? extends Pet> clazz) {
        if (!entityType.isSupported()) return;
        new PetType(configName, material, entityType, clazz);
    }

    public static void register() {
        // Pets that don't work
        /* registerPet("Squid", XMaterial.INK_SAC, XEntityType.SQUID, Pet.class); */
        /* registerPet("Ghast", XMaterial.FIRE_CHARGE, XEntityType.GHAST, Pet.class); */
        /* registerPet("GlowSquid", XMaterial.GLOW_INK_SAC, XEntityType.GLOW_SQUID, Pet.class); */
        /* registerPet("Phantom", XMaterial.PHANTOM_MEMBRANE, XEntityType.PHANTOM, Pet.class); */

        registerPet("Armadillo", XMaterial.ARMADILLO_SCUTE, XEntityType.ARMADILLO, Pet.class);
        registerPet("Piggy", XMaterial.PORKCHOP, XEntityType.PIG, Pet.class);
        registerPet("EasterBunny", XMaterial.CARROT, XEntityType.RABBIT, PetEasterBunny.class);
        registerPet("Cow", XMaterial.MILK_BUCKET, XEntityType.COW, Pet.class);
        registerPet("Mooshroom", XMaterial.RED_MUSHROOM, XEntityType.MOOSHROOM, PetMooshroom.class);
        registerPet("Dog", XMaterial.BONE, XEntityType.WOLF, PetDog.class);
        registerPet("Chick", XMaterial.EGG, XEntityType.CHICKEN, Pet.class);
        registerPet("ChristmasElf", XMaterial.BEACON, XEntityType.VILLAGER, PetChristmasElf.class);
        registerPet("IronGolem", XMaterial.IRON_INGOT, XEntityType.IRON_GOLEM, PetIronGolem.class);
        registerPet("Snowman", XMaterial.SNOWBALL, XEntityType.SNOW_GOLEM, PetSnowman.class);
        registerPet("Villager", XMaterial.EMERALD, XEntityType.VILLAGER, PetVillager.class);
        registerPet("Bat", XMaterial.COAL, XEntityType.BAT, Pet.class);
        registerPet("Sheep", XMaterial.WHITE_WOOL, XEntityType.SHEEP, PetSheep.class);
        new PetType("Wither", XMaterial.WITHER_SKELETON_SKULL, XEntityType.WITHER, PetWither.class) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Bossbar", "in range",
                        "Sets who the bossbar is visible for. (Has no effect on 1.8)",
                        "'in range': vanilla behavior, visible to all players in range.",
                        "'owner': only visible to pet owner",
                        "'none': not visible to any players");
            }
        };
        registerPet("Slime", XMaterial.SLIME_BALL, XEntityType.SLIME, PetSlime.class);
        registerPet("Silverfish", XMaterial.GRAY_DYE, XEntityType.SILVERFISH, Pet.class);
        registerPet("Blaze", XMaterial.BLAZE_ROD, XEntityType.BLAZE, Pet.class);
        registerPet("Creeper", XMaterial.GUNPOWDER, XEntityType.CREEPER, PetCreeper.class);
        registerPet("Enderman", XMaterial.ENDER_PEARL, XEntityType.ENDERMAN, PetEnderman.class);
        registerPet("Skeleton", XMaterial.BOW, XEntityType.SKELETON, PetSkeleton.class);
        registerPet("Zombie", XMaterial.ROTTEN_FLESH, XEntityType.ZOMBIE, PetZombie.class);

        registerPet("CaveSpider", XMaterial.SPIDER_EYE, XEntityType.CAVE_SPIDER, Pet.class);
        registerPet("Spider", XMaterial.STRING, XEntityType.SPIDER, Pet.class);
        registerPet("Endermite", XMaterial.ENDER_EYE, XEntityType.ENDERMITE, Pet.class);

        registerPet("Guardian", XMaterial.PRISMARINE_SHARD, XEntityType.GUARDIAN, Pet.class);
        registerPet("MagmaCube", XMaterial.MAGMA_CREAM, XEntityType.MAGMA_CUBE, PetMagmaCube.class);
        registerPet("Witch", XMaterial.POTION, XEntityType.WITCH, Pet.class);
        registerPet("Horse", XMaterial.LEATHER_HORSE_ARMOR.or(XMaterial.LEATHER), XEntityType.HORSE, PetHorse.class);

        registerPet("Sniffer", XMaterial.TORCHFLOWER_SEEDS, XEntityType.SNIFFER, PetSniffer.class);
        registerPet("Frog", XMaterial.LILY_PAD, XEntityType.FROG, PetFrog.class);
        if (XEntityType.WARDEN.isSupported()) {
            new PetType("Warden", XMaterial.SCULK_SHRIEKER, XEntityType.WARDEN, PetWarden.class) {
                @Override
                public void setupConfig(CustomConfiguration config, String path) {
                    super.setupConfig(config, path);
                    config.addDefault(path + ".Block-Effect", true,
                            "Whether the darkness effect is blocked while this pet is equipped.",
                            "Please note that this will also block darkness from real wardens,",
                            "due to Spigot API limitations.");
                }
            };
        }
        registerPet("Allay", XMaterial.ALLAY_SPAWN_EGG, XEntityType.ALLAY, PetAllay.class);
        registerPet("Tadpole", XMaterial.TADPOLE_BUCKET, XEntityType.TADPOLE, PetTadpole.class);
        registerPet("Goat", XMaterial.GOAT_HORN.or(XMaterial.WHEAT), XEntityType.GOAT, PetGoat.class);

        new PetType("Axolotl", XMaterial.AXOLOTL_BUCKET, XEntityType.AXOLOTL, PetAxolotl.class) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Fast", false, "https://imgur.com/a/EKWwQ6w");
            }
        };
        registerPet("Piglin", XMaterial.GOLD_INGOT, XEntityType.PIGLIN, PetPiglin.class);
        registerPet("Strider", XMaterial.WARPED_FUNGUS, XEntityType.STRIDER, Pet.class);
        registerPet("Hoglin", XMaterial.COOKED_PORKCHOP, XEntityType.HOGLIN, PetHoglin.class);
        registerPet("PiglinBrute", XMaterial.GOLDEN_AXE, XEntityType.PIGLIN_BRUTE, PetPiglinBrute.class);
        registerPet("Zoglin", XMaterial.ZOGLIN_SPAWN_EGG, XEntityType.ZOGLIN, Pet.class);
        registerPet("ZombifiedPiglin", XMaterial.GOLDEN_SWORD, XEntityType.ZOMBIFIED_PIGLIN, PetZombifiedPiglin.class);
        registerPet("Bee", XMaterial.HONEYCOMB, XEntityType.BEE, Pet.class);
        registerPet("Panda", XMaterial.BAMBOO, XEntityType.PANDA, PetPanda.class);
        registerPet("Fox", XMaterial.SWEET_BERRIES, XEntityType.FOX, PetFox.class);
        registerPet("Kitty", XMaterial.TROPICAL_FISH, XEntityType.CAT, PetKitty.class);
        registerPet("Ocelot", XMaterial.COD, XEntityType.OCELOT, Pet.class);
        registerPet("WanderingTrader", XMaterial.WANDERING_TRADER_SPAWN_EGG, XEntityType.WANDERING_TRADER, PetWanderingTrader.class);
        registerPet("Pillager", XMaterial.CROSSBOW, XEntityType.PILLAGER, Pet.class);
        registerPet("Ravager", XMaterial.RAVAGER_SPAWN_EGG, XEntityType.RAVAGER, Pet.class);
        registerPet("Cod", XMaterial.COD_BUCKET, XEntityType.COD, Pet.class);
        registerPet("Pufferfish", XMaterial.PUFFERFISH, XEntityType.PUFFERFISH, PetPufferfish.class);
        registerPet("Salmon", XMaterial.SALMON_BUCKET, XEntityType.SALMON, Pet.class);
        registerPet("TropicalFish", XMaterial.TROPICAL_FISH_BUCKET, XEntityType.TROPICAL_FISH, PetTropicalFish.class);
        new PetType("Turtle", XMaterial.TURTLE_HELMET, XEntityType.TURTLE, PetTurtle.class) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Bigger-Babies", false,
                        "Increase the size of baby turtles. They are just so small!",
                        "Supported on 1.20.5 or later only.");
            }
        };
        registerPet("Dolphin", XMaterial.DOLPHIN_SPAWN_EGG, XEntityType.DOLPHIN, Pet.class);
        registerPet("Drowned", XMaterial.TRIDENT, XEntityType.DROWNED, PetDrowned.class);
        registerPet("Parrot", XMaterial.COOKIE, XEntityType.PARROT, PetParrot.class);
        registerPet("Illusioner", XMaterial.COMMAND_BLOCK, XEntityType.ILLUSIONER, Pet.class);
        registerPet("Llama", XMaterial.RED_WOOL, XEntityType.LLAMA, PetLlama.class);
        registerPet("Vex", XMaterial.IRON_SWORD, XEntityType.VEX, PetVex.class);
        registerPet("Evoker", XMaterial.TOTEM_OF_UNDYING, XEntityType.EVOKER, Pet.class);
        registerPet("Vindicator", XMaterial.IRON_AXE, XEntityType.VINDICATOR, PetVindicator.class);
        // Some entities are here because they were split into different XEntityTypes in 1.11
        registerPet("Donkey", XMaterial.CHEST, XEntityType.DONKEY, PetDonkey.class);
        registerPet("Mule", XMaterial.SADDLE, XEntityType.MULE, PetMule.class);
        registerPet("SkeletonHorse", XMaterial.BONE_BLOCK, XEntityType.SKELETON_HORSE, Pet.class);
        registerPet("ZombieHorse", XMaterial.ZOMBIE_HORSE_SPAWN_EGG, XEntityType.ZOMBIE_HORSE, Pet.class);
        new PetType("ElderGuardian", XMaterial.PRISMARINE_CRYSTALS, XEntityType.ELDER_GUARDIAN, PetElderGuardian.class) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Block-Effect", true,
                        "Whether the mining fatigue effect is blocked while this pet is equipped.",
                        "Please note that this will also block mining fatigue from real elder guardians,",
                        "due to Spigot API limitations.");
            }
        };
        registerPet("WitherSkeleton", XMaterial.STONE_SWORD, XEntityType.WITHER_SKELETON, PetWitherSkeleton.class);
        registerPet("ZombieVillager", XMaterial.GOLDEN_APPLE, XEntityType.ZOMBIE_VILLAGER, PetZombieVillager.class);
        registerPet("Husk", XMaterial.SAND, XEntityType.HUSK, Pet.class);
        registerPet("Stray", XMaterial.ARROW, XEntityType.STRAY, PetStray.class);
        registerPet("PolarBear", XMaterial.SNOW_BLOCK, XEntityType.POLAR_BEAR, Pet.class);
        registerPet("Shulker", XMaterial.SHULKER_BOX, XEntityType.SHULKER, PetShulker.class);

        if (UltraCosmeticsData.get().getVersionManager().isUsingNMS()) {
            registerPet("Pumpling", XMaterial.PUMPKIN, XEntityType.ZOMBIE, UltraCosmeticsData.get().getVersionManager().getModule().getPumplingClass());
        }

        ConfigurationSection pets = getCustomConfig(Category.PETS);
        if (pets == null) return;

        Optional<XMaterial> mat;
        SmartLogger log = UltraCosmeticsData.get().getPlugin().getSmartLogger();
        for (String key : pets.getKeys(false)) {
            ConfigurationSection pet = pets.getConfigurationSection(key);
            if (!pet.isString("type") || !pet.isString("item") || !pet.isString("customization")) {
                log.write(LogLevel.WARNING, "Incomplete custom pet '" + key + "'");
                continue;
            }
            Optional<XEntityType> optionalType = XEntityType.of(pet.getString("type").toUpperCase(Locale.ROOT));
            if (optionalType.isEmpty()) {
                log.write(LogLevel.WARNING, "Invalid entity type for custom pet '" + key + "'");
                continue;
            }
            XEntityType type = optionalType.get();
            if (!PET_MAP.containsKey(type)) {
                log.write(LogLevel.WARNING, "Entity type '" + type + "' for pet '" + key + "' does not exist as a pet.");
                continue;
            }
            mat = XMaterial.matchXMaterial(pet.getString("item"));
            if (mat.isEmpty() || !mat.get().get().isItem()) {
                log.write(LogLevel.WARNING, "Invalid item for custom pet '" + key + "'");
                continue;
            }
            MessageManager.addMessage(Category.PETS.getConfigPath() + "." + key + ".menu-name", key);
            MessageManager.addMessage(Category.PETS.getConfigPath() + "." + key + ".entity-displayname", "<bold><playername>'s " + key);
            MessageManager.addMessage(Category.PETS.getConfigPath() + "." + key + ".Description", "A custom pet!");
            new PetType(key, mat.get(), type, PET_MAP.get(type), pet.getString("customization"));
        }
    }
}
