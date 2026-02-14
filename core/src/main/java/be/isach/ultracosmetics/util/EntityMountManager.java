package be.isach.ultracosmetics.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;

import java.util.function.Supplier;

public class EntityMountManager implements Listener {

    private static boolean bypass = false;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityMountEvent(EntityMountEvent event) {
        if (bypass) {
            event.setCancelled(false);
        }
    }

    public static <T> T withBypass(Supplier<T> supplier) {
        // No effect if we're already bypassing
        if (bypass) return supplier.get();
        bypass = true;
        try {
            return supplier.get();
        } finally {
            bypass = false;
        }
    }
}
