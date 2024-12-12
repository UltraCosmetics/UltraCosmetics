package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.util.Vector;

/**
 * Represents an instance of a thor hammer gadget summoned by a player.
 *
 * @author iSach
 * @since 08-08-2015
 */
public class GadgetThorHammer extends Gadget implements PlayerAffectingCosmetic {
    private Item hammer = null;
    private Vector v;

    public GadgetThorHammer(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        // I think this can only happen if a player is bypassing cooldowns
        if (hammer != null) {
            hammer.remove();
        }
        Vector velocity = getPlayer().getEyeLocation().getDirection().multiply(1.4);
        hammer = ItemFactory.spawnUnpickableItem(ItemFactory.create(XMaterial.IRON_AXE, getTypeName()), getPlayer().getEyeLocation(), velocity);
        getPlayer().getInventory().setItem(slot, null);
        v = getPlayer().getEyeLocation().getDirection().multiply(1.4).add(new Vector(0, 1, 0));
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
            if (hammer == null) return;
            hammer.setVelocity(getPlayer().getEyeLocation().toVector().subtract(hammer.getLocation().toVector()).multiply(0.2).add(new Vector(0, 0, 0)));
            v = null;
            getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
                if (hammer == null) return;
                pickupItem();
            }, 40);
        }, 20);
    }

    @EventHandler
    public void onHammerPickup(EntityPickupItemEvent event) {
        if (hammer != event.getItem()) return;
        event.setCancelled(true);
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Player player = getPlayer();
        if (event.getEntity() != player) {
            if (v != null && canAffect(event.getEntity(), player)) {
                MathUtils.applyVelocity(event.getEntity(), v);
            }
            return;
        }

        if (hammer.getTicksLived() <= 5) return;

        pickupItem();
    }

    private void pickupItem() {
        equipItem();
        hammer.remove();
        hammer = null;
    }

    @EventHandler
    public void onDamEnt(EntityDamageByEntityEvent event) {
        if (getOwner() != null
                && getPlayer() != null
                && event.getDamager() == getPlayer()
                && getItemStack().equals(getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onClear() {
        if (hammer != null) {
            hammer.remove();
            hammer = null;
        }
        v = null;
    }
}
