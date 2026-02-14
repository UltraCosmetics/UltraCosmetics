package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.EntityCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.run.MountRegionChecker;
import be.isach.ultracosmetics.task.UltraTask;
import be.isach.ultracosmetics.util.Area;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.EntityMountManager;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.version.VersionManager;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an instance of a mount summoned by a player.
 *
 * @author iSach
 * @since 08-03-2015
 */
public abstract class Mount extends EntityCosmetic<MountType, Entity> implements Updatable {
    private UltraTask mountRegionTask = null;

    protected boolean beingRemoved = false;
    protected final boolean placesBlocks = getType().doesPlaceBlocks();

    public Mount(UltraPlayer ultraPlayer, MountType type, UltraCosmetics ultraCosmetics) {
        super(ultraPlayer, type, ultraCosmetics);
    }

    @Override
    public void onEquip() {

        entity = spawnEntity();

        if (entity instanceof LivingEntity) {
            setMovementSpeed(getType().getMovementSpeed());
            if (entity instanceof Ageable) {
                ((Ageable) entity).setAdult();
            } else if (entity instanceof Slime) {
                ((Slime) entity).setSize(3);
            }
        }
        entity.setCustomNameVisible(true);
        getUltraCosmetics().getPaperSupport().setCustomName(entity, getType().getName(getPlayer()));
        EntityMountManager.withBypass(() -> entity.addPassenger(getPlayer()));
        entity.setPersistent(false);
        entity.setMetadata("Mount", new FixedMetadataValue(UltraCosmeticsData.get().getPlugin(), "UltraCosmetics"));
        setupEntity();

        if (!getUltraCosmetics().getWorldGuardManager().isHooked()) return;
        // Horses trigger PlayerMoveEvent so the standard WG move handler will be sufficient
        if (isHorse(entity.getType())) return;
        mountRegionTask = new MountRegionChecker(getOwner(), getUltraCosmetics());
        mountRegionTask.schedule();
    }

    @Override
    protected void scheduleTask() {
        if (getType().getRepeatDelay() == 0) return;
        task = getUltraCosmetics().getScheduler().runAtEntityTimer(getPlayer(), this::run, 1, getType().getRepeatDelay());
    }

    @Override
    protected boolean tryEquip() {
        // If the entity is a monster and the world is set to peaceful, we can't spawn it
        if (getType().isMonster() && getPlayer().getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            getOwner().sendMessage(MessageManager.getMessage("Mounts.Cant-Spawn"));
            return false;
        }

        Location center = getPlayer().getLocation();
        center.setY(Math.ceil(center.getY()));
        Area area = new Area(center, 1, 1);
        if (!area.isTransparent()) {
            getOwner().sendMessage(MessageManager.getMessage("Mounts.Not-Enough-Room"));
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if (entity.getPassengers().isEmpty() && entity.getTicksLived() > 10) {
            clear();
            return;
        }

        if (!entity.isValid()) {
            task.cancel();
            return;
        }

        // Prevents players on mounts from being able to fall in the void infinitely.
        if (entity.getLocation().getY() <= VersionManager.getWorldMinHeight(entity.getWorld()) - 15) {
            clear();
            return;
        }

        if (getOwner() != null
                && Bukkit.getPlayer(getOwnerUniqueId()) != null
                && getOwner().getCurrentMount() != null
                && getOwner().getCurrentMount().getType() == getType()) {
            onUpdate();
        } else {
            task.cancel();
        }
    }

    @Override
    protected void onClear() {
        beingRemoved = true;
        removeEntity();

        if (mountRegionTask != null) {
            mountRegionTask.cancel();
        }
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(VehicleExitEvent event) {
        if (event.getVehicle().getType() == EntityType.MINECART) {
            return;
        }

        if (event.getVehicle() == entity && !beingRemoved && event.getExited() == getPlayer()) {
            clear();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() == getPlayer() && getOwner().getCurrentMount() == this
                && !getUltraCosmetics().getConfig().getBoolean("allow-damage-to-players-on-mounts")) {
            event.setCancelled(true);
        }
    }

    @Override
    protected void onPortal() {
        entity.remove();
        if (mountRegionTask != null) mountRegionTask.cancel();
        getUltraCosmetics().getScheduler().runAtEntityLater(getEntity(), this::onEquip, 1);
    }

    @EventHandler
    public void openInv(InventoryOpenEvent event) {
        if (!isHorse(getType().getEntityType())) return;
        if (getOwner() != null
                && getPlayer() != null
                && event.getPlayer() == getPlayer()
                && event.getInventory().equals(((InventoryHolder) entity).getInventory())) {
            event.setCancelled(true);
        }
    }

    private boolean isHorse(EntityType type) {
        return AbstractHorse.class.isAssignableFrom(type.getEntityClass());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (placesBlocks
                && event.getPlayer() == getPlayer()
                && getOwner().getCurrentMount() == this
                && SettingsManager.getConfig().getBoolean("Mounts-Block-Trails")) {
            List<XMaterial> mats = ItemFactory.getXMaterialListFromConfig("Mounts." + getType().getConfigName() + ".Blocks-To-Place");
            if (mats.size() == 0) {
                return;
            }
            Map<Block, XMaterial> updates = new HashMap<>();
            for (Block b : BlockUtils.getBlocksInRadius(event.getPlayer().getLocation(), 3, false)) {
                if (b.getLocation().getBlockY() == event.getPlayer().getLocation().getBlockY() - 1) {
                    XMaterial mat = mats.get(RANDOM.nextInt(mats.size()));
                    updates.put(b, mat);
                }
            }
            BlockUtils.setToRestore(updates, 20);
        }
    }
}
