package be.isach.ultracosmetics;

import be.isach.ultracosmetics.command.CommandManager;
import be.isach.ultracosmetics.config.AutoCommentConfiguration;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.FunctionalConfigLoader;
import be.isach.ultracosmetics.config.ManualCommentConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.economy.EconomyHandler;
import be.isach.ultracosmetics.hook.ChestSortHook;
import be.isach.ultracosmetics.hook.DiscordSRVHook;
import be.isach.ultracosmetics.hook.PlaceholderHook;
import be.isach.ultracosmetics.hook.PlayerAuctionsHook;
import be.isach.ultracosmetics.hook.TownyHook;
import be.isach.ultracosmetics.listeners.ClientBrandListener;
import be.isach.ultracosmetics.listeners.EntityDismountListener;
import be.isach.ultracosmetics.listeners.MainListener;
import be.isach.ultracosmetics.listeners.PlayerListener;
import be.isach.ultracosmetics.listeners.PriorityListener;
import be.isach.ultracosmetics.listeners.UnmovableItemListener;
import be.isach.ultracosmetics.menu.CosmeticsInventoryHolder;
import be.isach.ultracosmetics.menu.Menus;
import be.isach.ultracosmetics.menu.menus.CustomMainMenu;
import be.isach.ultracosmetics.mysql.MySqlConnectionManager;
import be.isach.ultracosmetics.paper.DummyPaperSupport;
import be.isach.ultracosmetics.paper.PaperSupport;
import be.isach.ultracosmetics.permissions.PermissionManager;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.run.FallDamageManager;
import be.isach.ultracosmetics.run.InvalidWorldChecker;
import be.isach.ultracosmetics.run.VanishChecker;
import be.isach.ultracosmetics.treasurechests.TreasureChestManager;
import be.isach.ultracosmetics.util.EntitySpawningManager;
import be.isach.ultracosmetics.util.InventoryViewHelper;
import be.isach.ultracosmetics.util.PermissionPrinter;
import be.isach.ultracosmetics.util.PlayerUtils;
import be.isach.ultracosmetics.util.Problem;
import be.isach.ultracosmetics.util.ProblemSeverity;
import be.isach.ultracosmetics.util.SmartLogger;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import be.isach.ultracosmetics.util.UpdateManager;
import be.isach.ultracosmetics.version.ServerVersion;
import be.isach.ultracosmetics.worldguard.WorldGuardManager;
import com.cryptomorin.xseries.XMaterial;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.libraryaddict.disguise.DisguiseConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Main class of the plugin.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class UltraCosmetics extends JavaPlugin {
    /**
     * Instance of FoliaLib which allows plugin to work on Folia or Spigot/Paper.
     */
    private final FoliaLib foliaLib;

    /**
     * Manages sub commands.
     */
    private CommandManager commandManager;

    /**
     * The Configuration. (config.yml)
     */
    private CustomConfiguration config;

    /**
     * Config File.
     */
    private File file;

    /**
     * Player Manager instance.
     */
    private UltraPlayerManager playerManager;

    /**
     * Smart Logger Instance.
     */
    private SmartLogger smartLogger;

    /**
     * MySql Manager.
     */
    private MySqlConnectionManager mySqlConnectionManager;

    /**
     * Update Manager.
     */
    private UpdateManager updateChecker;

    /**
     * Menus.
     */
    private Menus menus;

    private EconomyHandler economyHandler;

    private PermissionManager permissionManager;
    private PlaceholderHook placeholderHook;

    private DiscordSRVHook discordHook;

    private ChestSortHook chestSortHook;

    private PlayerAuctionsHook playerAuctionsHook;

    private UnmovableItemListener unmovableItemListener;
    private TreasureChestManager treasureChestManager;
    private EntityDismountListener entityDismountListener;
    private PaperSupport paperSupport;

    /**
     * Manages WorldGuard flags.
     */
    private final WorldGuardManager worldGuardManager = new WorldGuardManager(this);

    private boolean legacyMessagePrinted = false;
    private boolean enableFinished = false;

    /**
     * Problems with the configuration of UC, severe (aka fatal) or otherwise
     */
    private Set<Problem> activeProblems = new HashSet<>();

    private Set<Problem> loadTimeProblems = new HashSet<>();

    private final List<String> supportedLanguages = new ArrayList<>();

    private final List<UCAddon> addons = new ArrayList<>();

    public UltraCosmetics() {
        foliaLib = new FoliaLib(this);
    }

    /**
     * Called when plugin is loaded. Used for registering WorldGuard flags as recommended in API documentation.
     */
    @Override
    public void onLoad() {
        // moved to onLoad so it's ready for WorldGuard support
        this.smartLogger = new SmartLogger(getLogger());

        UltraCosmeticsData.init(this);

        Problem problem = UltraCosmeticsData.get().checkServerVersion();
        if (problem != null) {
            if (problem.getSeverity() == ProblemSeverity.FATAL) {
                loadTimeProblems.add(problem);
                return;
            }
            activeProblems.add(problem);
        }

        // Use super.getConfig() because CustomConfiguration doesn't load until onEnable
        boolean worldGuardIntegration = super.getConfig().getBoolean("WorldGuard-Integration", true);
        // Not using isPluginEnabled() because WorldGuard should be
        // loaded but not yet enabled when registering flags
        if (worldGuardIntegration && getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardManager.register();
        }
        loadTimeProblems = new HashSet<>(activeProblems);
    }

    /**
     * Called when plugin is enabled.
     */
    @Override
    public void onEnable() {
        String pathPrefix = "messages/messages_";
        try {
            URL jar = getFile().toURI().toURL();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null) break;
                String path = e.getName();
                if (path.startsWith(pathPrefix)) {
                    // Start string at end of prefix, end string two characters later
                    supportedLanguages.add(path.substring(pathPrefix.length(), pathPrefix.length() + 2));
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        start(true);
        // Load the class manually so it's available on shutdown
        PlayerUtils.class.getName();
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", new ClientBrandListener(this));
    }

    public void start(boolean firstRun) {
        long startTime = System.currentTimeMillis();

        // If this is a reload, it's important to clear all enable-time problems
        activeProblems = new HashSet<>(loadTimeProblems);

        // Enable command manager as early as possible
        // so we can print helpful error messages about
        // why the plugin didn't start correctly.
        commandManager = new CommandManager(this);

        // Also register early so we can send a message about issues.
        getServer().getPluginManager().registerEvents(new PriorityListener(this), this);
        // Set up config.
        if (!setUpConfig()) {
            getSmartLogger().write(LogLevel.ERROR, "Failed to load config.yml, shutting down to protect data.");
            getSmartLogger().write(LogLevel.ERROR, "Please run config.yml through a YAML checker site.");
            activeProblems.add(Problem.BAD_CONFIG);
            return;
        }

        updateChecker = new UpdateManager(this);
        // Start update checker ASAP so if there's a problem that can be
        // resolved by updating, the user knows there's an update.
        // (We can't start it before the config loader because we need config settings.)
        if (SettingsManager.getConfig().getBoolean("Check-For-Updates")) {
            getSmartLogger().write("Checking for update...");
            updateChecker.schedule();
        }

        // if early loading failed...
        if (UltraCosmeticsData.get().getServerVersion() == null) {
            getSmartLogger().write(LogLevel.ERROR, "Plugin load has failed, please check earlier in the log for details.");
            return;
        }

        UltraCosmeticsData.get().initConfigFields();

        String langFileName = "messages_" + UltraCosmeticsData.get().getLanguage() + ".yml";
        File langFile = new File(getDataFolder(), langFileName);
        if (!langFile.exists()) {
            try {
                // Copied from JavaPlugin#saveResource
                InputStream in = getResource("messages/" + langFileName);
                OutputStream out = Files.newOutputStream(langFile.toPath());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } catch (IOException e) {
                getSmartLogger().write(LogLevel.ERROR, "Failed to copy " + langFileName);
            }
        }

        // Create UltraPlayer Manager.
        this.playerManager = new UltraPlayerManager(this);

        // Beginning of boot log. basic informations.
        getSmartLogger().write("-------------------------------------------------------------------");
        getSmartLogger().write("Thanks for using UltraCosmetics! Version: " + updateChecker.getCurrentVersion().versionClassifierCommit());
        getSmartLogger().write("Plugin by Datatags. Original Author: iSach");
        getSmartLogger().write("Link: https://bit.ly/UltraCosmetics");

        if (activeProblems.contains(Problem.BAD_MC_VERSION)) {
            getSmartLogger().write();
            getSmartLogger().write(LogLevel.WARNING, "This server version is unknown (" + ServerVersion.getMinecraftVersion() + "), but UltraCosmetics will try to continue running.");
        }

        // Initialize NMS Module
        if (!UltraCosmeticsData.get().initModule()) {
            // This method sets its own problems
            return;
        }

        // Init Message manager.
        if (!MessageManager.load()) {
            getSmartLogger().write(LogLevel.ERROR, "Failed to load messages.yml, shutting down to protect data.");
            getSmartLogger().write(LogLevel.ERROR, "Please run messages.yml through a YAML checker site.");
            activeProblems.add(Problem.BAD_MESSAGES);
            return;
        }
        migrateConfigToMiniMessage();

        treasureChestManager = new TreasureChestManager(this);

        // Register Listeners.
        registerListeners();

        if (!loadPaperSupport()) {
            paperSupport = new DummyPaperSupport();
        }

        // Set up Cosmetics config.
        CosmeticType.loadCustomCosmetics();
        CosmeticType.registerAll();

        // Can't use Category.MORPHS.isEnabled() here because it checks whether LibsDisguises is enabled on its own
        if (SettingsManager.getConfig().getBoolean("Categories-Enabled." + Category.MORPHS.getConfigPath())) {
            if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
                getSmartLogger().write();
                getSmartLogger().write(LogLevel.WARNING, "Morphs require Lib's Disguises, but it is not installed. Morphs will be disabled.");
                // TODO: make this a Problem?
            } else {
                try {
                    // Option is not present on older versions of LibsDisguises, added in commit af492c2
                    if (!DisguiseConfig.isTallSelfDisguises()) {
                        getSmartLogger().write();
                        getSmartLogger().write(LogLevel.WARNING, "You have TallSelfDisguises disabled in LibsDisguises's players.yml. Self view of morphs may not work as expected.");
                        activeProblems.add(Problem.TALL_DISGUISES_DISABLED);
                    }
                } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
                }
            }
        }

        if (!UltraCosmeticsData.get().isMobChipAvailable()) {
            getSmartLogger().write();
            getSmartLogger().write("MobChip does not support this version of Minecraft, pets will be disabled.");
            activeProblems.add(Problem.MOBCHIP_ERROR);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getSmartLogger().write();
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
            getSmartLogger().write("Hooked into PlaceholderAPI");
        }

        // Set up WorldGuard if needed.
        // WorldGuard handlers persist across UC reloads, so we only need to register them once.
        if (firstRun) {
            worldGuardManager.registerPhase2();
        }

        // Set up economy if needed.
        setupEconomy();

        if (!UltraCosmeticsData.get().usingFileStorage()) {
            getSmartLogger().write();
            getSmartLogger().write("Connecting to MySQL database...");

            // Start MySQL. May forcefully switch to file storage if it fails to connect.
            mySqlConnectionManager = new MySqlConnectionManager(this);
            // If initialization was successful, create tables and such.
            if (mySqlConnectionManager.success()) {
                mySqlConnectionManager.start();
            }
            // Separate check because both constructor and MySqlConnectionManager#start
            // can modify the success tracker.
            if (mySqlConnectionManager.success()) {
                getSmartLogger().write("Connected to MySQL database.");
            } else {
                getSmartLogger().write("File storage will be used instead.");
                activeProblems.add(Problem.SQL_INIT_FAILURE);
            }
        }

        permissionManager = new PermissionManager(this);

        playerManager.initPlayers();

        // Start the Fall Damage and Invalid World Check Runnables.
        new FallDamageManager().schedule();
        // No need to worry about the invalid world checker if all worlds are allowed
        if (!config.getStringList("Enabled-Worlds").contains("*")) {
            new InvalidWorldChecker(this).schedule();
        }
        if (config.getBoolean("Prevent-Cosmetics-In-Vanish")) {
            new VanishChecker(this).schedule();
        }

        if (getServer().getPluginManager().isPluginEnabled("DiscordSRV")
                && !SettingsManager.getConfig().getString("DiscordSRV-Loot-Channel", "0").equals("0")) {
            discordHook = new DiscordSRVHook();
            getSmartLogger().write();
            getSmartLogger().write("Hooked into DiscordSRV");
        }

        chestSortHook = hookIfEnabled("ChestSort", () -> new ChestSortHook(this));
        hookIfEnabled("Towny", TownyHook::new);
        playerAuctionsHook = hookIfEnabled("PlayerAuctions", () -> new PlayerAuctionsHook(this), 1.24f);

        // Start up bStats
        setupMetrics();

        this.menus = new Menus(this);
        setupCustomMainMenu();

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PermissionPrinter.printPermissions(this);

        // Ended well :v
        getSmartLogger().write();
        getSmartLogger().write("UltraCosmetics successfully finished loading in " + (System.currentTimeMillis() - startTime) + "ms!");
        getSmartLogger().write("-------------------------------------------------------------------");
        enableFinished = true;
    }

    private boolean loadPaperSupport() {
        if (!UltraCosmeticsData.get().getServerVersion().isAtLeast(ServerVersion.v1_20)) {
            // Commodore didn't have a new enough version of ASM to load Java 21 classes until 1.20
            return false;
        }
        try {
            paperSupport = Class.forName("be.isach.ultracosmetics.paper.PaperSupportImpl").asSubclass(PaperSupport.class).getDeclaredConstructor().newInstance();
            getSmartLogger().write("Paper-specific features enabled");
            return true;
        } catch (ReflectiveOperationException | UnsupportedClassVersionError | IllegalArgumentException e) {
            // ReflectiveOperationException shouldn't happen
            // UnsupportedClassVersionError is thrown when server is running on a version below Java 21
            // IllegalArgumentException is also thrown when the server is running on a version below Java 21
            // and CraftBukkit tries to process it.
            return false;
        }
    }

    /**
     * Called when plugin disables.
     */
    @Override
    public void onDisable() {
        shutdown();
    }

    public void shutdown() {
        // Prepare for re-enable
        HandlerList.unregisterAll(this);
        getScheduler().cancelAllTasks();

        // when the plugin is disabled from onEnable, skip cleanup
        if (!enableFinished) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (InventoryViewHelper.getTopInventory(player).getHolder() instanceof CosmeticsInventoryHolder) {
                player.closeInventory();
            }
        }

        if (mySqlConnectionManager != null && mySqlConnectionManager.success()) {
            mySqlConnectionManager.shutdown();
        }

        playerManager.dispose();

        UltraCosmeticsData.get().getVersionManager().getModule().disable();
    }

    public void reload() {
        getLogger().info("Shutting down...");
        shutdown();
        CosmeticType.removeAllTypes();
        getLogger().info("Starting up...");
        start(false);
        for (UCAddon addon : addons) {
            addon.reload(this);
        }
    }

    /**
     * Addons registered with this function will be notified after UC finishes reloading.
     *
     * @param addon The addon to register
     */
    public void registerAddon(UCAddon addon) {
        if (addons.contains(addon)) {
            throw new IllegalArgumentException("This addon has already been registered!");
        }
        addons.add(addon);
    }

    private <T extends Listener> T hookIfEnabled(String pluginName, Supplier<T> hookSupplier) {
        if (!getServer().getPluginManager().isPluginEnabled(pluginName)
                || !SettingsManager.getConfig().getBoolean(pluginName + "-Hook", true)) {
            return null;
        }
        T hook = hookSupplier.get();
        getServer().getPluginManager().registerEvents(hook, this);
        getSmartLogger().write();
        getSmartLogger().write("Hooked into " + pluginName);
        return hook;
    }

    private <T extends Listener> T hookIfEnabled(String pluginName, Supplier<T> hookSupplier, float requiredVersion) {
        if (!getServer().getPluginManager().isPluginEnabled(pluginName)) {
            return null;
        }
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        Pattern pattern = Pattern.compile("^\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(plugin.getDescription().getVersion());
        if (matcher.find()) {
            float version = Float.parseFloat(matcher.group());
            if (version < requiredVersion) {
                getSmartLogger().write(LogLevel.WARNING, pluginName + " " + requiredVersion + " or later is required for UC to hook it.");
                return null;
            }
        } else {
            getSmartLogger().write(LogLevel.WARNING, "Failed to parse " + pluginName + " version, hoping it's ok...");
        }
        return hookIfEnabled(pluginName, hookSupplier);
    }

    /**
     * Registers Listeners.
     */
    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new MainListener(), this);
        pluginManager.registerEvents(new EntitySpawningManager(), this);
        pluginManager.registerEvents(new EntityMountManager(), this);
        unmovableItemListener = new UnmovableItemListener(this);
        pluginManager.registerEvents(unmovableItemListener, this);
        // No need to register this one, it doesn't have any event handlers of its own
        entityDismountListener = new EntityDismountListener(this);
    }

    /**
     * Sets the economy up.
     */
    private void setupEconomy() {
        economyHandler = new EconomyHandler(this);
        UltraCosmeticsData.get().checkTreasureChests();
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this, 2629);
        String nms;
        ServerVersion sv = UltraCosmeticsData.get().getServerVersion();
        if (UltraCosmeticsData.get().getVersionManager().isUsingNMS()) {
            nms = sv.getNmsVersion();
        } else {
            nms = "NMS-less flattening";
        }
        String version = updateChecker.getCurrentVersion().versionWithClassifier();
        metrics.addCustomChart(new DrilldownPie("uc_by_mc", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(version, 1);
            map.put(nms, entry);
            return map;
        }));
        metrics.addCustomChart(new DrilldownPie("mc_by_uc", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(nms, 1);
            map.put(version, entry);
            return map;
        }));
        metrics.addCustomChart(new SimplePie("mysql_enabled", () -> String.valueOf(getConfig().getBoolean("MySQL.Enabled"))));
    }

    private boolean setUpConfig() {
        file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveResource("config.yml", false);
        }
        config = loadConfiguration(c -> c.load(file));
        if (config == null) return false;

        Reader reader = UltraCosmeticsData.get().getPlugin().getFileReader("config.yml");
        CustomConfiguration defaults = loadConfiguration(c -> c.load(reader));
        if (defaults == null) return false;

        configMigration();
        boolean lootCommandsPresent = config.isConfigurationSection("TreasureChests.Loots.Commands");
        boolean designsPresent = config.isConfigurationSection("TreasureChests.Designs") && !config.getConfigurationSection("TreasureChests.Designs").getKeys(false).isEmpty();

        for (String key : defaults.getKeys(true)) {
            if (key.startsWith("TreasureChests.Loots.Commands.") && lootCommandsPresent
                    && !key.endsWith("Overall-Chance")) {
                continue;
            }
            if (key.startsWith("TreasureChests.Locations.default")) continue;
            if (key.startsWith("TreasureChests.Designs") && designsPresent) continue;
            config.addDefault(key, defaults.get(key), defaults.comments(key));
        }
        for (String key : defaults.getConfigurationSection("TreasureChests.Loots").getKeys(false)) {
            String path = "TreasureChests.Loots." + key + ".Message.message";
            if (!config.isString(path)) continue;
            String msg = config.getString(path);
            config.set(path, msg.replaceAll("%(?!ammo|name|prefix|money)[\\w-]+%", "%cosmetic%"));
        }
        config.set("Disabled-Items", null);
        config.set("Menu-Item.Data", null);

        config.set("Supported-Languages", supportedLanguages, "Languages supported by this version of UltraCosmetics.", "This is not a configurable list, just informative.");

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void configMigration() {
        ConfigurationSection oldSQL = SettingsManager.getConfig().getConfigurationSection("Ammo-System-For-Gadgets.MySQL");
        if (oldSQL != null) {
            SettingsManager.getConfig().set("MySQL", oldSQL);
            SettingsManager.getConfig().set("Ammo-System-For-Gadgets.MySQL", null);
        }
        String oldMysqlKey = "Ammo-System-For-Gadgets.System";
        if (config.isString(oldMysqlKey)) {
            config.set("MySQL.Enabled", false);
            config.set(oldMysqlKey, null);
        }
        if (config.isString("MySQL.table")) {
            config.set("MySQL.table", null);
            if (config.getBoolean("MySQL.Enabled")) {
                config.set("MySQL.Enabled", false);
                config.set("MySQL.Legacy", true, "To remove the warning about how the SQL config options", "have changed, delete this key.");
            }
        }
        if (config.getBoolean("MySQL.Legacy")) {
            getSmartLogger().write(LogLevel.WARNING, "SQL config options have changed, please check they are correct.");
            getSmartLogger().write(LogLevel.WARNING, "Remove the 'Legacy' key in the MySQL block to remove this message.");
            addProblem(Problem.SQL_MIGRATION_REQUIRED);
        }

        if (config.isBoolean("Auto-Equip-Cosmetics.is-enabled")) {
            boolean autoEquip = config.getBoolean("Auto-Equip-Cosmetics.is-enabled");
            config.set("Auto-Equip-Cosmetics", autoEquip, "Allows for players to auto-equip on join cosmetics they had before disconnecting.", "Supports both flatfile and SQL, choosing SQL when possible.");
        }

        if (config.isBoolean("Menu-Item.Give-On-Join")) {
            boolean enabled = config.getBoolean("Menu-Item.Give-On-Join");
            config.set("Menu-Item.Enabled", enabled);
            config.set("Menu-Item.Give-On-Join", null);
            config.set("Menu-Item.Give-On-Respawn", null);
        }

        String oldEffects = "Treasure-Chests-Loot.Effects";
        if (config.isConfigurationSection(oldEffects)) {
            config.set("Treasure-Chests-Loot.Particle-Effects", config.getConfigurationSection(oldEffects));
            config.set(oldEffects, null);
        }

        upgradeIdsToMaterials();
    }

    private void migrateConfigToMiniMessage() {
        migrateKeyToMiniMessage("Menu-Item.Displayname");
        migrateKeyToMiniMessage("Menu-Item.Lore");
        migrateKeyToMiniMessage("No-Permission.Custom-Item.Name");
        migrateKeyToMiniMessage("No-Permission.Custom-Item.Lore");
        ConfigurationSection commands = config.getConfigurationSection("TreasureChests.Loots.Commands");
        for (String key : commands.getKeys(false)) {
            migrateKeyToMiniMessage(commands.getCurrentPath() + "." + key + ".Name");
        }
    }

    private void migrateKeyToMiniMessage(String path) {
        String raw;
        if (config.isString(path)) {
            raw = config.getString(path);
        } else if (config.isList(path)) {
            raw = String.join("\n", config.getStringList(path));
        } else {
            return;
        }
        if (!raw.contains("&")) return;
        MiniMessage mm = MessageManager.getMiniMessage();
        LegacyComponentSerializer deserializer = MessageManager.legacySerializer();
        String[] parts = raw.split("\n");
        StringBuilder builder = new StringBuilder(mm.serialize(deserializer.deserialize(parts[0])));
        for (int i = 1; i < parts.length; i++) {
            builder.append("\n").append(mm.serialize(deserializer.deserialize(parts[i])));
        }
        config.set(path, builder.toString());
    }

    private void setupCustomMainMenu() {
        File customFile = CustomMainMenu.getFile(this);
        if (!customFile.exists()) {
            saveResource(customFile.getName(), false);
        }
        CustomMainMenu customMenu = null;
        try {
            customMenu = new CustomMainMenu(this);
        } catch (IllegalArgumentException e) {
            getSmartLogger().write(SmartLogger.LogLevel.WARNING, "Failed to load custom main menu, please run it through a YAML checker");
        }
        if (customMenu.isEnabled()) {
            menus.setMainMenu(customMenu);
        }
    }

    /**
     * Get the instance of FoliaLib used by the plugin
     *
     * @return the instance of FoliaLib
     */
    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    /**
     * Get a platform-agnostic scheduler.
     *
     * @return the scheduler.
     */
    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }

    /**
     * Gets the Custom Player Manager.
     *
     * @return the Custom Player Manager.
     */
    public UltraPlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * @return Command Manager.
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Increase visibility of getFile() for Updater
     */
    @Override
    public File getFile() {
        return super.getFile();
    }

    /**
     * @return Config file
     */
    public File getConfigFile() {
        return file;
    }

    /**
     * Increase visibility of getTextResource for MessageManager
     */
    public Reader getFileReader(String path) {
        return getTextResource(path);
    }

    /**
     * @return Overwrites getConfig to return our own Custom Configuration.
     */
    @Override
    public CustomConfiguration getConfig() {
        return config;
    }

    /**
     * @return Smart Logger Instance.
     */
    public SmartLogger getSmartLogger() {
        return smartLogger;
    }

    /**
     * @return The Update Checker.
     */
    public UpdateManager getUpdateChecker() {
        return updateChecker;
    }

    /**
     * @return The menus.
     */
    public Menus getMenus() {
        return menus;
    }

    /**
     * @return MySql Manager.
     */
    public MySqlConnectionManager getMySqlConnectionManager() {
        return mySqlConnectionManager;
    }

    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public DiscordSRVHook getDiscordHook() {
        return discordHook;
    }

    public ChestSortHook getChestSortHook() {
        return chestSortHook;
    }

    public UnmovableItemListener getUnmovableItemListener() {
        return unmovableItemListener;
    }

    public EntityDismountListener getEntityDismountListener() {
        return entityDismountListener;
    }

    public TreasureChestManager getTreasureChestManager() {
        return treasureChestManager;
    }

    public CustomConfiguration loadConfiguration(FunctionalConfigLoader loaderFunc) {
        CustomConfiguration config;
        // In 1.18.1 and later, Spigot supports comment preservation and
        // writing comments programmatically, so use built-in methods if we can.
        // Check if the method exists before we load AutoCommentConfig
        try {
            ConfigurationSection.class.getDeclaredMethod("getComments", String.class);
            config = new AutoCommentConfiguration();
        } catch (NoSuchMethodException ignored) {
            // getComments() doesn't exist yet, load ManualCommentConfig
            config = new ManualCommentConfiguration();
        } catch (SecurityException e) {
            // ???
            e.printStackTrace();
            return null;
        }

        try {
            loaderFunc.load(config);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            getSmartLogger().write(LogLevel.ERROR, "Cannot load " + file, ex);
            return null;
        }
        return config;
    }

    private void upgradeIdsToMaterials() {
        upgradeKeyToMaterial("Categories.Gadgets.Main-Menu-Item", "409:0", XMaterial.PRISMARINE_SHARD);
        upgradeKeyToMaterial("Categories.Particle-Effects.Main-Menu-Item", "399:0", XMaterial.NETHER_STAR);
        upgradeKeyToMaterial("Categories.Mounts.Main-Menu-Item", "329:0", XMaterial.SADDLE);
        upgradeKeyToMaterial("Categories.Pets.Main-Menu-Item", "352:0", XMaterial.BONE);
        upgradeKeyToMaterial("Categories.Morphs.Main-Menu-Item", "334:0", XMaterial.LEATHER);
        upgradeKeyToMaterial("Categories.Hats.Main-Menu-Item", "314:0", XMaterial.GOLDEN_HELMET);
        upgradeKeyToMaterial("Categories.Suits.Main-Menu-Item", "299:0", XMaterial.LEATHER_CHESTPLATE);
        upgradeKeyToMaterial("Categories.Clear-Cosmetic-Item", "152:0", XMaterial.REDSTONE_BLOCK);

        upgradeKeyToMaterial("Categories.Previous-Page-Item", "368:0", XMaterial.ENDER_PEARL);
        upgradeKeyToMaterial("Categories.Next-Page-Item", "381:0", XMaterial.ENDER_EYE);
        upgradeKeyToMaterial("Categories.Back-Main-Menu-Item", "262:0", XMaterial.ARROW);
        upgradeKeyToMaterial("Categories.Self-View-Item.When-Enabled", "381:0", XMaterial.ENDER_EYE);
        upgradeKeyToMaterial("Categories.Self-View-Item.When-Disabled", "368:0", XMaterial.ENDER_PEARL);
        upgradeKeyToMaterial("Categories.Gadgets-Item.When-Enabled", "351:10", XMaterial.LIGHT_GRAY_DYE);
        upgradeKeyToMaterial("Categories.Gadgets-Item.When-Disabled", "351:8", XMaterial.GRAY_DYE);
        upgradeKeyToMaterial("Categories.Rename-Pet-Item", "421:0", XMaterial.NAME_TAG);

        upgradeKeyToMaterial("TreasureChests.Designs.Classic.center-block", "169:0", XMaterial.SEA_LANTERN);
        upgradeKeyToMaterial("TreasureChests.Designs.Classic.around-center", "5:0", XMaterial.OAK_PLANKS);
        upgradeKeyToMaterial("TreasureChests.Designs.Classic.third-blocks", "5:1", XMaterial.SPRUCE_PLANKS);
        upgradeKeyToMaterial("TreasureChests.Designs.Classic.below-chests", "17:0", XMaterial.OAK_LOG);
        upgradeKeyToMaterial("TreasureChests.Designs.Classic.barriers", "85:0", XMaterial.OAK_FENCE);

        upgradeKeyToMaterial("TreasureChests.Designs.Modern.center-block", "169:0", XMaterial.SEA_LANTERN);
        upgradeKeyToMaterial("TreasureChests.Designs.Modern.around-center", "159:11", XMaterial.BLUE_TERRACOTTA);
        upgradeKeyToMaterial("TreasureChests.Designs.Modern.third-blocks", "155:0", XMaterial.WHITE_TERRACOTTA);
        upgradeKeyToMaterial("TreasureChests.Designs.Modern.below-chests", "159:11", XMaterial.BLUE_TERRACOTTA);
        upgradeKeyToMaterial("TreasureChests.Designs.Modern.barriers", "160:3", XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);

        upgradeKeyToMaterial("TreasureChests.Designs.Nether.center-block", "89:0", XMaterial.GLOWSTONE);
        upgradeKeyToMaterial("TreasureChests.Designs.Nether.around-center", "88:0", XMaterial.SOUL_SAND);
        upgradeKeyToMaterial("TreasureChests.Designs.Nether.third-blocks", "87:0", XMaterial.NETHERRACK);
        upgradeKeyToMaterial("TreasureChests.Designs.Nether.below-chests", "112:0", XMaterial.NETHER_BRICKS);
        upgradeKeyToMaterial("TreasureChests.Designs.Nether.barriers", "113:0", XMaterial.NETHER_BRICK_FENCE);

        upgradeKeyToMaterial("Fill-Blank-Slots-With-Item.Item", "160:15", XMaterial.BLACK_STAINED_GLASS_PANE);
    }

    private void upgradeKeyToMaterial(String key, String oldValue, XMaterial newValue) {
        if (oldValue.equals(config.getString(key))) {
            if (!legacyMessagePrinted) {
                getSmartLogger().write(LogLevel.WARNING, "You seem to still have numeric IDs in your config, which UC no longer supports.");
                getSmartLogger().write(LogLevel.WARNING, "I'll attempt to upgrade them, but only if the values haven't been touched.");
                legacyMessagePrinted = true;
            }
            config.set(key, newValue.toString());
            getSmartLogger().write(LogLevel.INFO, "Successfully upgraded key '" + key + "' from '" + oldValue + "' to '" + newValue + "'!");
            // this code runs on every startup so don't print "failed to upgrade" message unless there's an actual issue
        } else if (legacyMessagePrinted) {
            getSmartLogger().write(LogLevel.WARNING, "Couldn't upgrade key '" + key + "' because it has been changed. Please upgrade it manually.");
        }
    }

    public void addProblem(Problem problem) {
        activeProblems.add(problem);
    }

    public Set<Problem> getProblems() {
        return activeProblems;
    }

    public Set<Problem> getSevereProblems() {
        Set<Problem> severe = new HashSet<>(activeProblems);
        severe.removeIf(p -> p.getSeverity() != ProblemSeverity.FATAL);
        return severe;
    }

    public PaperSupport getPaperSupport() {
        return paperSupport;
    }
}
