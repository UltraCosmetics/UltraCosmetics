package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a quake gun gadget summoned by a player.
 *
 * @author iSach
 * @since 10-12-2015
 */
public class GadgetQuakeGun extends Gadget implements PlayerAffectingCosmetic {
    private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder().flicker(false).trail(false)
            .with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withFade(Color.ORANGE).build();
    private static final EntityType FIREWORK_ENTITY = XEntityType.FIREWORK_ROCKET.get();
    private static final ParticleDisplay FLAME = ParticleDisplay.of(XParticle.FLAME).withCount(60).withExtra(0.4f);

    private final List<Firework> fireworkList = new ArrayList<>();
    private final XSound.SoundPlayer sound;

    public GadgetQuakeGun(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        sound = XSound.ENTITY_BLAZE_DEATH.record().withVolume(1.4f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
    }

    @Override
    protected void onRightClick() {
        sound.play();

        Location location = getPlayer().getEyeLocation().subtract(0, 0.4, 0);
        Vector vector = location.getDirection();

        for (int i = 0; i < 20; i++) {
            Firework firework = (Firework) location.getWorld().spawnEntity(location, FIREWORK_ENTITY);
            firework.setMetadata("uc_firework", new FixedMetadataValue(UltraCosmeticsData.get().getPlugin(), true));
            location.add(vector);
            fireworkList.add(firework);

            List<Entity> nearbyEntities = firework.getNearbyEntities(0.5d, 0.5d, 0.5d);

            Player player = getPlayer();
            for (Entity entity : nearbyEntities) {
                if ((entity instanceof Player || entity instanceof Creature)
                        && entity != player && canAffect(entity, player)) {
                    MathUtils.applyVelocity(entity, new Vector(0, 1, 0));
                    FLAME.spawn(entity.getLocation());
                    UltraCosmeticsData.get().getVersionManager().getModule().spawnFirework(location, FIREWORK_EFFECT);
                }
            }
        }
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
            for (Firework firework : fireworkList) {
                firework.remove();
            }
            fireworkList.clear();
        }, 6);
    }
}
