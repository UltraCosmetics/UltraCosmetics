package be.isach.ultracosmetics.nms.mount;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.nms.customentities.CustomSlime;
import be.isach.ultracosmetics.player.UltraPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

/**
 * @author RadBuilder
 */
public class MountSlime extends MountCustomEntity {

    public MountSlime(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public LivingEntity getNewEntity() {
        return new CustomSlime(EntityType.SLIME, ((CraftPlayer) getPlayer()).getHandle().level());
    }
}
