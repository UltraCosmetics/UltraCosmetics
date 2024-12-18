package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.task.UltraTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockViewUpdater extends UltraTask {
    private static final Set<Block> blocksUpdating = ConcurrentHashMap.newKeySet();
    private final Set<Block> blocks;
    private final int delay;

    public BlockViewUpdater(Set<Block> blocks, int delay) {
        this.blocks = blocks;
        this.delay = delay;
        addForProcessing(blocks);
    }

    @Override
    public void run() {
        removeForProcessing(blocks);
        for (Block block : blocks) {
            for (Player player : block.getWorld().getPlayers()) {
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }
        }
    }


    @Override
    public void schedule() {
        if (blocks.isEmpty()) return;
        Location location = blocks.iterator().next().getLocation();
        task = getScheduler().runAtLocationLater(location, this::run, delay);
    }

    public static boolean isUpdating(Block block) {
        return blocksUpdating.contains(block);
    }

    // Synchronized add and remove methods so we can properly minimize packets sent,
    // as well as avoid leaving any blocks stuck in queue.
    private static synchronized void addForProcessing(Set<Block> blocks) {
        blocks.removeAll(blocksUpdating);
        blocksUpdating.addAll(blocks);
    }

    private static synchronized void removeForProcessing(Set<Block> blocks) {
        blocksUpdating.removeAll(blocks);
    }
}
