package be.isach.ultracosmetics.nms.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.pets.Pet;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.nms.customentities.CustomEntities;
import be.isach.ultracosmetics.player.UltraPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Mob;

/**
 * @author RadBuilder
 */
public abstract class CustomEntityPet extends Pet {

    public CustomEntityPet(UltraPlayer owner, PetType petType, UltraCosmetics ultraCosmetics) {
        super(owner, petType, ultraCosmetics);
    }

    @Override
    public Mob spawnEntity() {
        entity = (Mob) CustomEntities.spawnEntity(getNewEntity(), getPlayer().getLocation());
        return entity;
    }

    @Override
    protected void removeEntity() {
        CustomEntities.removeCustomEntity(getNMSEntity());
    }

    @Override
    public boolean isCustomEntity() {
        return true;
    }

    public Entity getNMSEntity() {
        return ((CraftEntity) entity).getHandle();
    }

    public abstract Entity getNewEntity();
}
