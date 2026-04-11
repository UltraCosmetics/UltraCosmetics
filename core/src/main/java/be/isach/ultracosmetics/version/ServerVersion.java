package be.isach.ultracosmetics.version;

import com.cryptomorin.xseries.reflection.XReflection;

/**
 * Created by Sacha on 6/03/16.
 */
public enum ServerVersion {

    // Do not supply a NMS revision when there is no NMS module.
    // 1.17 is the first version to support Java 17
    v1_17(17, 1),
    v1_18(18, 2),
    v1_19(19, 4),
    v1_20(20, 6),
    v1_21(21, 11, "v1_21_R7"),
    NMS("26+", "nms") {
        @Override
        public boolean isNmsSupportedOn(int major, int minor) {
            // optimism
            return major >= 26;
        }
    },
    NEW("???"),
    ;

    private final String name;
    private final int majorVer;
    private final int minorVer;
    // The name of the package containing support for this version, or null if none.
    private final String module;

    // Dummy constructor for placeholder versions
    private ServerVersion(String name) {
        this(name, null);
    }

    private ServerVersion(String name, String module) {
        this.name = name;
        this.majorVer = 0;
        this.minorVer = 0;
        this.module = module;
    }

    private ServerVersion(int majorVer, int minorVer) {
        this(majorVer, minorVer, null);
    }

    private ServerVersion(int majorVer, int minorVer, String module) {
        this.name = majorVer + (minorVer > 0 ? "." + minorVer : "");
        this.majorVer = majorVer;
        this.minorVer = minorVer;
        this.module = module;
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

    public boolean isNmsSupportedOn(int major, int minor) {
        return isNmsSupported() && majorVer == major && minorVer == minor;
    }

    public boolean isNmsSupported() {
        return module != null;
    }

    public String getModule() {
        return module;
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
        if (id >= 26) {
            return ServerVersion.NMS;
        }
        for (ServerVersion version : values()) {
            if (id == version.majorVer) {
                return version;
            }
        }
        return ServerVersion.NEW;
    }

    /**
     * Get the Minecraft version, without the leading "1.", if present.
     *
     * @return The minecraft version, like 17.1 or 26.1
     */
    public static String getMinecraftVersion() {
        StringBuilder builder = new StringBuilder();
        if (XReflection.MAJOR_NUMBER != 1) {
            builder.append(XReflection.MAJOR_NUMBER).append('.');
        }
        builder.append(XReflection.MINOR_NUMBER);
        if (XReflection.PATCH_NUMBER != 0) {
            builder.append('.').append(XReflection.PATCH_NUMBER);
        }
        return builder.toString();
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
