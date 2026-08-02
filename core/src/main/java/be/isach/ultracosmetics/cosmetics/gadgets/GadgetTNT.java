package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GadgetTNT extends Gadget implements PlayerAffectingCosmetic {

    private static final ParticleDisplay EMITTER = ParticleDisplay.of(XParticle.EXPLOSION_EMITTER);
    private final Set<Entity> entities = new HashSet<>();
    private final XSound.SoundPlayer sound;

    public GadgetTNT(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        sound = XSound.ENTITY_GENERIC_EXPLODE.record().withVolume(1.4f).withPitch(1.5f).soundPlayer()
                .forPlayers(getPlayer());
    }

    @Override
    protected void onRightClick() {
        TNTPrimed tnt = getPlayer().getWorld().spawn(getPlayer().getLocation().add(0, 2, 0), TNTPrimed.class);
        // Vector stuff
        tnt.setFuseTicks(20);
        tnt.setVelocity(getPlayer().getLocation().getDirection().multiply(0.854321));
        entities.add(tnt);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (entities.contains(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemFrameBreak(HangingBreakEvent event) {
        for (Entity ent : entities) {
            if (ent.getWorld() != event.getEntity().getWorld()) {
                continue;
            }
            if (ent.getLocation().distanceSquared(event.getEntity().getLocation()) < 15 * 15) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        for (Entity tnt : entities) {
            if (tnt.getWorld() != event.getVehicle().getWorld()) {
                continue;
            }
            if (tnt.getLocation().distanceSquared(event.getVehicle().getLocation()) < 10 * 10) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!entities.remove(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
        EMITTER.spawn(event.getEntity().getLocation());
        sound.play();

        Player player = getPlayer();
        for (Entity ent : event.getEntity().getNearbyEntities(3, 3, 3)) {
            if (!canAffect(ent, player)) {
                continue;
            }
            Vector vector = getVector(event, ent);
            MathUtils.applyVelocity(ent, vector.multiply(1.3D).add(new Vector(0, 1.4D, 0)));
        }
    }

    @NotNull
    private static Vector getVector(EntityExplodeEvent event, Entity ent) {
        double dX = event.getEntity().getLocation().getX() - ent.getLocation().getX();
        double dY = event.getEntity().getLocation().getY() - ent.getLocation().getY();
        double dZ = event.getEntity().getLocation().getZ() - ent.getLocation().getZ();
        double yaw = Math.atan2(dZ, dX);
        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
        double X = Math.sin(pitch) * Math.cos(yaw);
        double Y = Math.sin(pitch) * Math.sin(yaw);
        double Z = Math.cos(pitch);

        return new Vector(X, Z, Y);
    }

    @Override
    public void onClear() {
        for (Entity ent : entities) {
            ent.remove();
        }
        entities.clear();
    }
}
