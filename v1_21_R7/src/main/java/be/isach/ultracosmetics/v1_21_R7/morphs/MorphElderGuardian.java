package be.isach.ultracosmetics.v1_21_R7.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.morphs.Morph;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.task.UltraTask;
import be.isach.ultracosmetics.util.EntitySpawningManager;
import be.isach.ultracosmetics.util.MathUtils;
import be.isach.ultracosmetics.v1_21_R7.VersionModule;
import be.isach.ultracosmetics.v1_21_R7.customentities.CustomEntities;
import be.isach.ultracosmetics.v1_21_R7.customentities.CustomGuardian;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

/**
 * @author RadBuilder
 */
public class MorphElderGuardian extends Morph implements Updatable {

    private boolean cooldown;
    private CustomGuardian customGuardian;

    public MorphElderGuardian(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK) && !cooldown
                && event.getPlayer() == getPlayer()) {
            shootLaser();
            cooldown = true;
            UltraTask.runLater(() -> cooldown = false, 80);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (((CraftEntity) event.getDamager()).getHandle() == customGuardian
                && event.getEntity() == getPlayer()) {
            event.setCancelled(true);
        }
    }

    private void shootLaser() {
        if (customGuardian == null) return;

        final Location FROM = customGuardian.getBukkitEntity().getLocation();
        final Location TO = FROM.clone().add(getPlayer().getLocation().getDirection().multiply(10));

        final ArmorStand armorStand = getPlayer().getWorld().spawn(TO, ArmorStand.class);

        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);

        customGuardian.target(armorStand);

        UltraTask.runAtLocationLater(FROM, () -> {
            FireworkEffect.Builder builder = FireworkEffect.builder();
            FireworkEffect effect = builder.flicker(false).trail(false).with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.TEAL).withFade(Color.TEAL).build();

            VersionModule.spawnFirework_(TO, effect);

            Vector vector = TO.toVector().subtract(FROM.toVector());

            Location current = FROM.clone();

            for (int i = 0; i < 10; i++) {
                for (org.bukkit.entity.Entity entity : current.getWorld().getNearbyEntities(current, 4.5, 4.5, 4.5)) {
                    if (entity instanceof LivingEntity && entity != getPlayer()) {
                        MathUtils.applyVelocity(entity, new Vector(0, 0.5d, 0));
                    }
                }
                current.add(vector);
            }

            armorStand.remove();
            customGuardian.target(null);
        }, 25);
    }

    @Override
    public void onClear() {
        if (customGuardian == null) return;
        CustomEntities.removeCustomEntity(customGuardian);
    }

    @Override
    protected void onEquip() {
        super.onEquip();
        customGuardian = new CustomGuardian(EntityType.ELDER_GUARDIAN, ((CraftWorld) getPlayer().getWorld()).getHandle());
        EntitySpawningManager.withBypass(() -> CustomEntities.spawnEntity(customGuardian, getPlayer().getLocation()));
        getPlayer().addPassenger(customGuardian.getBukkitEntity());
        ((Entity) customGuardian).setInvisible(true);
    }

    @Override
    public void onUpdate() {
        if (getOwner() == null || getPlayer() == null) {
            cancelTask();
            return;
        }
        if (customGuardian == null || !customGuardian.isAlive()) {
            getOwner().removeCosmetic(Category.MORPHS);
            cancelTask();
        }
    }
}
