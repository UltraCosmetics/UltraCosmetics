package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.MathUtils;
import be.isach.ultracosmetics.util.StructureRollback;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a smashdown gadget summoned by a player.
 *
 * @author iSach
 * @since 08-08-2015
 */
public class GadgetSmashDown extends Gadget implements PlayerAffectingCosmetic, Updatable {
    private static final Particle BLOCK_PARTICLE = XParticle.BLOCK.get();
    private final XSound.SoundPlayer useSound;
    private final XSound.SoundPlayer smashSound;
    private final XSound.SoundPlayer landSound;
    private final ParticleDisplay cloud = ParticleDisplay.of(XParticle.CLOUD).withLocationCaller(() -> getPlayer().getLocation());
    private final List<FallingBlock> fallingBlocks = new ArrayList<>();
    private boolean active = false;
    private int i = 1;
    private boolean playEffect;

    private WrappedTask gadgetTask;

    public GadgetSmashDown(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        useSound = XSound.ENTITY_FIREWORK_ROCKET_LAUNCH.record().withVolume(2.0f).publicSound(true).soundPlayer().forPlayers(getPlayer());
        smashSound = XSound.ENTITY_GENERIC_EXPLODE.record().withVolume(2.0f).publicSound(true).soundPlayer().forPlayers(getPlayer());
        landSound = XSound.BLOCK_ANVIL_BREAK.record().withVolume(0.05f).publicSound(true).soundPlayer().forPlayers(getPlayer());
    }

    @Override
    protected void onRightClick() {
        useSound.play();
        getPlayer().setVelocity(new Vector(0, 3, 0));

        gadgetTask = getUltraCosmetics().getScheduler().runAtEntityTimer(getPlayer(), () -> {
            if (getOwner() != null && getPlayer() != null && isEquipped()) {
                cloud.spawn();
            } else {
                gadgetTask.cancel();
            }
        }, 0, 1);

        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
            if (getOwner() != null && getPlayer() != null && isEquipped()) {
                gadgetTask.cancel();
                getOwner().applyVelocity(new Vector(0, -3, 0));
                active = true;
            }
        }, 25);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (active && event.getEntity() == getPlayer()) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onUpdate() {
        if (active && getPlayer().isOnGround()) {
            this.playEffect = true;
            getUltraCosmetics().getScheduler().runLaterAsync(() -> active = false, 5);
            return;
        }

        if (!playEffect) {
            return;
        }

        Location loc = getPlayer().getLocation();
        smashSound.play();

        if (i == 5) {
            playEffect = false;
            active = false;
            i = 1;
            return;
        }
        if (getOwner().getCurrentGadget() != this) {
            playEffect = false;
            active = false;
            return;
        }
        for (Block b : BlockUtils.getBlocksInRadius(loc.clone().add(0, -1, 0), i, true)) {
            if (b.getLocation().getBlockY() == loc.getBlockY() - 1) {
                if (!BlockUtils.isBadMaterial(b.getType())
                        && !StructureRollback.isBlockRollingBack(b)
                        && b.getType().isSolid()
                        && BlockUtils.isAir(b.getRelative(BlockFace.UP).getType())) {
                    getUltraCosmetics().getScheduler().runAtEntity(getPlayer(), (task) -> {
                        FallingBlock fb = BlockUtils.spawnFallingBlock(b.getLocation().clone().add(0, 1.1f, 0), b);

                        fb.setVelocity(new Vector(0, 0.3f, 0));
                        fb.setDropItem(false);
                        fallingBlocks.add(fb);
                        Player player = getPlayer();
                        fb.getNearbyEntities(1, 1, 1).stream().filter(ent -> ent != player
                                        && !(ent instanceof FallingBlock) && canAffect(ent, player))
                                .forEach(ent -> MathUtils.applyVelocity(ent, new Vector(0, 0.5, 0)));
                    });
                }
            }
        }
        i++;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockChangeState(EntityChangeBlockEvent event) {
        if (!fallingBlocks.remove(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
        FallingBlock fb = (FallingBlock) event.getEntity();
        BlockData data = fb.getBlockData();
        fb.getWorld().spawnParticle(BLOCK_PARTICLE, fb.getLocation(), 50, 0, 0, 0, 0.4d, data);
        landSound.play();
        event.getEntity().remove();
    }

    @Override
    public void onClear() {
        for (FallingBlock block : fallingBlocks) {
            block.remove();
        }
    }
}
