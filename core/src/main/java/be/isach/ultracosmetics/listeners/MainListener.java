package be.isach.ultracosmetics.listeners;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.util.CancelLeashDrop;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Main listener
 *
 * @author iSach
 * @since 12-25-2015
 */
public class MainListener implements Listener {
    private static final ParticleDisplay HEART_PARTICLES = ParticleDisplay.of(XParticle.HEART).withCount(4).offset(0.5);
    private static final NamespacedKey PET_COOLDOWN = new NamespacedKey(UltraCosmeticsData.get().getPlugin(), "pet_cooldown");

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().hasMetadata("NO_INTER") || event.getRightClicked().hasMetadata("C_AD_ArmorStand")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (isPet(event.getRightClicked()) || isMount(event.getRightClicked())) {
            event.setCancelled(true);

            // Use a cooldown for playing sounds so it's not too obnoxious
            PersistentDataContainer pdc = event.getRightClicked().getPersistentDataContainer();
            Long cooldown = pdc.get(PET_COOLDOWN, PersistentDataType.LONG);
            if (cooldown != null && cooldown > System.currentTimeMillis()) {
                return;
            }
            boolean babies = SettingsManager.getConfig().getBoolean("Pets-Are-Babies");
            double yOffset = event.getRightClicked().getHeight();
            if (babies && event.getRightClicked() instanceof Wither) {
                // Height for small withers isn't calculated correctly
                yOffset = 1;
            }
            HEART_PARTICLES.spawn(event.getRightClicked().getLocation().add(0, yOffset, 0));
            if (event.getRightClicked() instanceof Mob mob && !SettingsManager.getConfig().getBoolean("Pets-Are-Silent")) {
                Sound sound = mob.getAmbientSound();
                if (sound != null) {
                    float pitch = ThreadLocalRandom.current().nextFloat(0.8f, 1.2f);
                    if (babies) {
                        pitch += 0.5f;
                    }
                    mob.getWorld().playSound(mob, sound, 1, pitch);
                }
            }
            pdc.set(PET_COOLDOWN, PersistentDataType.LONG, System.currentTimeMillis() + 1000);
        }
    }

    @EventHandler
    public void onDamageEntity(EntityDamageEvent event) {
        if (isPet(event.getEntity()) || isMount(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (isPet(event.getDamager()) || isMount(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (isPet(event.getEntity())) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            Entity holder = entity.getLeashHolder();
            UltraCosmeticsData.get().getPlugin().getScheduler().teleportAsync(entity, holder.getLocation());
            CancelLeashDrop.apply(event);
        }
    }

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent event) {
        processPickup(event.getItem(), event);
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        processPickup(event.getItem(), event);
    }

    public void processPickup(Item item, Cancellable event) {
        if (item.hasMetadata("UNPICKABLEUP")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMerge(ItemMergeEvent event) {
        if (event.getEntity().hasMetadata("UNPICKABLEUP") || event.getTarget().hasMetadata("UNPICKABLEUP")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (isPet(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrowned(EntityTransformEvent event) {
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.DROWNED && event.getEntity().hasMetadata("Pet")) {
            event.setCancelled(true);
            ((Zombie) event.getEntity()).setConversionTime(Integer.MAX_VALUE);
        }
    }

    @EventHandler
    public void stopDragonDamage(EntityExplodeEvent event) {
        Entity e = event.getEntity();
        if (e instanceof EnderDragonPart) e = ((EnderDragonPart) e).getParent();
        if (isPet(e) || isMount(e)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity e = event.getDamager();
        if (e instanceof EnderDragonPart) {
            e = ((EnderDragonPart) e).getParent();
        }
        if (isPet(e) || isMount(e)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent event) {
        if (isPet(event.getEntity()) || isMount(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private boolean isPet(Entity entity) {
        if (entity == null) return false;
        return entity.hasMetadata("Pet");
    }

    private boolean isMount(Entity entity) {
        if (entity == null) return false;
        return entity.hasMetadata("Mount");
    }
}
