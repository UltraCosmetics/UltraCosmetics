package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MountNyanSheep extends Mount {
    private static final List<Color> COLORS = new ArrayList<>();
    private EntityBrain brain;

    static {
        COLORS.add(new Color(255, 0, 0));
        COLORS.add(new Color(255, 165, 0));
        COLORS.add(new Color(255, 255, 0));
        COLORS.add(new Color(154, 205, 50));
        COLORS.add(new Color(30, 144, 255));
        COLORS.add(new Color(148, 0, 211));
    }

    private final ParticleDisplay display = ParticleDisplay.of(XParticle.DUST).withCount(10);

    public MountNyanSheep(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        Sheep sheep = (Sheep) entity;
        sheep.setNoDamageTicks(Integer.MAX_VALUE);
        brain = BukkitBrain.getBrain(sheep);
        brain.getGoalAI().clear();
        brain.getTargetAI().clear();
    }

    @Override
    public void onUpdate() {
        move();

        ((Sheep) entity).setColor(DyeColor.values()[RANDOM.nextInt(16)]);

        Location particleLoc =
                entity.getLocation().add(entity.getLocation().getDirection().normalize().multiply(-2)).add(0, 1.2, 0);
        for (Color rgbColor : COLORS) {
            display.withColor(rgbColor).spawn(particleLoc);
            particleLoc.subtract(0, 0.2, 0);
        }
    }

    private void move() {
        Location target = getPlayer().getLocation();
        Vector vel = target.getDirection().setY(0).normalize().multiply(4);
        target.add(vel);

        brain.getController().moveTo(target, 2);
    }
}
