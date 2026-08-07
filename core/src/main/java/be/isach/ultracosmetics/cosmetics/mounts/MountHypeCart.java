package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.EntitySpawningManager;
import be.isach.ultracosmetics.util.PlayerUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;

public class MountHypeCart extends Mount {

    public MountHypeCart(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
        if (entity.isOnGround()) {
            Vector vector = PlayerUtils.getHorizontalDirection(getPlayer(), 7.6);
            if (Math.abs(vector.getX()) > 4) {
                vector.setX(vector.getX() < 0 ? -4 : 4);
            }
            if (Math.abs(vector.getZ()) > 4) {
                vector.setZ(vector.getZ() < 0 ? -4 : 4);
            }
            entity.setVelocity(vector);
        }
    }

    @Override
    protected Entity spawnEntity() {
        entity = EntitySpawningManager.withBypass(() -> UltraCosmeticsData.get().getVersionManager().getModule()
                .spawnCustomMinecart(getPlayer().getLocation()));
        return entity;
    }

    @Override
    protected void removeEntity() {
        UltraCosmeticsData.get().getVersionManager().getModule().removeCustomEntity(entity);
    }

    @EventHandler
    public void onDestroy(VehicleDestroyEvent event) {
        if (event.getVehicle() == entity) {
            event.setCancelled(true);
        }
    }
}
