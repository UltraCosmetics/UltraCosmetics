package be.isach.ultracosmetics.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityUnleashEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class exists to work around Paper #13645
 */
public class CancelLeashDrop {
    private static final Method setDropLeash;

    static {
        Method m = null;
        try {
            m = EntityUnleashEvent.class.getDeclaredMethod("setDropLeash", boolean.class);
        } catch (NoSuchMethodException ignored) {
        }
        setDropLeash = m;
    }

    private CancelLeashDrop() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void apply(EntityUnleashEvent event) {
        if (setDropLeash == null) {
            ((LivingEntity) event.getEntity()).setLeashHolder(null);
            return;
        }
        try {
            setDropLeash.invoke(event, true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
