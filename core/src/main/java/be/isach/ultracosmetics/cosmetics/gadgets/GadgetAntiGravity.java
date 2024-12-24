package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents an instance of an antigravity gadget summoned by a player.
 *
 * @author iSach
 * @since 08-10-2015
 */
public class GadgetAntiGravity extends Gadget implements PlayerAffectingCosmetic, Updatable {
    private static final ItemStack SEA_LANTERN = XMaterial.SEA_LANTERN.parseItem();
    private ArmorStand as;
    private boolean running;
    private ParticleDisplay portalParticles;
    private ParticleDisplay witchParticles;

    public GadgetAntiGravity(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        as = getPlayer().getWorld().spawn(getPlayer().getLocation(), ArmorStand.class);
        as.setMetadata("NO_INTER", new FixedMetadataValue(getUltraCosmetics(), ""));
        as.setPersistent(false);
        as.setGravity(false);
        as.setSmall(true);
        running = true;
        as.setVisible(false);
        as.getEquipment().setHelmet(SEA_LANTERN);

        portalParticles = ParticleDisplay.of(XParticle.PORTAL).offset(3, 3, 3).withCount(150).withLocation(as.getLocation());
        witchParticles = ParticleDisplay.of(XParticle.WITCH).offset(0.3, 0.3, 0.3).withCount(5).withLocation(as.getEyeLocation());

        getUltraCosmetics().getScheduler().runLaterAsync(() -> running = false, 240);
    }

    @Override
    public void onUpdate() {
        if (as == null || !as.isValid()) return;
        if (!running) {
            as.remove();
            as = null;
            return;
        }

        as.setHeadPose(as.getHeadPose().add(0, 0.1, 0));
        portalParticles.spawn();
        witchParticles.spawn();
        for (Entity ent : as.getNearbyEntities(3, 2, 3)) {
            if (canAffect(ent, getPlayer())) {
                LivingEntity le = (LivingEntity) ent;
                le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 5, 0));
            }
        }
    }

    @Override
    public void onClear() {
        if (as != null) {
            as.remove();
        }
        running = false;
    }
}
