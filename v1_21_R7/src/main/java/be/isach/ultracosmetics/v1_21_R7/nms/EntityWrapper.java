package be.isach.ultracosmetics.v1_21_R7.nms;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import org.bukkit.attribute.Attribute;

import java.lang.reflect.Field;

/**
 * @author RadBuilder
 */
public class EntityWrapper {

    protected LivingEntity handle;

    public EntityWrapper(LivingEntity handle) {
        this.handle = handle;
    }

    public <T> T getField(String name, Class<?> fieldClass, Class<T> clazz) {
        try {
            Field field = fieldClass.getDeclaredField(name);
            field.setAccessible(true);
            return clazz.cast(field.get(handle));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> void setField(String name, Class<?> fieldClass, T value) {
        try {
            Field field = fieldClass.getDeclaredField(name);
            field.setAccessible(true);
            field.set(handle, value);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public float getStepHeight() {
        return ((Entity) handle).maxUpStep();
    }

    public void setStepHeight(float stepHeight) {
        ((org.bukkit.entity.LivingEntity) handle.getBukkitEntity()).getAttribute(Attribute.STEP_HEIGHT).setBaseValue(stepHeight);
    }

    public float getRotationYawHead() {
        return handle.yHeadRot;
    }

    public void setRotationYawHead(float rotationYawHead) {
        handle.yHeadRot = rotationYawHead;
    }

    public float getRenderYawOffset() {
        return handle.yBodyRot;
    }

    public void setRenderYawOffset(float renderYawOffset) {
        handle.yBodyRot = renderYawOffset;
    }

    public float getMoveStrafing() {
        return handle.xxa;
    }

    public void setMoveStrafing(float moveStrafing) {
        handle.xxa = moveStrafing;
    }

    public float getMoveForward() {
        return handle.zza;
    }

    public void setMoveForward(float moveForward) {
        handle.zza = moveForward;
    }

    public Input getInputIntent() {
        return ((ServerPlayer) handle).getLastClientInput();
    }

    public void setJumping(boolean jumping) {
        // not sure why there's a setJumping method but no isJumping method
        handle.setJumping(jumping);
    }

    public float getJumpMovementFactor() {
        return handle.yBodyRotO;
    }

    public void setJumpMovementFactor(float jumpMovementFactor) {
        handle.yBodyRotO = jumpMovementFactor;
    }

    public float getLimbSwingAmount() {
        return handle.walkAnimation.speed();
    }

    public void setLimbSwingAmount(float limbSwingAmount) {
        handle.walkAnimation.setSpeed(limbSwingAmount);
    }

    public float getLimbSwing() {
        return handle.walkAnimation.position();
    }

    public void setLimbSwing(float limbSwing) {
        handle.walkAnimation.position(limbSwing);
    }

    public float getMoveSpeed() {
        return handle.getSpeed();
    }

    public void setMoveSpeed(float moveSpeed) {
        handle.setSpeed(moveSpeed);
    }

    public LivingEntity getHandle() {
        return handle;
    }
}
