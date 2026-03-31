package be.isach.ultracosmetics;

import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.util.Problem;
import be.isach.ultracosmetics.util.SmartLogger;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import be.isach.ultracosmetics.version.ServerVersion;
import be.isach.ultracosmetics.version.VersionManager;
import com.cryptomorin.xseries.reflection.XReflection;
import me.gamercoder215.mobchip.abstraction.ChipUtil;

/**
 * This class is only for cleaning main class a bit.
 *
 * @author iSach
 * @since 08-05-2016
 */
public class UltraCosmeticsData {

    private static UltraCosmeticsData instance;

    /**
     * True -> should execute custom command when going back to main menu.
     */
    private boolean customCommandBackArrow;

    /**
     * Command to execute when going back to Main Menu.
     */
    private String customBackMenuCommand;

    /**
     * Determines if Ammo Use is enabled.
     */
    private boolean ammoEnabled;

    /**
     * Determines if File Storage is enabled.
     */
    private boolean fileStorage = true;

    /**
     * Determines if Treasure Chests are enabled.
     */
    private boolean treasureChests;

    /**
     * Determines if Treasure Chest Money Loot enabled.
     */
    private boolean moneyTreasureLoot;

    /**
     * Determines if Gadget Cooldown should be shown in action bar.
     */
    private boolean cooldownInBar;

    /**
     * Should the GUI close after Cosmetic Selection?
     */
    private boolean closeAfterSelect;

    /**
     * If true, the color will be removed in placeholders.
     */
    private boolean placeHolderColor;

    /**
     * Language set in config.yml
     */
    private String language;

    /**
     * If false, players will not be able to purchase ammo
     */
    private boolean ammoPurchase;

    /**
     * Whether NMS support should be loaded. Options are "auto", "no", and "force"
     */
    private String useNMS;

    /**
     * Server NMS version.
     */
    private ServerVersion serverVersion;

    /**
     * NMS Version Manager.
     */
    private VersionManager versionManager;

    private final boolean mobchipAvailable = checkMobChipAvailable();

    private final UltraCosmetics ultraCosmetics;

    private boolean cosmeticsProfilesEnabled;
    private boolean cosmeticsAffectEntities;

    public UltraCosmeticsData(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
    }

    public static UltraCosmeticsData get() {
        return instance;
    }

    public static void init(UltraCosmetics ultraCosmetics) {
        instance = new UltraCosmeticsData(ultraCosmetics);
    }

    /**
     * Check Treasure Chests requirements.
     */
    protected void checkTreasureChests() {
        moneyTreasureLoot = ultraCosmetics.getEconomyHandler().isUsingEconomy()
                && SettingsManager.getConfig().getBoolean("TreasureChests.Loots.Money.Enabled");
        treasureChests = SettingsManager.getConfig().getBoolean("TreasureChests.Enabled");
    }

    protected boolean initModule() {
        SmartLogger logger = ultraCosmetics.getSmartLogger();
        logger.write("Initializing module " + serverVersion + " (expected version: " + serverVersion.canonicalName() + ")");

        // If NMS is force enabled and we don't know what version we're on, don't use strict version checking.
        boolean isNmsDowngrade = false;
        if (useNMS.equalsIgnoreCase("force") && serverVersion == ServerVersion.NEW) {
            isNmsDowngrade = true;
            // If we're forcing NMS and we don't know the version, assume we're on the latest version.
            serverVersion = ServerVersion.latest();
        }
        if (useNMS.equalsIgnoreCase("no")) {
            logger.write("NMS support has been disabled in the config, will run without it.");
        } else if (isNmsDowngrade || (serverVersion.isNmsSupported() && ServerVersion.getMinecraftVersion().equals(serverVersion.getName()))) {
            if (startNMS()) return true;
        } else {
            logger.write("Loading NMS-less mode...");
        }

        try {
            versionManager = new VersionManager(serverVersion, false);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            logger.write(LogLevel.ERROR, "Failed to start NMS-less module, please report this error.");
            return false;
        }
        logger.write("Loaded NMS-less mode");
        return true;
    }

    /*
     * Returns false if UC should use NMS-less mode, or true if no further setup is needed.
     */
    protected boolean startNMS() {
        SmartLogger logger = ultraCosmetics.getSmartLogger();

        try {
            versionManager = new VersionManager(serverVersion, true);
        } catch (ReflectiveOperationException | NoClassDefFoundError e) {
            e.printStackTrace();
            logger.write(LogLevel.ERROR, "Couldn't find module for " + serverVersion + ", please report this issue.");
            logger.write("UC will try to continue in NMS-less mode.");
            ultraCosmetics.addProblem(Problem.NMS_LOAD_FAILURE);
            return false;
        }
        logger.write("Loaded NMS support");
        return true;
    }

    /**
     * Attempt to parse the Minecraft version of the server into a ServerVersion.
     *
     * @return The parsed server version, or {@code ServerVersion.NEW} if it couldn't be determined.
     */
    private ServerVersion parseVersion() {
        return ServerVersion.byId(XReflection.MAJOR_NUMBER == 1 ? XReflection.MINOR_NUMBER : XReflection.MAJOR_NUMBER);
    }

    /**
     * Checks to make sure UC is OK to run on this MC version
     *
     * @return the reason the check failed, or null if it succeeded.
     */
    protected Problem checkServerVersion() {
        serverVersion = parseVersion();
        // If we don't know the server version...
        if (serverVersion == ServerVersion.NEW) {
            // Error message printed in onEnable so it's more visible
            return Problem.BAD_MC_VERSION;
        }
        return null;
    }

    public boolean isMobChipAvailable() {
        return mobchipAvailable;
    }

    private boolean checkMobChipAvailable() {
        if (ServerVersion.isMobchipEdgeCase()) {
            return false;
        }

        try {
            ChipUtil.getWrapper();
            return true;
        } catch (IllegalStateException | NoClassDefFoundError e) {
            // NoClassDefFoundError can happen if we're on paper without remapping
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void initConfigFields() {
        this.fileStorage = !SettingsManager.getConfig().getBoolean("MySQL.Enabled");
        this.placeHolderColor = SettingsManager.getConfig().getBoolean("Chat-Cosmetic-PlaceHolder-Color");
        this.ammoEnabled = SettingsManager.getConfig().getBoolean("Ammo-System-For-Gadgets.Enabled");
        this.cooldownInBar = SettingsManager.getConfig().getBoolean("Categories.Gadgets.Cooldown-In-ActionBar");
        this.customCommandBackArrow = ultraCosmetics.getConfig().getBoolean("Categories.Back-To-Main-Menu-Custom-Command.Enabled");
        this.customBackMenuCommand = ultraCosmetics.getConfig().getString("Categories.Back-To-Main-Menu-Custom-Command.Command").replace("/", "");
        this.closeAfterSelect = ultraCosmetics.getConfig().getBoolean("Categories.Close-GUI-After-Select");
        this.cosmeticsProfilesEnabled = ultraCosmetics.getConfig().getBoolean("Auto-Equip-Cosmetics");
        this.language = SettingsManager.getConfig().getString("Language");
        this.ammoPurchase = SettingsManager.getConfig().getBoolean("Ammo-System-For-Gadgets.Allow-Purchase");
        this.cosmeticsAffectEntities = SettingsManager.getConfig().getBoolean("Cosmetics-Affect-Entities");
        this.useNMS = SettingsManager.getConfig().getString("Use-NMS", "auto");
        // I'm not sure why "no" is translated to "false", but this changes it back
        if (useNMS.equalsIgnoreCase("false")) useNMS = "no";
    }

    public boolean isAmmoEnabled() {
        return ammoEnabled;
    }

    public boolean shouldCloseAfterSelect() {
        return closeAfterSelect;
    }

    public boolean displaysCooldownInBar() {
        return cooldownInBar;
    }

    public boolean usingCustomCommandBackArrow() {
        return customCommandBackArrow;
    }

    public boolean usingFileStorage() {
        return fileStorage;
    }

    public boolean useMoneyTreasureLoot() {
        return moneyTreasureLoot;
    }

    public boolean arePlaceholdersColored() {
        return placeHolderColor;
    }

    public boolean areTreasureChestsEnabled() {
        return treasureChests;
    }

    public String getCustomBackMenuCommand() {
        return customBackMenuCommand;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isAmmoPurchaseEnabled() {
        return ammoEnabled && ammoPurchase;
    }

    public boolean isCosmeticsAffectEntities() {
        return cosmeticsAffectEntities;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    /**
     * @return UltraCosmetics instance. (As Plugin)
     */
    public UltraCosmetics getPlugin() {
        return ultraCosmetics;
    }

    public boolean areCosmeticsProfilesEnabled() {
        return cosmeticsProfilesEnabled;
    }

    public void setFileStorage(boolean fileStorage) {
        this.fileStorage = fileStorage;
    }
}
