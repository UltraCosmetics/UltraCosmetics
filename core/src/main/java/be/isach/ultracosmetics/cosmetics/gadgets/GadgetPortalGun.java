package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.PortalLoc;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an instance of a portal gun gadget summoned by a player.
 *
 * @author iSach
 * @since 08-07-2015
 */
public class GadgetPortalGun extends Gadget implements PlayerAffectingCosmetic, Updatable {
    private static final double CIRCLE_STEP = (2 * Math.PI) / 20;
    private final Set<UUID> playersOnCooldown = new HashSet<>();
    private final PortalLoc red = new PortalLoc(255, 0, 0);
    private final PortalLoc blue = new PortalLoc(31, 0, 127);
    private final boolean affectsOthers;
    private final XSound.SoundPlayer useSound;
    private final XSound.Record teleportSound;

    public GadgetPortalGun(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        displayCooldownMessage = false;
        affectsOthers = SettingsManager.getConfig().getBoolean(getOptionPath("Affects-Others"));
        useSound = XSound.ENTITY_ENDERMAN_TELEPORT.record().withVolume(0.2f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
        teleportSound = XSound.ENTITY_ENDERMAN_TELEPORT.record();
    }

    @Override
    protected void onRightClick() {
        handleClick(blue);
    }

    @Override
    protected void onLeftClick() {
        handleClick(red);
    }

    private void handleClick(PortalLoc portalLoc) {
        useSound.play();
        List<Block> sight = getPlayer().getLastTwoTargetBlocks(null, 20);
        Block target = sight.get(1);
        Location playerFaceLoc = getPlayer().getEyeLocation().add(getPlayer().getEyeLocation().getDirection().multiply(0.6));

        Particles.line(playerFaceLoc, target.getLocation(), 0.25, portalLoc.getParticle());

        BlockFace face = getBlockFace(sight.get(0), target);
        Location loc = target.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        // BlockFace#getDirection does not exist in 1.8
        Vector faceVector = new Vector(face.getModX(), face.getModY(), face.getModZ());
        loc.add(faceVector.multiply(-0.25));
        portalLoc.setLocation(loc);
        portalLoc.setFace(face);
    }

    public float getPitch(BlockFace bf) {
        if (bf == BlockFace.UP) {
            return -90;
        } else if (bf == BlockFace.DOWN) {
            return 90;
        }
        return 0;
    }

    public float getYaw(BlockFace bf) {
        if (bf == BlockFace.WEST) {
            return 90;
        } else if (bf == BlockFace.EAST) {
            return -90;
        } else if (bf == BlockFace.NORTH) {
            return 180;
        } else { // BlockFace.SOUTH or something else
            return 0;
        }
    }

    public BlockFace getBlockFace(Block a, Block b) {
        for (BlockFace bf : BlockFace.values()) {
            if (a.getRelative(bf).getLocation().equals(b.getLocation())) {
                return bf.getOppositeFace();
            }
        }
        return null;
    }

    private boolean portalTeleportCheck(Player player, PortalLoc portalLoc, PortalLoc dest) {
        BlockFace face = portalLoc.getFace();
        Location playerLoc;
        if (face == BlockFace.DOWN) {
            playerLoc = player.getEyeLocation();
        } else if (face == BlockFace.UP) {
            playerLoc = player.getLocation();
        } else {
            playerLoc = player.getLocation().add(0, 1, 0);
        }

        // 'distanceSquared' is faster than 'distance', and sqrt(1) == 1 anyway
        if (playerLoc.getWorld() != portalLoc.getLocation().getWorld() || playerLoc.distanceSquared(portalLoc.getLocation()) > 1) {
            return false;
        }
        playersOnCooldown.add(player.getUniqueId());
        Location loc = dest.getLocation().clone();
        BlockFace destFace = dest.getFace();
        loc.setYaw(getYaw(destFace));
        loc.setPitch(getPitch(destFace));
        teleport(player, loc, destFace.getDirection().multiply(0.3));

        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> playersOnCooldown.remove(player.getUniqueId()), 20);
        return true;
    }

    private void checkPortals() {
        if (!red.isValid() || !blue.isValid()) return;
        if (red.getLocation().getWorld() != blue.getLocation().getWorld()) {
            red.clear();
            blue.clear();
            MessageManager.send(getPlayer(), "Gadgets.PortalGun.Different-Worlds");
            return;
        }
        Player owner = getPlayer();
        for (Player player : owner.getWorld().getPlayers()) {
            if (playersOnCooldown.contains(player.getUniqueId())) {
                continue;
            }
            if (player != owner && (!affectsOthers || !canAffect(player, owner))) {
                continue;
            }
            if (!portalTeleportCheck(player, red, blue)) {
                portalTeleportCheck(player, blue, red);
            }
        }
    }

    private void showParticles(PortalLoc portalLoc) {
        if (!portalLoc.isValid()) return;
        Location loc = portalLoc.getLocation().clone();
        BlockFace face = portalLoc.getFace();
        ParticleDisplay particle = portalLoc.getParticle();
        for (int i = 0; i < 20; i++) {
            double angle = i * CIRCLE_STEP;
            Vector v = new Vector();
            double a1 = Math.cos(angle);
            double a2 = Math.sin(angle);
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                v.setX(a1);
                v.setY(a2);
            } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                v.setY(a1);
                v.setZ(a2);
            } else {
                v.setX(a1);
                v.setZ(a2);
            }
            particle.spawn(loc.clone().add(v));
        }
    }

    @Override
    public void onUpdate() {
        checkPortals();
        showParticles(red);
        showParticles(blue);
    }

    @Override
    protected boolean checkRequirements(PlayerInteractEvent event) {
        List<Block> sight = getPlayer().getLastTwoTargetBlocks(null, 20);
        if (sight.size() < 2 || BlockUtils.isAir(sight.get(1).getType())) {
            MessageManager.send(getPlayer(), "Gadgets.PortalGun.No-Block-Range");
            return false;
        }
        return true;
    }

    private void teleport(final Entity entity, final Location location, final Vector velocity) {
        getUltraCosmetics().getScheduler().runNextTick((task) -> {
            getUltraCosmetics().getScheduler().teleportAsync(entity, location);
            entity.setVelocity(velocity);
            if (entity instanceof Player) {
                teleportSound.soundPlayer().forPlayers((Player) entity).play();
            }
        });
    }

    @Override
    public void onClear() {
        red.clear();
        blue.clear();
    }
}
