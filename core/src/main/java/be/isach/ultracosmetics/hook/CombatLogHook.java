package be.isach.ultracosmetics.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Reflective adapter for Lightly's internal combat-log module. Lightly is not published
 * as a Maven artifact and lives in a separate repo, so accessing its API via reflection
 * keeps UC's build self-contained. If Lightly is missing or its API shape changes, the
 * hook fails at construction and the caller no-ops.
 */
public class CombatLogHook {
    public static final String PLUGIN_NAME = "Lightly";

    private final Object handler;
    private final Method isInCombat;

    public CombatLogHook() throws ReflectiveOperationException {
        Class<?> lightlyClass = Class.forName("me.lightly.survival.Lightly");
        Object lightly = lightlyClass.getMethod("getInstance").invoke(null);
        this.handler = lightlyClass.getMethod("getCombatLogHandler").invoke(lightly);
        this.isInCombat = handler.getClass().getMethod("isInCombat", Player.class);
    }

    public boolean isInCombat(Player player) {
        try {
            return (Boolean) isInCombat.invoke(handler, player);
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    public static boolean isPluginPresent() {
        return Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }
}
