package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.ParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleEffectCrushedCandyCane extends ParticleEffect {
    private static final Particle ITEM_PARTICLE = XParticle.ITEM.get();
    private int step;

    public ParticleEffectCrushedCandyCane(UltraPlayer owner, ParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.useAlternativeEffect = true;
    }

    @Override
    public void onUpdate() {
        if (step > 360) {
            step = 0;
        }
        Location center = getPlayer().getEyeLocation().add(0, 0.6, 0);
        double inc = (2 * Math.PI) / 20;
        double angle = step * inc;
        double x = Math.cos(angle) * 1.1f;
        double z = Math.sin(angle) * 1.1f;
        center.add(x, 0, z);
        showParticlesAt(center);
        step++;
    }

    private void showParticlesAt(Location loc) {
        for (int i = 0; i < getModifiedAmount(15); i++) {
            getPlayer().getLocation().getWorld()
                    .spawnParticle(ITEM_PARTICLE, loc, 1, 0.2, 0.2, 0.2, 0, ItemFactory.getRandomDye());
        }
    }

    @Override
    public void showAlternativeEffect() {
        showParticlesAt(getPlayer().getLocation().add(0, 0.2, 0));
    }

    public static byte getRandomColor() {
        int random = RANDOM.nextInt(100);
        if (random > 98) {
            return (byte) 2;
        } else if (random > 49) {
            return (byte) 1;
        } else {
            return (byte) 15;
        }
    }
}
