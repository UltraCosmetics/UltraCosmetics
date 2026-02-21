package be.isach.ultracosmetics.cosmetics.suits;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TrailSuit extends Suit implements Updatable {
    private final boolean trailEnabled = SettingsManager.getConfig().getBoolean(getOptionPath("Trail"), true);
    private final List<XMaterial> trailBlocks;

    public TrailSuit(UltraPlayer ultraPlayer, SuitType suitType, UltraCosmetics ultraCosmetics) {
        super(ultraPlayer, suitType, ultraCosmetics);
        this.trailBlocks = getTrailBlocks();
    }

    protected abstract List<XMaterial> getTrailBlocks();

    @Override
    public void onUpdate() {
        if (!trailEnabled || !isFullSuit()) return;
        Block block = getPlayer().getLocation().subtract(0, 1, 0).getBlock();
        if (BlockUtils.isAir(block.getType()) || BlockUtils.isRestoring(block)) {
            return;
        }
        // Easy way to make sure that walking in a straight line goes through each block sequence, even if we cross a
        // block we've already changed.
        int index = (block.getX() + block.getZ()) % trailBlocks.size();
        if (index < 0) {
            // -1 mod 3 is -1, but we want it to be 2
            index += trailBlocks.size();
        }
        Map<Block, XMaterial> updates = new HashMap<>();
        updates.put(block, trailBlocks.get(index));
        BlockUtils.setToRestore(updates, 60);
    }
}
