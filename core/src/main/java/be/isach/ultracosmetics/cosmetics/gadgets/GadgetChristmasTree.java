package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.awt.*;

/**
 * Represents an instance of a Christmas Tree gadget summoned by a player.
 *
 * @author iSach
 * @since 11-29-2015
 */
public class GadgetChristmasTree extends Gadget implements Updatable {

    private static final Color LOG_COLOR = new Color(101, 67, 33);
    private static final Color STAR_COLOR = new Color(255, 255, 0);
    private static final Color LEAF_COLOR = new Color(0, 100, 0);
    private boolean active = false;
    private Location lastLocation;
    private static final ParticleDisplay LOG = ParticleDisplay.of(XParticle.DUST).withColor(LOG_COLOR);
    private static final ParticleDisplay SNOW = ParticleDisplay.of(XParticle.FIREWORK).offset(4, 3, 4).withCount(10);
    private static final ParticleDisplay STAR = ParticleDisplay.of(XParticle.DUST).withColor(STAR_COLOR);
    private static final ParticleDisplay LEAF = ParticleDisplay.of(XParticle.DUST).withColor(LEAF_COLOR);

    public GadgetChristmasTree(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void onRightClick() {
        lastLocation = lastClickedBlock.getLocation().add(0.5d, 1.05d, 0.5d);
        active = true;
        getUltraCosmetics().getScheduler().runLaterAsync(() -> active = false, 200);
    }

    @Override
    public void onUpdate() {
        if (active) {
            drawLog();
            drawLeavesAndBalls();
            drawStar();
            drawSnow();
        }
    }

    private void drawSnow() {
        SNOW.spawn(lastLocation.clone().add(0, 3, 0));
    }

    @Override
    protected boolean checkRequirements(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            MessageManager.send(getPlayer(), "Gadgets.ChristmasTree.Click-On-Block");
            return false;
        }
        return true;
    }

    private void drawLog() {
        Location to = lastLocation.clone().add(0, 2.5, 0);
        Particles.line(lastLocation, to, 0.25, LOG.clone());
    }

    private void drawLeavesAndBalls() {
        float radius = 0.7f;
        for (float y = 0.8f; y <= 2.5f; y += 0.2f) {
            float steps = 13f / y;
            float inc = (float) ((2 * Math.PI) / steps);
            if (RANDOM.nextInt(2) == 1) {
                float angle = MathUtils.random(steps) * inc;
                float x = MathUtils.cos(angle) * (radius + 0.05f);
                float z = MathUtils.sin(angle) * (radius + 0.05f);
                LEAF.clone().withColor(new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                        .spawn(lastLocation.clone().add(x, y, z));
            }
            for (int i = 0; i < steps; i++) {
                float angle = i * inc;
                float x = MathUtils.cos(angle) * radius;
                float z = MathUtils.sin(angle) * radius;
                LEAF.spawn(lastLocation.clone().add(x, y, z));
            }
            radius -= 0.7f / 8.5f;
        }
    }

    private void drawStar() {
        STAR.spawn(lastLocation.clone().add(0, 2.6, 0));
    }

    @Override
    public void onClear() {
        active = false;
        lastLocation = null;
    }
}
