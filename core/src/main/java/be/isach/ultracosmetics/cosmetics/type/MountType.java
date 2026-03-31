package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.mounts.*;
import be.isach.ultracosmetics.version.VersionManager;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A cosmetic type.
 *
 * @author iSach
 * @since 12-18-2015
 */
public class MountType extends CosmeticEntType<Mount> {

    private final int repeatDelay;
    private final List<XMaterial> defaultBlocks;
    private final double defaultSpeed;
    private final double movementSpeed;

    private MountType(String configName, XMaterial material, XEntityType entityType, int repeatDelay, double defaultSpeed, Class<? extends Mount> mountClass, List<XMaterial> defaultBlocks) {
        super(Category.MOUNTS, configName, material, entityType, mountClass);
        this.repeatDelay = repeatDelay;
        this.defaultBlocks = defaultBlocks;
        this.defaultSpeed = defaultSpeed;
        this.movementSpeed = SettingsManager.getConfig().getDouble("Mounts." + configName + ".Speed", defaultSpeed);
        setupConfigLate(SettingsManager.getConfig(), getConfigPath());
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".menu-name", configName);
            MessageManager.addMessage(getConfigPath() + ".entity-displayname", "<bold><playername>'s " + configName);
        }
    }

    private MountType(String configName, XMaterial material, XEntityType entityType, int repeatDelay, double defaultSpeed, Class<? extends Mount> mountClass) {
        this(configName, material, entityType, repeatDelay, defaultSpeed, mountClass, null);
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public double getDefaultMovementSpeed() {
        return defaultSpeed;
    }

    @Override
    public Component getName() {
        return MessageManager.getMessage("Mounts." + getConfigName() + ".menu-name");
    }

    public Component getName(Player player) {
        return MessageManager.getMessage("Mounts." + getConfigName() + ".entity-displayname",
                Placeholder.unparsed("playername", player.getName())
        );
    }

    public int getRepeatDelay() {
        return repeatDelay;
    }

    public List<XMaterial> getDefaultBlocks() {
        return defaultBlocks;
    }

    public boolean doesPlaceBlocks() {
        return defaultBlocks != null;
    }

    public void setupConfigLate(CustomConfiguration config, String path) {
        if (LivingEntity.class.isAssignableFrom(getEntityType().getEntityClass())) {
            config.addDefault(path + ".Speed", getDefaultMovementSpeed(), "The movement speed of the mount, see:", "https://minecraft.fandom.com/wiki/Attribute#Attributes_available_on_all_living_entities");
        }
        if (doesPlaceBlocks()) {
            // Don't use Stream#toList(), it doesn't exist in Java 8
            config.addDefault(path + ".Blocks-To-Place", getDefaultBlocks().stream().map(Enum::name).collect(Collectors.toList()), "Blocks to choose from as this mount walks.");
        }
    }

    public static void register() {
        VersionManager vm = UltraCosmeticsData.get().getVersionManager();
        new MountType("DruggedHorse", XMaterial.SUGAR, XEntityType.HORSE, 2, 1.1, MountDruggedHorse.class);
        new MountType("GlacialSteed", XMaterial.PACKED_ICE, XEntityType.HORSE, 2, 0.4, MountGlacialSteed.class, Collections.singletonList(XMaterial.SNOW_BLOCK));
        new MountType("MountOfFire", XMaterial.BLAZE_POWDER, XEntityType.HORSE, 2, 0.4, MountOfFire.class, Arrays.asList(XMaterial.ORANGE_TERRACOTTA, XMaterial.YELLOW_TERRACOTTA, XMaterial.RED_TERRACOTTA));
        new MountType("Snake", XMaterial.WHEAT_SEEDS, XEntityType.SHEEP, 2, 0.3, MountSnake.class);
        new MountType("MoltenSnake", XMaterial.MAGMA_CREAM, XEntityType.MAGMA_CUBE, 1, 0.4, MountMoltenSnake.class);
        new MountType("SlimeSnake", XMaterial.SLIME_BLOCK, XEntityType.SLIME, 1, 0.4, MountSlimeSnake.class);
        new MountType("MountOfWater", XMaterial.LIGHT_BLUE_DYE, XEntityType.HORSE, 2, 0.4, MountOfWater.class, Arrays.asList(XMaterial.LIGHT_BLUE_TERRACOTTA, XMaterial.CYAN_TERRACOTTA, XMaterial.BLUE_TERRACOTTA));
        new MountType("EcologistHorse", XMaterial.GREEN_DYE, XEntityType.HORSE, 2, 0.4, MountEcologistHorse.class, Arrays.asList(XMaterial.LIME_TERRACOTTA, XMaterial.GREEN_TERRACOTTA));
        new MountType("Rudolph", XMaterial.DEAD_BUSH, XEntityType.MULE, 1, 0.4, MountRudolph.class);
        new MountType("WalkingDead", XMaterial.ROTTEN_FLESH, XEntityType.ZOMBIE_HORSE, 2, 0.4, MountWalkingDead.class);
        new MountType("InfernalHorror", XMaterial.BONE, XEntityType.SKELETON_HORSE, 2, 0.4, MountInfernalHorror.class);
        new MountType("Horse", XMaterial.SADDLE, XEntityType.HORSE, 0, 0.3, MountHorse.class);
        new MountType("Donkey", XMaterial.CHEST, XEntityType.DONKEY, 0, 0.25, MountDonkey.class);
        new MountType("Mule", XMaterial.ENDER_CHEST, XEntityType.MULE, 0, 0.25, MountMule.class);
        new MountType("Pig", XMaterial.PORKCHOP, XEntityType.PIG, 0, 0.35, MountPig.class);

        if (UltraCosmeticsData.get().isMobChipAvailable()) {
            new MountType("NyanSheep", XMaterial.CYAN_DYE, XEntityType.SHEEP, 1, 0.4, MountNyanSheep.class);
            new MountType("Dragon", XMaterial.DRAGON_EGG, XEntityType.ENDER_DRAGON, 1, 0.7, MountDragon.class) {
                @Override
                public void setupConfig(CustomConfiguration config, String path) {
                    super.setupConfig(config, path);
                    config.addDefault("Mounts.Dragon.Stationary", false, "If true, the dragon will not move.");
                }
            };
        }

        new MountType("Strider", XMaterial.WARPED_FUNGUS_ON_A_STICK, XEntityType.STRIDER, 0, 0.35, MountStrider.class);

        if (XEntityType.CAMEL.isSupported()) {
            new MountType("Camel", XMaterial.CACTUS, XEntityType.CAMEL, 0, 0.35, MountCamel.class);
        }

        if (vm.isUsingNMS()) {
            new MountType("Slime", XMaterial.SLIME_BALL, XEntityType.SLIME, 2, 0.8, vm.getModule().getSlimeClass());
            new MountType("Spider", XMaterial.COBWEB, XEntityType.SPIDER, 2, 0.4, vm.getModule().getSpiderClass());
            new MountType("HypeCart", XMaterial.MINECART, XEntityType.MINECART, 1, 0, MountHypeCart.class);
        }
    }
}
