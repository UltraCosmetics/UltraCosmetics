package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.run.FallDamageManager;
import be.isach.ultracosmetics.util.Area;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.EntityMountManager;
import be.isach.ultracosmetics.util.StructureRollback;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a rocket gadget summoned by a player.
 *
 * @author iSach
 * @since 08-17-2015
 */
public class GadgetRocket extends Gadget implements Updatable {

    private static final BlockFace[] CARDINAL = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final Material FENCE = XMaterial.OAK_FENCE.get();
    private static final Material QUARTZ_BLOCK = XMaterial.QUARTZ_BLOCK.get();
    private static final ParticleDisplay EMITTER = ParticleDisplay.of(XParticle.EXPLOSION_EMITTER);
    private final ParticleDisplay flame = ParticleDisplay.of(XParticle.FLAME).withCount(10).offset(0.3, 0.2, 0.3).withLocationCaller(() -> getPlayer().getLocation().subtract(0, 3, 0));
    private final ParticleDisplay lava = flame.copy().withParticle(XParticle.LAVA);

    private final StructureRollback rollback = new StructureRollback();
    private boolean stillEquipped = true;
    private boolean launching;
    private ArmorStand armorStand;
    // key is used for easy access for contains() checks
    private final List<FallingBlock> fallingBlocks = new ArrayList<>();
    private Entity playerVehicle = null;
    private int height;
    private RocketTask activeTask = null;
    private final XSound.SoundPlayer countdownSound;
    private final XSound.SoundPlayer liftoffSound;
    private final XSound.SoundPlayer flightSound1;
    private final XSound.SoundPlayer flightSound2;

    public GadgetRocket(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        countdownSound = XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(getPlayer());
        liftoffSound = XSound.ENTITY_GENERIC_EXPLODE.record().publicSound(true).soundPlayer().forPlayers(getPlayer());
        flightSound1 = XSound.ENTITY_BAT_LOOP.record().withVolume(1.5f).soundPlayer();
        flightSound2 = XSound.BLOCK_FIRE_EXTINGUISH.record().withVolume(0.025f).soundPlayer();
    }

    @Override
    protected void onRightClick() {
        getPlayer().setVelocity(new Vector(0, 1, 0));
        final Location loc = getPlayer().getLocation();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + 0.5);

        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
            if (getOwner() == null || getOwner().getCurrentGadget() != this) return;
            for (int i = 0; i < 2; i++) {
                Block center = loc.clone().add(0, i, 0).getBlock();
                for (BlockFace face : CARDINAL) {
                    rollback.setToRestore(center.getRelative(face), FENCE);
                }
                rollback.setToRestore(center.getRelative(BlockFace.UP), QUARTZ_BLOCK);
            }
            armorStand = loc.getWorld().spawn(loc.add(0, 1.5, 0), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
        }, 10);

        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
            if (getOwner() == null || getOwner().getCurrentGadget() != this) return;
            playerVehicle = null;
            boolean success = EntityMountManager.withBypass(() -> armorStand.addPassenger(getPlayer()));
            if (!success) {
                cleanup();
                return;
            }
            playerVehicle = armorStand;
            // prevent kicking
            enableFlight();
            activeTask = new RocketTask() {
                private int countdown = 3;

                @Override
                public void run() {
                    if (getOwner() == null || getPlayer() == null || !getPlayer().isOnline()) {
                        task.cancel();
                        return;
                    }

                    if (!isStillCurrentGadget()) {
                        task.cancel();
                        return;
                    }
                    if (countdown > 0) {
                        sendTitle(ChatColor.RED.toString() + ChatColor.BOLD + countdown);
                        countdownSound.play();
                        countdown--;
                        return;
                    }

                    stop();

                    sendTitle(MessageManager.getLegacyMessage("Gadgets.Rocket.Takeoff"));
                    liftoffSound.play();

                    final FallingBlock top = BlockUtils.spawnFallingBlock(getPlayer().getLocation().add(0, 3, 0), QUARTZ_BLOCK);
                    FallingBlock base = BlockUtils.spawnFallingBlock(getPlayer().getLocation().add(0, 2, 0), QUARTZ_BLOCK);
                    for (int i = 0; i < 2; i++) {
                        fallingBlocks.add(BlockUtils.spawnFallingBlock(getPlayer().getLocation().add(0, 1 + i, 1), FENCE));
                        fallingBlocks.add(BlockUtils.spawnFallingBlock(getPlayer().getLocation().add(0, 1 + i, -1), FENCE));
                        fallingBlocks.add(BlockUtils.spawnFallingBlock(getPlayer().getLocation().add(1, 1 + i, 0), FENCE));
                        fallingBlocks.add(BlockUtils.spawnFallingBlock(getPlayer().getLocation().add(-1, 1 + i, 0), FENCE));
                    }

                    fallingBlocks.add(top);
                    fallingBlocks.add(base);
                    boolean success = EntityMountManager.withBypass(() -> top.addPassenger(getPlayer()));
                    if (!success) {
                        cleanup();
                    }
                    playerVehicle = top;
                    launching = true;
                    activeTask = new RocketTask() {
                        @Override
                        public void run() {
                            if (getPlayer().getLocation().getBlockY() < height - 10) return;
                            playerVehicle = null;
                            if (!isStillCurrentGadget()) {
                                task.cancel();
                                activeTask = null;
                                return;
                            }
                            stop();
                            activeTask = null;
                        }

                        @Override
                        public void stop() {
                            fallingBlocks.forEach(Entity::remove);
                            fallingBlocks.clear();
                            FallDamageManager.addNoFall(getPlayer());
                            liftoffSound.play();
                            EMITTER.spawn(getPlayer().getLocation());
                            disableFlight();
                            launching = false;
                            task.cancel();
                        }
                    }.schedule(getUltraCosmetics(), getPlayer(), 5, 5);
                }

                @Override
                public void stop() {
                    playerVehicle = null;
                    armorStand.remove();
                    armorStand = null;

                    rollback.rollback();
                    sendTitle(" ");
                    task.cancel();
                }
            }.schedule(getUltraCosmetics(), getPlayer(), 0, 20);
        }, 12);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean checkRequirements(PlayerInteractEvent event) {
        if (activeTask != null) return false;
        Location loc = getPlayer().getLocation();
        height = Area.findMaxY(loc, 1);
        if (height - loc.getBlockY() < 25) {
            MessageManager.send(getPlayer(), "Gadgets.Rocket.Not-Enough-Space");
            return false;
        }
        if (!getPlayer().isOnGround()) {
            MessageManager.send(getPlayer(), "Gadgets.Rocket.Not-On-Ground");
            return false;
        }
        return true;
    }

    private boolean isStillCurrentGadget() {
        return stillEquipped;
    }

    @Override
    public void onUpdate() {
        for (FallingBlock fallingBlock : fallingBlocks) {
            fallingBlock.setVelocity(new Vector(0, 0.9, 0));
        }

        if (launching && !fallingBlocks.isEmpty()) {
            flame.spawn();
            lava.spawn();
            Location soundLoc = fallingBlocks.get(9).getLocation().clone().add(0, -1, 0);
            flightSound1.atLocation(soundLoc).play();
            flightSound2.atLocation(soundLoc).play();
        }
    }

    protected void cleanup() {
        rollback.rollback();
        for (FallingBlock fallingBlock : fallingBlocks) {
            fallingBlock.remove();
        }
        playerVehicle = null;
        fallingBlocks.clear();
        if (armorStand != null) {
            armorStand.remove();
        }
        disableFlight();
        launching = false;

        if (getPlayer() != null) {
            sendTitle(" ");
        }
        if (activeTask != null) {
            activeTask.cancel();
        }
    }

    @Override
    public void onEquip() {
        super.onEquip();
        getUltraCosmetics().getEntityDismountListener().addHandler(this, this::onDismount);
    }

    @Override
    public void onClear() {
        stillEquipped = false;
        cleanup();
        rollback.cleanup();
        getUltraCosmetics().getEntityDismountListener().removeHandler(this);
    }

    public boolean onDismount(Entity who, Entity dismounted) {
        if (who != getPlayer() || dismounted != playerVehicle) return false;
        disableFlight();
        activeTask.cancel();
        if (activeTask != null) {
            activeTask.stop();
            activeTask = null;
        }
        return false;
    }

    private void enableFlight() {
        getPlayer().setAllowFlight(true);
    }

    private void disableFlight() {
        if (getPlayer().getGameMode() != GameMode.CREATIVE) {
            getPlayer().setAllowFlight(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void sendTitle(String title) {
        getPlayer().sendTitle(title, "");
    }

    private abstract static class RocketTask {
        protected WrappedTask task;

        public abstract void stop();

        public abstract void run();

        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        public RocketTask schedule(UltraCosmetics ultraCosmetics, Player player, long delay, long period) {
            task = ultraCosmetics.getScheduler().runAtEntityTimer(player, this::run, delay, period);
            return this;
        }
    }
}
