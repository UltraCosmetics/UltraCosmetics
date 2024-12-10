package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.task.UltraTask;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EntitySpawner<T extends Entity> extends UltraTask {
    private static final Material FIREWORK = XMaterial.FIREWORK_ROCKET.parseMaterial();
    private static final EntityType FIREWORK_ENTITY = XEntityType.FIREWORK_ROCKET.get();
    private final int limit = SettingsManager.getConfig().getInt("Max-Entity-Spawns-Per-Tick");
    private final EntityType type;
    private final Consumer<T> func;
    private final Location loc;
    private final boolean spread;
    private int remaining;
    private final Set<T> entities = new HashSet<>();
    private boolean scheduled = false;

    public EntitySpawner(EntityType type, Location loc, int amount, boolean spread, Consumer<T> func, UltraCosmetics ultraCosmetics) {
        this.type = type;
        this.loc = loc;
        this.remaining = amount;
        this.spread = spread;
        this.func = func;
        if (limit < 1 || amount <= limit) {
            run();
            return;
        }
        this.schedule();
        scheduled = true;
    }

    public EntitySpawner(EntityType type, Location loc, int amount, Consumer<T> func, UltraCosmetics ultraCosmetics) {
        this(type, loc, amount, false, func, ultraCosmetics);
    }

    public EntitySpawner(EntityType type, Location loc, int amount, UltraCosmetics ultraCosmetics) {
        this(type, loc, amount, e -> {
        }, ultraCosmetics);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        for (int i = 0; i < limit || limit == 0; i++) {
            if (remaining < 1) {
                if (scheduled) {
                    cancel();
                }
                return;
            }
            // TODO: make this dynamic? Currently used for parachute gadget
            Location spawnLoc = loc.clone();
            if (spread) {
                //noinspection IntegerDivisionInFloatingPointContext
                spawnLoc.add(remaining % 5 - 2, 0, remaining / 4 - 2);
            }

            T entity = EntitySpawningManager.withBypass(() -> (T) loc.getWorld().spawnEntity(spawnLoc, type));
            entity.setPersistent(false);
            func.accept(entity);
            entities.add(entity);

            remaining--;
        }
    }

    @Override
    public void schedule() {
        task = this.getScheduler().runAtLocationTimer(loc, this::run, 1, 1);
    }

    public Set<T> getEntities() {
        return entities;
    }

    public void removeEntity(Entity entity) {
        entity.remove();
        entities.remove(entity);
    }

    public void removeEntities() {
        for (T entity : entities) {
            entity.remove();
        }
        entities.clear();
        try {
            cancel();
        } catch (IllegalStateException ignored) {
        }
    }

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    public boolean contains(Entity entity) {
        return entities.contains(entity);
    }

    public static <K extends Entity> EntitySpawner<K> empty() {
        return new EntitySpawner<>(null, null, 0, null);
    }

    /**
     * Spawns 4 fireworks at the location given. The fireworks do not damage entities.
     *
     * @param location The location to spawn at
     * @param main     The primary color the firework should be
     * @param fade     The secondary color the firework should be
     * @param type     The firework effect type to use
     */
    public static void spawnFireworks(Location location, Color main, Color fade, FireworkEffect.Type type) {
        // EntitySpawner doesn't work well here, so just spawning manually
        Set<Firework> fireworks = new HashSet<>();
        FireworkMeta meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(FIREWORK);
        meta.addEffect(buildFireworkEffect(main, fade, type));
        for (int i = 0; i < 4; i++) {
            Firework f = (Firework) location.getWorld().spawnEntity(location, FIREWORK_ENTITY);
            f.setFireworkMeta(meta);
            f.setMetadata("uc_firework", new FixedMetadataValue(UltraCosmeticsData.get().getPlugin(), true));
            fireworks.add(f);
        }
        UltraCosmeticsData.get().getPlugin().getScheduler().runAtLocationLater(location, () -> {
            for (Firework f : fireworks) {
                f.detonate();
            }
        }, 2);
    }

    public static void spawnFireworks(Location location, Color main, Color fade) {
        spawnFireworks(location, main, fade, FireworkEffect.Type.BALL_LARGE);
    }

    private static FireworkEffect buildFireworkEffect(Color main, Color fade, FireworkEffect.Type type) {
        FireworkEffect.Builder builder = FireworkEffect.builder();
        return builder.flicker(false).trail(false).with(type).withColor(main).withFade(fade).build();
    }
}
