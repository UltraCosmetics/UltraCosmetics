package be.isach.ultracosmetics.version;

import org.bukkit.Bukkit;

/**
 * Created by Sacha on 6/03/16.
 */
public enum ServerVersion {

    // Do not supply a mapping version when there is no NMS module.
    // 1.17 is the first version to support Java 17
    v1_17(17, 1),
    v1_18(18, 2),
    v1_19(19, 4),
    v1_20(20, 6, "ee13f98a43b9c5abffdcc0bb24154460", 4),
    v1_21(21, 11, "e3cd927e07e6ff434793a0474c51b2b9", 7),
    NEW("???"),
    ;

    private final String name;
    private final int majorVer;
    private final int minorVer;
    // mappingsVersion is a random string that is changed whenever NMS changes
    // which is more often than actual NMS revisions happen. You can find this
    // value by checking the source code of this method:
    // org.bukkit.craftbukkit.util.CraftMagicNumbers#getMappingsVersion
    // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/util/CraftMagicNumbers.java#240
    // getMappingsVersion was added in 1.13.2, earlier versions don't have it.
    private final String mappingsVersion;
    // The NMS revision the corresponding module is built for, or 0 for no module.
    private final int nmsRevision;

    // Dummy constructor for placeholder versions
    private ServerVersion(String name) {
        this.name = name;
        this.majorVer = 0;
        this.minorVer = 0;
        this.mappingsVersion = null;
        this.nmsRevision = 0;
    }

    private ServerVersion(int majorVer, int minorVer) {
        this(majorVer, minorVer, null, 0);
    }

    private ServerVersion(int majorVer, int minorVer, String mappingsVersion, int nmsRevision) {
        this.name = majorVer + (minorVer > 0 ? "." + minorVer : "");
        this.majorVer = majorVer;
        this.minorVer = minorVer;
        this.mappingsVersion = mappingsVersion;
        this.nmsRevision = nmsRevision;
    }

    public String getName() {
        return name;
    }

    public String canonicalName() {
        return (majorVer < 26 && majorVer > 0 ? "1." : "") + name;
    }

    public int getMajorVer() {
        return majorVer;
    }

    public int getMinorVer() {
        return minorVer;
    }

    public String getMappingsVersion() {
        return mappingsVersion;
    }

    public int getNMSRevision() {
        return nmsRevision;
    }

    public static ServerVersion earliest() {
        return values()[0];
    }

    /**
     * Look up the ServerVersion for the given ID.
     *
     * @param id The major version ID to look for, e.g. 26
     * @return The matching ServerVersion, or ServerVersion.NEW if no match was found.
     */
    public static ServerVersion byId(int id) {
        for (ServerVersion version : values()) {
            if (id == version.majorVer) {
                return version;
            }
        }
        return ServerVersion.NEW;
    }

    public boolean isAtLeast(ServerVersion version) {
        return this.compareTo(version) >= 0;
    }

    public boolean isNmsSupported() {
        return nmsRevision > 0;
    }

    public String getNmsVersion() {
        //noinspection UnnecessaryToStringCall
        return toString() + "_R" + nmsRevision;
    }

    /**
     * Get the Minecraft version, without the leading "1.", if present.
     *
     * @return The minecraft version, like 17.1 or 26
     */
    public static String getMinecraftVersion() {
        // Should be something like "1.21.11-R0.2-SNAPSHOT"
        String version = Bukkit.getBukkitVersion().split("-")[0];
        if (version.startsWith("1.")) {
            return version.substring(2);
        }
        return version;
    }

    public static boolean isMobchipEdgeCase() {
        // MobChip thinks it works on this version but it really has some critical issues due to mapping changes.
        return getMinecraftVersion().equals("19");
    }

    /**
     * Returns the latest supported version. NEW does not count
     */
    public static ServerVersion latest() {
        return values()[values().length - 2];
    }
}
