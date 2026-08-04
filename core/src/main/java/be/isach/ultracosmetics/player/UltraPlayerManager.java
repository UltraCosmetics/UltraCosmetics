package be.isach.ultracosmetics.player;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.run.LoadUltraPlayerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UltraPlayerManager {

    private final Map<UUID, UltraPlayer> playerCache;
    private final UltraCosmetics ultraCosmetics;

    public UltraPlayerManager(UltraCosmetics ultraCosmetics) {
        this.playerCache = new ConcurrentHashMap<>();
        this.ultraCosmetics = ultraCosmetics;
    }

    public void createUltraPlayer(UUID uuid) {
        // Happens if the player logs in before they're fully logged out,
        // like when they are kicked for connecting from another location.
        // In this case, schedule a task to load a new UltraPlayer as soon
        // as the old one is disposed of.
        if (playerCache.containsKey(uuid)) {
            new LoadUltraPlayerTask(uuid, this).schedule();
            return;
        }
        playerCache.put(uuid, new UltraPlayer(uuid, ultraCosmetics));
    }

    public boolean hasUltraPlayer(UUID uuid) {
        return playerCache.containsKey(uuid);
    }

    public UltraPlayer getUltraPlayer(Player player) {
        if (player == null) {
            return null;
        }
        return getUltraPlayer(player.getUniqueId());
    }

    public UltraPlayer getUltraPlayer(UUID uuid) {
        return playerCache.computeIfAbsent(uuid, u -> new UltraPlayer(u, ultraCosmetics));
    }

    public boolean remove(Player player) {
        return remove(player.getUniqueId());
    }

    public boolean remove(UUID uuid) {
        return playerCache.remove(uuid) != null;
    }

    public Collection<UltraPlayer> getUltraPlayers() {
        return playerCache.values();
    }

    /**
     * Initialize players.
     */
    public void initPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            getUltraPlayer(p).load();
        }
    }

    public void dispose() {
        Collection<UltraPlayer> set = playerCache.values();
        for (UltraPlayer cp : set) {
            cp.dispose();
        }

        playerCache.clear();
    }
}
