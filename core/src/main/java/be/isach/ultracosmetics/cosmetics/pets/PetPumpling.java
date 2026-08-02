package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

/**
 * @author RadBuilder
 */
public class PetPumpling extends Pet {
    public PetPumpling(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        Zombie zombie = (Zombie) getEntity();
        zombie.setBaby();
        zombie.getEquipment().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
        zombie.setSilent(true);
    }

    @Override
    public boolean useArmorStandNameTag() {
        // Entity is invisible so its name tag doesn't show
        return true;
    }

    @Override
    public void onUpdate() {
        // Is this necesssary?
        entity.setFireTicks(0);
        Location loc = ((Zombie) getEntity()).getEyeLocation();
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.2, 0.2, 0.2);
        // this doesn't seem to work when just in setupEntity()
        entity.setInvisible(true);
    }
}
