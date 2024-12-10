package be.isach.ultracosmetics.run;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.task.UltraTask;
import org.bukkit.entity.Player;

/**
 * Project: UltraCosmetics
 * Package: be.isach.ultracosmetics.tick
 * Created by: Sacha
 * Created on: 21th June, 2016
 * at 14:03
 */
public class InvalidWorldChecker extends UltraTask {

    private UltraCosmetics ultraCosmetics;

    public InvalidWorldChecker(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
    }

    @Override
    public void run() {
        for (UltraPlayer ultraPlayer : ultraCosmetics.getPlayerManager().getUltraPlayers()) {
            Player p = ultraPlayer.getBukkitPlayer();
            // not sure what causes p to be null, but it happens in some circumstances apparently
            // https://mcpaste.io/1bbcbf856c5e503b
            if (p == null) return;
            if (!SettingsManager.isAllowedWorld(p.getWorld())) {
                ultraPlayer.removeMenuItem();
                ultraPlayer.withPreserveEquipped(() -> {
                    if (ultraPlayer.clear()) {
                        MessageManager.send(p, "World-Disabled");
                    }
                });
            }
        }
    }

    @Override
    public void schedule() {
        task = getScheduler().runTimer(this::run, 0, 5);
    }
}
