package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a tsunami gadget summoned by a player.
 *
 * @author iSach
 * @since 08-08-2015
 */
public class GadgetTsunami extends Gadget implements PlayerAffectingCosmetic {
    private static final ParticleDisplay POOF = ParticleDisplay.of(XParticle.POOF).offset(0.2);
    private static final ParticleDisplay DRIPPING_WATER = ParticleDisplay.of(XParticle.DRIPPING_WATER).offset(0.4).withCount(2);
    private static final ParticleDisplay DUST = ParticleDisplay.of(XParticle.DUST).withColor(Color.BLUE);
    private final List<Entity> cooldownJump = new ArrayList<>();

    public GadgetTsunami(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        final Vector v = getPlayer().getLocation().getDirection().normalize().multiply(0.3);
        v.setY(0);
        final Location loc = getPlayer().getLocation().subtract(0, 1, 0).add(v);
        final WrappedTask i = getUltraCosmetics().getScheduler().runTimerAsync(() -> {
            if (loc.getBlock().getType().isSolid()) {
                loc.add(0, 1, 0);
            }
            if (BlockUtils.isAir(loc.clone().subtract(0, 1, 0).getBlock().getType())) {
                loc.add(0, -1, 0);
            }
            Location loc1 = loc.clone().add(MathUtils.randomDouble(-1.5, 1.5), MathUtils.randomDouble(0, .5) - 0.75, MathUtils.randomDouble(-1.5, 1.5));
            Location loc2 = loc.clone().add(MathUtils.randomDouble(-1.5, 1.5), MathUtils.randomDouble(1.3, 1.8) - 0.75, MathUtils.randomDouble(-1.5, 1.5));
            for (int i1 = 0; i1 < 5; i1++) {
                POOF.spawn(loc1);
                DRIPPING_WATER.spawn(loc2);
            }
            for (int a = 0; a < 100; a++) {
                // Can't use offsets for dust particles, they store their color data in the offset fields
                DUST.spawn(loc.clone().add(MathUtils.randomDouble(-1.5, 1.5), MathUtils.randomDouble(1, 1.6) - 0.75, MathUtils.randomDouble(-1.5, 1.5)));
            }
            if (isAffectingPlayersEnabled()) {
                getUltraCosmetics().getScheduler().runAtLocation(loc, (task) -> {
                    Player player = getPlayer();
                    for (final Entity ent : player.getWorld().getNearbyEntities(loc, 0.6, 0.6, 0.6)) {
                        if (!cooldownJump.contains(ent) && ent != player && canAffect(ent, player)) {
                            MathUtils.applyVelocity(ent, new Vector(0, 1, 0).add(v.clone().multiply(2)));
                            cooldownJump.add(ent);
                            getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> cooldownJump.remove(ent), 20);
                        }
                    }
                });
            }
            loc.add(v);
        }, 0, 1);
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> getUltraCosmetics().getScheduler().cancelTask(i), 40);
    }
}
