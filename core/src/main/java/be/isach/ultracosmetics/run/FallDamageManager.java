package be.isach.ultracosmetics.run;

import be.isach.ultracosmetics.task.UltraTask;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Sacha on 15/12/15.
 */
public class FallDamageManager extends UltraTask {
    // Keys which map to this value have not started their countdown yet
    private static final int SENTINEL = Integer.MAX_VALUE;
    private static final int COUNTDOWN_TICKS = 5;
    private static final Map<Entity, Integer> noFallDamage = Collections.synchronizedMap(new HashMap<>());

    public static void addNoFall(Entity entity) {
        noFallDamage.put(entity, SENTINEL);
    }

    public static boolean shouldBeProtected(Entity entity) {
        return noFallDamage.containsKey(entity);
    }

    @Override
    public void run() {
        // Even though the map itself is synchronized, the docs say iteration must be synchronized manually
        synchronized (noFallDamage) {
            Iterator<Map.Entry<Entity, Integer>> iter = noFallDamage.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Entity, Integer> entry = iter.next();
                if (!entry.getKey().isValid()) {
                    iter.remove();
                    continue;
                }
                if (entry.getValue() == SENTINEL) {
                    if (entry.getKey().isOnGround()) {
                        entry.setValue(COUNTDOWN_TICKS);
                    }
                } else if (entry.getValue() == 0) {
                    iter.remove();
                } else {
                    entry.setValue(entry.getValue() - 1);
                }
            }
        }
    }

    @Override
    public void schedule() {
        task = getScheduler().runTimerAsync(this::run, 1, 1);
    }
}
