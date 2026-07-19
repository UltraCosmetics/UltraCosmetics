package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.MathUtils;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a snake mount.
 *
 * @author iSach
 * @since 08-10-2015
 */
public class MountSnake extends Mount {
    private final List<Creature> tail = new ArrayList<>();
    private int color;

    public MountSnake(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    private void setupSheep(Entity entity) {
        Sheep sheep = (Sheep) entity;
        sheep.setNoDamageTicks(Integer.MAX_VALUE);
        sheep.setColor(DyeColor.values()[color]);
        sheep.setPersistent(false);
        sheep.setRemoveWhenFarAway(false);
        AttributeInstance speed = sheep.getAttribute(Attribute.MOVEMENT_SPEED);
        // Make first sheep slower so the others can catch up
        double speedVal = tail.isEmpty() ? 0.8 : 1.5;
        speed.addModifier(
                ItemFactory.createAttributeModifier("snake_speed", speedVal, AttributeModifier.Operation.ADD_SCALAR,
                        null));
        tail.add(sheep);
    }

    @Override
    public void setupEntity() {
        color = MathUtils.randomRangeInt(0, 14);
        setupSheep(entity);
        addSheepToTail(4);
    }

    @Override
    public void onClear() {
        super.onClear();
        for (Entity ent : tail) {
            ent.remove();
        }
        tail.clear();
    }

    @Override
    public void onUpdate() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        World world = player.getWorld();
        Vector vel = player.getLocation().getDirection().setY(0).normalize().multiply(16);

        Creature before = null;
        for (Creature tailEnt : tail) {
            if (!tailEnt.isValid() || tailEnt.getWorld() != world) {
                clear();
                return;
            }
            Location loc;
            if (before == null) {
                loc = tailEnt.getLocation().add(vel);
            } else {
                loc = before.getLocation();
                if (before.getLocation().distanceSquared(tailEnt.getLocation()) > 6 * 6) {
                    Location tp = before.getLocation().add(traj(before, tailEnt).multiply(1.4D));
                    tp.setPitch(tailEnt.getLocation().getPitch());
                    tp.setYaw(tailEnt.getLocation().getYaw());
                    getUltraCosmetics().getScheduler().teleportAsync(tailEnt, tp);
                }
            }

            BukkitBrain.getBrain(tailEnt).getController().moveTo(loc);

            before = tailEnt;
        }
    }

    public Vector traj(Location a, Location b) {
        return b.toVector().subtract(a.toVector()).setY(0).normalize();
    }

    public Vector traj(Entity a, Entity b) {
        return traj(a.getLocation(), b.getLocation());
    }

    public void addSheepToTail(int amount) {
        Player player = getPlayer();
        for (int i = 0; i < amount; i++) {
            Location loc = player.getLocation();
            if (!tail.isEmpty()) {
                loc = lastTail().getLocation();
            }
            if (tail.size() > 1) {
                loc.add(traj(tail.get(tail.size() - 2), lastTail()));
            } else {
                loc.subtract(player.getLocation().getDirection().setY(0));
            }
            setupSheep(loc.getWorld().spawn(loc, Sheep.class));
        }
    }

    @EventHandler
    public void onSnakeDamage(EntityDamageEvent event) {
        if (tail.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private Creature lastTail() {
        return tail.get(tail.size() - 1);
    }
}
