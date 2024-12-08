package be.isach.ultracosmetics.run;

import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.task.UltraTask;
import org.bukkit.Bukkit;

import java.util.UUID;

public class LoadUltraPlayerTask extends UltraTask {
    private final UUID uuid;
    private final UltraPlayerManager ultraPlayerManager;

    public LoadUltraPlayerTask(UUID uuid, UltraPlayerManager ultraPlayerManager) {
        this.uuid = uuid;
        this.ultraPlayerManager = ultraPlayerManager;
    }

    @Override
    public void run() {
        if (Bukkit.getPlayer(uuid) == null || ultraPlayerManager.hasUltraPlayer(uuid)) {
            return;
        }
        ultraPlayerManager.createUltraPlayer(uuid);
    }

    @Override public void schedule() {
        task = getScheduler().runLater(this::run, 2);
    }
}
