package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.task.UltraTask;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlaceBlocksRunnable extends UltraTask {
    private final TreasureChest chest;
    private final TreasureChestDesign design;
    private final boolean large;
    private final UltraCosmetics uc = UltraCosmeticsData.get().getPlugin();
    private int i = 6;
    private ChestParticleRunnable particleRunnable = null;

    public PlaceBlocksRunnable(TreasureChest chest) {
        this.chest = chest;
        this.design = chest.getDesign();
        this.large = chest.isLarge();
    }

    @Override
    public void run() {
        Player player = chest.getPlayer();
        UltraPlayerManager pm = uc.getPlayerManager();
        if (player == null || (pm.getUltraPlayer(player).getCurrentTreasureChest() != chest)) {
            cancel();
            return;
        }
        if (i <= 0) {
            particleRunnable = new ChestParticleRunnable(chest);
            particleRunnable.schedule();
            cancel();
            return;
        }
        doChestStages(player);
    }

    @Override
    public void schedule() {
        task = getScheduler().runAtLocationTimer(this.chest.getCenter(), this::run, 0L, 12L);
    }

    private void doChestStages(Player player) {
        if (i == 6) {
            doChestStage(player.getLocation().subtract(0, 1, 0), ChestBlockPattern.CENTER_BLOCK, design.getCenter());
        }
        if (i == 5) {
            boolean executed = doChestStage(chest.getCenter().add(0.0D, -1.0D, 0.0D), ChestBlockPattern.AROUND_CENTER, design.getBlocks2());
            if (!large) {
                i--;
                if (executed) {
                    // If this stage did run, we don't want to immediately trigger the next stage by the previous decrement
                    i--;
                    return;
                }
            }
        }
        if (i == 4) {
            doChestStage(chest.getCenter().add(0.0D, -1.0D, 0.0D), ChestBlockPattern.LARGE_AROUND_AROUND, design.getBlocks2());
        }
        if (i == 3) {
            doChestStage(chest.getCenter().add(0.0D, -1.0D, 0.0D), large ? ChestBlockPattern.LARGE_CORNERS : ChestBlockPattern.CORNERS, design.getBlocks3());
        }
        if (i == 2) {
            doChestStage(chest.getCenter().add(0.0D, -1.0D, 0.0D), large ? ChestBlockPattern.LARGE_BELOW_CHEST : ChestBlockPattern.BELOW_CHEST, design.getBelowChests());
        }
        if (i == 1) {
            doChestStage(chest.getCenter(), large ? ChestBlockPattern.LARGE_CORNERS : ChestBlockPattern.CORNERS, design.getBarriers());
        }
        i--;
    }

    /**
     * @return true if stage was executed
     */
    private boolean doChestStage(Location center, ChestBlockPattern pattern, XMaterial newData) {
        if (newData == null) {
            // Skip this stage, go to next
            i--;
            return false;
        }
        pattern.loop(center, loc -> chest.addRestoreBlock(loc.getBlock(), newData));
        return true;
    }

    public void propagateCancel() {
        cancel();
        if (particleRunnable != null) {
            particleRunnable.propogateCancel();
        }
    }
}
