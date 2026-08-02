package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GadgetFreezeCannon extends Gadget implements Updatable {
    private static final ItemStack ICE = XMaterial.ICE.parseItem();
    private static final ParticleDisplay FIREWORK =
            ParticleDisplay.of(XParticle.FIREWORK).offset(4, 3, 4).withCount(80);
    private final Set<Item> items = new HashSet<>();

    public GadgetFreezeCannon(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        Item item = getPlayer().getWorld().dropItem(getPlayer().getEyeLocation(), ICE);
        item.setVelocity(getPlayer().getEyeLocation().getDirection().multiply(0.9));
        items.add(item);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (items.contains(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onUpdate() {
        Iterator<Item> iter = items.iterator();
        Map<Block, XMaterial> updates = new HashMap<>();
        while (iter.hasNext()) {
            Item item = iter.next();
            if (item.isOnGround()) {
                for (Block b : BlockUtils.getBlocksInRadius(item.getLocation(), 4, false)) {
                    updates.put(b, XMaterial.PACKED_ICE);
                }
                FIREWORK.spawn(item.getLocation());
                item.remove();
                iter.remove();
            }
        }
        BlockUtils.setToRestore(updates, 50);
    }

    @Override
    public void onClear() {
        for (Item item : items) {
            item.remove();
        }
        items.clear();
    }
}
