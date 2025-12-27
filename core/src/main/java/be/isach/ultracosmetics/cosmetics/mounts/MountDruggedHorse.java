package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.potion.PotionEffect;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sacha on 10/08/15.
 */
public class MountDruggedHorse extends MountAbstractHorse {
    private final ParticleDisplay fireworkDisplay = ParticleDisplay.of(XParticle.FIREWORK).offset(0.4, 0.2, 0.4).withCount(5);
    private final ParticleDisplay effectDisplay = fireworkDisplay.copy().withParticle(XParticle.EFFECT);
    private final ParticleDisplay ambientEffectDisplay = effectDisplay.copy().withParticle(XParticle.ENTITY_EFFECT);
    private final ParticleDisplay coloredEffectDisplay = effectDisplay.copy().withColor(new Color(5, 255, 0));

    public MountDruggedHorse(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        super.setupEntity();
        Horse horse = (Horse) getEntity();
        horse.setColor(Horse.Color.CHESTNUT);
        horse.setJumpStrength(1.3);

        getUltraCosmetics().getScheduler().runAtEntity(getPlayer(), (task) -> {
            getPlayer().addPotionEffect(new PotionEffect(XPotion.NAUSEA.getPotionEffectType(), 10000000, 1));
        });
    }

    @Override
    public void onUpdate() {
        Location loc = entity.getLocation().add(0, 1, 0);
        fireworkDisplay.spawn(loc);
        effectDisplay.spawn(loc);
        coloredEffectDisplay.spawn(loc);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        ambientEffectDisplay.withColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256), 100)).spawn(loc);
    }

    @Override
    protected void onClear() {
        getPlayer().removePotionEffect(XPotion.NAUSEA.getPotionEffectType());
        super.onClear();
    }
}
