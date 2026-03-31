package be.isach.ultracosmetics.version;

import be.isach.ultracosmetics.version.dummy.DummyEntityUtil;
import be.isach.ultracosmetics.version.dummy.DummyModule;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VersionManager {
    public static final String PACKAGE = "be.isach.ultracosmetics";
    private static final Map<UUID, Integer> WORLD_MIN_HEIGHTS = new HashMap<>();
    private static final Map<UUID, Integer> WORLD_MAX_HEIGHTS = new HashMap<>();

    private final ServerVersion serverVersion;
    private final boolean useNMS;
    private final IModule module;
    private final IEntityUtil entityUtil;

    public VersionManager(ServerVersion serverVersion, boolean useNMS) throws ReflectiveOperationException {
        this.serverVersion = serverVersion;
        this.useNMS = useNMS;
        if (useNMS) {
            module = loadModule("VersionModule");
            entityUtil = loadModule("EntityUtil");
        } else {
            module = new DummyModule();
            entityUtil = new DummyEntityUtil();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T loadModule(String name) throws ReflectiveOperationException {
        return (T) Class.forName(PACKAGE + "." + serverVersion.getModule() + "." + name).getConstructor().newInstance();
    }

    public IEntityUtil getEntityUtil() {
        return entityUtil;
    }

    public IModule getModule() {
        return module;
    }

    public boolean isUsingNMS() {
        return useNMS;
    }

    public static int getWorldMinHeight(World world) {
        return WORLD_MIN_HEIGHTS.computeIfAbsent(world.getUID(), w -> {
            try {
                return world.getMinHeight();
            } catch (NoSuchMethodError ex) {
                return 0;
            }
        });
    }

    public static int getWorldMaxHeight(World world) {
        return WORLD_MAX_HEIGHTS.computeIfAbsent(world.getUID(), w -> {
            try {
                return world.getMaxHeight();
            } catch (NoSuchMethodError ex) {
                return 255;
            }
        });
    }
}
