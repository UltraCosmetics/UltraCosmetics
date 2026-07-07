package be.isach.ultracosmetics.util;

import java.util.HashMap;
import java.util.Map;

public class CooldownMap<K> {
    private final long delay;
    private final Map<K, Long> cooldowns = new HashMap<>();

    public CooldownMap(long delayMs) {
        this.delay = delayMs;
    }

    public boolean isOnCooldown(K key) {
        return cooldowns.getOrDefault(key, 0L) > System.currentTimeMillis();
    }

    public void setCooldown(K key) {
        cooldowns.put(key, System.currentTimeMillis() + delay);
    }
}
