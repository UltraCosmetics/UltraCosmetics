package be.isach.ultracosmetics.v1_21_R7.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.v1_21_R7.customentities.Pumpling;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

/**
 * @author RadBuilder
 */
public class PetPumpling extends CustomEntityPet {
    public PetPumpling(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public LivingEntity getNewEntity() {
        return new Pumpling(EntityType.ZOMBIE, ((CraftPlayer) getPlayer()).getHandle().level());
    }

    @Override
    public void setupEntity() {
        // use API when we can
        Zombie zombie = (Zombie) getEntity();
        zombie.setBaby();
        zombie.getEquipment().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
    }

    @Override
    public void onUpdate() {
        // Is this necesssary?
        getNMSEntity().setRemainingFireTicks(0);
        Location loc = ((Zombie) getEntity()).getEyeLocation();
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.2, 0.2, 0.2);
        // this doesn't seem to work when just in setupEntity()
        getNMSEntity().setInvisible(true);
    }
}
