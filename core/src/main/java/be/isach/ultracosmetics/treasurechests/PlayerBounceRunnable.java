package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.task.UltraTask;
import be.isach.ultracosmetics.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerBounceRunnable extends UltraTask {
    private final TreasureChest chest;
    private final Location center;
    private final int searchDistance;
    private final Player player;
    private final UltraPlayer ultraPlayer;
    private final UltraPlayerManager pm = UltraCosmeticsData.get().getPlugin().getPlayerManager();

    public PlayerBounceRunnable(TreasureChest chest) {
        this.chest = chest;
        this.center = chest.getCenter();
        this.player = chest.getPlayer();
        this.ultraPlayer = UltraCosmeticsData.get().getPlugin().getPlayerManager().getUltraPlayer(player);
        searchDistance = chest.isLarge() ? 3 : 2;
    }

    @Override
    public void run() {
        if (ultraPlayer.getCurrentTreasureChest() != chest) {
            cancel();
            return;
        }
        if (player.getWorld() != center.getWorld() || player.getLocation().distanceSquared(center) > 2.25) {
            getScheduler().teleportAsync(player, center);
        }
        for (Entity ent : player.getNearbyEntities(searchDistance, searchDistance, searchDistance)) {
            if (!TreasureChestManager.shouldPush(ultraPlayer, ent)) continue;
            if (chest.isSpecialEntity(ent)) continue;
            // Passed all checks, launch the entity!
            Vector v = ent.getLocation().toVector().subtract(player.getLocation().toVector()).multiply(0.5).setY(1);
            MathUtils.applyVelocity(ent, v.add(MathUtils.getRandomCircleVector().multiply(0.2)));
        }
    }

    @Override
    public void schedule() {
        task = getScheduler().runAtEntityTimer(ultraPlayer.getBukkitPlayer(), this::run, 0L, 1L);
    }

}
