package be.isach.ultracosmetics.v1_21_R7.customentities;

import be.isach.ultracosmetics.v1_21_R7.EntityBase;
import be.isach.ultracosmetics.v1_21_R7.nms.EntityWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;

import java.util.HashSet;
import java.util.Set;

/**
 * @author RadBuilder
 */
public class CustomEntities {
    private static final Set<Entity> customEntities = new HashSet<>();

    public static org.bukkit.entity.Entity spawnEntity(Entity entity, Location loc) {
        entity.snapTo(loc.getX(), loc.getY() + 2, loc.getZ(), 0, 0);
        ((CraftWorld) loc.getWorld()).getHandle().addFreshEntity(entity);
        addCustomEntity(entity);
        return entity.getBukkitEntity();
    }

    public static void ride(float sideMot, float forMot, Player passenger, Mob mob) {
        if (!(mob instanceof EntityBase)) {
            throw new IllegalArgumentException("The entity parameter should implement EntityBase");
        }

        EntityBase entityBase = (EntityBase) mob;
        // Upcasting required because otherwise SpecialSource doesn't pick it up
        //noinspection UnnecessaryLocalVariable
        Entity entity = mob;

        EntityWrapper wEntity = new EntityWrapper(mob);
        EntityWrapper wPassenger = new EntityWrapper(passenger);

        if (passenger == null) {
            wEntity.setStepHeight(0.5f);
            wEntity.setJumpMovementFactor(0.02f);

            entityBase.travel_(sideMot, forMot);
            return;
        }

        entity.yRotO = ((Entity) passenger).getYRot() % 360f;
        entity.setYRot(entity.yRotO);
        entity.setXRot((((Entity) passenger).getXRot() * 0.5F) % 360f);

        wEntity.setRenderYawOffset(entity.getYRot());
        wEntity.setRotationYawHead(entity.getYRot());

        Input intent = wPassenger.getInputIntent();
        sideMot = 0;
        forMot = 0;
        if (intent.left()) {
            sideMot = 0.25f;
        } else if (intent.right()) {
            sideMot = -0.25f;
        }
        if (intent.forward()) {
            forMot = 0.5f;
        } else if (intent.backward()) {
            forMot = -0.5f;
        }

        if (forMot < 0.0f) {
            forMot *= 0.25f;
        }

        wEntity.setJumping(intent.jump());

        if (intent.jump() && entity.onGround()) {
            Vec3 v = entity.getDeltaMovement();
            Vec3 v2 = new Vec3(v.x(), 0.4D, v.z());
            entity.setDeltaMovement(v2);
        }

        wEntity.setStepHeight(1.0f);
        wEntity.setJumpMovementFactor(wEntity.getMoveSpeed() * 0.1f);

        wEntity.setRotationYawHead(entity.getYRot());

        entityBase.travel_(sideMot, forMot);

        //wEntity.setPrevLimbSwingAmount(wEntity.getLimbSwingAmount());

        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;

        float f4 = Mth.sqrt((float) (dx * dx + dz * dz)) * 4;

        if (f4 > 1) f4 = 1;

        wEntity.setLimbSwingAmount(wEntity.getLimbSwingAmount() + (f4 - wEntity.getLimbSwingAmount()) * 0.4f);
        wEntity.setLimbSwing(wEntity.getLimbSwing() + wEntity.getLimbSwingAmount());
    }

    public static void addCustomEntity(Entity entity) {
        customEntities.add(entity);
    }

    public static boolean isCustomEntity(Entity entity) {
        return customEntities.contains(entity);
    }

    public static void removeCustomEntity(Entity entity) {
        entity.discard();
        customEntities.remove(entity);
    }

    public static Component toComponent(String str) {
        return Component.literal(str);
    }
}
