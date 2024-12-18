package be.isach.ultracosmetics.run;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.task.UltraTask;
import org.bukkit.entity.Player;

/*
 * Mounts that aren't horses don't trigger PlayerMoveEvent I guess,
 * so we have to check manually for those.
 */

public class MountRegionChecker extends UltraTask {
    private UltraPlayer player;
    private UltraCosmetics uc;

    public MountRegionChecker(UltraPlayer player, UltraCosmetics uc) {
        this.player = player;
        this.uc = uc;
    }

    @Override
    public void run() {
        Player bukkitPlayer = player.getBukkitPlayer();
        // Mount#onClear() will cancel it for us
        if (bukkitPlayer == null) return;
        uc.getWorldGuardManager().doCosmeticCheck(bukkitPlayer, uc);
    }

    @Override public void schedule() {
        task = getScheduler().runAtEntityTimer(player.getBukkitPlayer(), this::run, 0, 1);
    }
}
