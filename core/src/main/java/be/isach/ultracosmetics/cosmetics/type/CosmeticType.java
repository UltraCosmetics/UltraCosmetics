package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import be.isach.ultracosmetics.version.ServerVersion;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A cosmetic type.
 *
 * @author iSach
 * @since 07-05-2016
 */
public abstract class CosmeticType<T extends Cosmetic<?>> {
    // For use when adding new cosmetics
    public static final boolean GENERATE_MISSING_MESSAGES = false;
    private static final Permission ALL_PERMISSION = new Permission("ultracosmetics.allcosmetics");
    private static final Map<String, Permission> registeredPermissions = new HashMap<>();
    private static final Map<Category, List<CosmeticType<?>>> VALUES = new HashMap<>();
    private static final Map<Category, List<CosmeticType<?>>> ENABLED = new HashMap<>();
    private static final YamlConfiguration customConfig = new YamlConfiguration();
    protected static boolean rewriteCustomConfig = false;

    static {
        try {
            Bukkit.getPluginManager().addPermission(ALL_PERMISSION);
        } catch (IllegalArgumentException e) {
            // Happens when permission is already registered, i.e. UltraCosmetics is being reloaded externally...
        }
    }

    public static void loadCustomCosmetics() {
        try {
            File configFile = new File(UltraCosmeticsData.get().getPlugin().getDataFolder(), "custom_cosmetics.yml");
            if (!configFile.exists()) {
                UltraCosmeticsData.get().getPlugin().saveResource("custom_cosmetics.yml", false);
            }
            customConfig.load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            UltraCosmeticsData.get().getPlugin().getSmartLogger().write(LogLevel.ERROR, "Failed to load custom cosmetics, they will be ignored.");
            e.printStackTrace();
        }
    }

    protected static ConfigurationSection getCustomConfig(Category cat) {
        if (cat.isSuits()) return customConfig.getConfigurationSection("Suits");
        return customConfig.getConfigurationSection(cat.getConfigPath());
    }

    private static void saveCustomCosmetics() {
        try {
            File configFile = new File(UltraCosmeticsData.get().getPlugin().getDataFolder(), "custom_cosmetics.yml");
            customConfig.save(configFile);
        } catch (IOException e) {
            UltraCosmeticsData.get().getPlugin().getSmartLogger().write(LogLevel.ERROR, "Failed to load custom cosmetics, they will be ignored.");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <V extends CosmeticType<?>> V valueOf(Category cat, String name) {
        for (CosmeticType<?> type : VALUES.get(cat)) {
            if (type.getConfigName().equalsIgnoreCase(name)) {
                return (V) type;
            }
        }
        return null;
    }

    public static List<CosmeticType<?>> valuesOf(Category cat) {
        return VALUES.getOrDefault(cat, new ArrayList<>());
    }

    public static List<CosmeticType<?>> enabledOf(Category cat) {
        return ENABLED.getOrDefault(cat, new ArrayList<>());
    }

    public static void removeAllTypes() {
        VALUES.clear();
        ENABLED.clear();
        SuitCategory.values().clear();
    }

    public static void registerAll() {
        ServerVersion version = UltraCosmeticsData.get().getServerVersion();
        GadgetType.register(version);
        MountType.register(version);
        ParticleEffectType.register(version);
        PetType.register();
        HatType.register();
        SuitCategory.register();
        for (SuitCategory sc : SuitCategory.values()) {
            sc.initializeSuitParts();
        }
        MorphType.register();
        EmoteType.register();
        ProjectileEffectType.register(version);
        DeathEffectType.register();
        if (rewriteCustomConfig) {
            saveCustomCosmetics();
        }

        // Permissions registered by cosmetics are not fully calculated until here,
        // reducing loading time.
        ALL_PERMISSION.recalculatePermissibles();

        // Always save in case we need to add new custom cosmetics to the messages file
        MessageManager.save();
        VALUES.forEach((c, l) -> l.forEach(t -> t.setupConfig(SettingsManager.getConfig(), t.getConfigPath())));
        // Reprocess enabled cosmetics now that each cosmetic has added its config options
        updateEnabled();
    }

    public static void updateEnabled() {
        ENABLED.clear();
        VALUES.forEach((cat, types) -> types.forEach(type -> {
            if (type.isEnabled()) {
                ENABLED.computeIfAbsent(cat, l -> new ArrayList<>()).add(type);
            }
        }));
    }

    private final String configName;
    private final List<String> description;
    private final Class<? extends T> clazz;
    private final Category category;
    private final XMaterial material;
    private Permission permission;
    private Permission purchasePermission;

    public CosmeticType(Category category, String configName, XMaterial material, Class<? extends T> clazz) {
        this(category, configName, material, clazz, true);
    }

    public CosmeticType(Category category, String configName, XMaterial material, Class<? extends T> clazz, boolean registerPerm) {
        this.category = category;
        this.configName = configName;
        this.material = material;
        this.clazz = clazz;

        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".Description", "Description");
        }
        Component colors = MessageManager.getMiniMessage().deserialize(SettingsManager.getConfig().getString("Description-Style", ""));
        this.description = MessageManager.getLore(getCategory().getConfigPath() + "." + configName + ".Description", colors.style());
        if (registerPerm) {
            registerPermission();
        }
        VALUES.computeIfAbsent(category, l -> new ArrayList<>()).add(this);
        if (isEnabled()) {
            ENABLED.computeIfAbsent(category, l -> new ArrayList<>()).add(this);
        }
    }

    public T equip(UltraPlayer player, UltraCosmetics ultraCosmetics) {
        T cosmetic = null;
        try {
            Class<?> selfClass = getClass();
            // Cosmetic types that require extra configuration are anonymous classes
            if (selfClass.isAnonymousClass()) {
                selfClass = selfClass.getSuperclass();
            }
            cosmetic = getClazz().getDeclaredConstructor(UltraPlayer.class, selfClass, UltraCosmetics.class).newInstance(player, this, ultraCosmetics);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
        cosmetic.equip();
        return cosmetic;
    }

    public boolean isEnabled() {
        return SettingsManager.getConfig().getBoolean(category.getConfigPath() + "." + configName + ".Enabled");
    }

    public Component getName() {
        return MessageManager.getMessage(category.getConfigPath() + "." + configName + ".name");
    }

    public String getConfigName() {
        return configName;
    }

    public Permission getPermission() {
        return permission;
    }

    public Permission getPurchasePermission() {
        return purchasePermission;
    }

    public Class<? extends T> getClazz() {
        return clazz;
    }

    public Category getCategory() {
        return category;
    }

    public XMaterial getMaterial() {
        return material;
    }

    public ItemStack getItemStack() {
        String skull = SettingsManager.getConfig().getString(category.getConfigPath() + "." + getConfigName() + ".Custom-Head");
        if (skull != null) {
            return ItemFactory.createSkull(skull, "");
        }
        return material.parseItem();
    }

    public String getConfigPath() {
        return getCategory().getConfigPath() + "." + getConfigName();
    }

    /**
     * Transforms the description from a String to a list.
     * Without colors.
     *
     * @return The description as a list.
     */
    public List<String> getDescription() {
        return description;
    }

    /**
     * Check if the cosmetic should show a description.
     *
     * @return {@code true} if it should show a description, otherwise {@code false}.
     */
    public boolean showsDescription() {
        return SettingsManager.getConfig().getBoolean(category.getConfigPath() + "." + getConfigName() + ".Show-Description");
    }

    /**
     * Check if the cosmetic can be found in Treasure Chests.
     *
     * @return {@code true} if it can be found in treasure chests, otherwise {@code false}.
     */
    public boolean canBeFound() {
        return getChestWeight() > 0;
    }

    /**
     * Gets the weight of getting this cosmetic in its category.
     * The absolute chance of getting this cosmetic is also affected by the category weight.
     *
     * @return its weight
     */
    public int getChestWeight() {
        return SettingsManager.getConfig().getInt(category.getConfigPath() + "." + getConfigName() + ".Treasure-Chest-Weight");
    }

    /**
     * Override toString method to show Cosmetic name.
     *
     * @return cosmetic name in uppercase
     */
    @Override
    public String toString() {
        return getConfigName().toUpperCase(Locale.ROOT);
    }

    protected void registerPermission() {
        permission = registeredPermissions.computeIfAbsent(category.getPermission() + "." + getPermissionSuffix(), s -> {
            Permission perm = new Permission(s);
            try {
                ALL_PERMISSION.getChildren().put(s, true);
                Bukkit.getPluginManager().addPermission(perm);
            } catch (IllegalArgumentException ignored) {
            }
            return perm;
        });
        purchasePermission = registeredPermissions.computeIfAbsent(category.getPurchasePermission() + "." + getPermissionSuffix(), s -> {
            Permission perm = new Permission(s, PermissionDefault.TRUE);
            Bukkit.getPluginManager().addPermission(perm);
            return perm;
        });
    }

    protected String getPermissionSuffix() {
        return getConfigName().toLowerCase(Locale.ROOT);
    }

    protected void setupConfig(CustomConfiguration config, String path) {
        if (PlayerAffectingCosmetic.class.isAssignableFrom(this.getClazz())) {
            config.addDefault(path + ".Affect-Players", true, "Should it affect players? (Velocity, etc.)");
        }
        config.addDefault(path + ".Enabled", true);
        config.addDefault(path + ".Show-Description", true, "Whether to show description when hovering in GUI");
        String findableKey = path + ".Can-Be-Found-In-Treasure-Chests";
        int weight = 1;
        if (config.isBoolean(findableKey)) {
            weight = config.getBoolean(findableKey) ? 1 : 0;
            config.set(findableKey, null);
        }
        config.addDefault(path + ".Treasure-Chest-Weight", weight, "The higher the weight, the better the chance of", "finding this cosmetic when this category is picked.", "Fractional values are not allowed.", "Set to 0 to disable finding in chests.");
        config.addDefault(path + ".Purchase-Price", 500, "Price to buy individually in GUI", "Only works if No-Permission.Allow-Purchase is true and this setting > 0");
    }
}
