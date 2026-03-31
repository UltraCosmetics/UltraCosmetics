package be.isach.ultracosmetics.nms.mount;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.nms.customentities.RideableSpider;
import be.isach.ultracosmetics.player.UltraPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.CraftWorld;

/**
 * @author RadBuilder
 */
public class MountSpider extends MountCustomEntity {
    public MountSpider(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public LivingEntity getNewEntity() {
        return new RideableSpider(EntityType.SPIDER, ((CraftWorld) getPlayer().getWorld()).getHandle());
    }
}
