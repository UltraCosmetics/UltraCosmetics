package be.isach.ultracosmetics.run;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.task.UltraTask;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sacha on 15/12/15.
 */
public class FallDamageManager extends UltraTask {

    public static final List<Entity> noFallDamage = Collections.synchronizedList(new ArrayList<>());
    public static final List<Entity> queue = Collections.synchronizedList(new ArrayList<>());

    public static void addNoFall(Entity entity) {
        if (!queue.contains(entity)
                && !noFallDamage.contains(entity)) {
            queue.add(entity);
        }
    }

    public static boolean shouldBeProtected(Entity entity) {
        return noFallDamage.contains(entity) || queue.contains(entity);
    }

    @Override
    public void run() {
        List<Entity> toRemove = new ArrayList<>();
        synchronized (noFallDamage) {
            for (Entity ent : noFallDamage) {
                if (ent.isOnGround()) {
                    toRemove.add(ent);
                }
            }
        }
        UltraCosmeticsData.get().getPlugin().getScheduler().runLaterAsync(() -> noFallDamage.removeAll(toRemove), 5);
        noFallDamage.addAll(queue);
        queue.clear();
    }

    @Override public void schedule() {
        task = getScheduler().runTimerAsync(this::run, 0, 1);
    }
}
