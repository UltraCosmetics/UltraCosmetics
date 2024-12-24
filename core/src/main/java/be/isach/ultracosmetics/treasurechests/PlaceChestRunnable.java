package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.task.UltraTask;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.block.Block;

public class PlaceChestRunnable extends UltraTask {
    private static final ParticleDisplay SMOKE = ParticleDisplay.of(XParticle.LARGE_SMOKE).withCount(5);
    private static final ParticleDisplay LAVA = ParticleDisplay.of(XParticle.LAVA).withCount(5);
    private final TreasureChest chest;
    private final Block chestBlock;
    private final int direction;
    private final XSound.SoundPlayer sound;
    private final int animationTime;

    public PlaceChestRunnable(TreasureChest chest, Block chestBlock, int direction, int animationTime) {
        this.chest = chest;
        this.chestBlock = chestBlock;
        this.direction = direction;
        this.animationTime = animationTime;
        sound = XSound.BLOCK_ANVIL_LAND.record().withVolume(1.4f).withPitch(1.5f).soundPlayer().forPlayers(chest.getPlayer());
    }

    @Override
    public void run() {
        chest.addChest(chestBlock, chest.getDesign().getChestType().getType());
        sound.play();
        SMOKE.spawn(chestBlock.getLocation());
        LAVA.spawn(chestBlock.getLocation());
        XBlock.setDirection(chestBlock, ChestParticleRunnable.getDirection(direction).getOppositeFace());
    }

    @Override
    public void schedule() {
        task = getScheduler().runAtLocationLater(chestBlock.getLocation(), this::run, animationTime);
    }
}
