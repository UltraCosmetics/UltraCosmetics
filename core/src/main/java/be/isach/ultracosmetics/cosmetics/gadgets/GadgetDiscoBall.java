package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.Area;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents an instance of a discoball gadget summoned by a player.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class GadgetDiscoBall extends Gadget implements PlayerAffectingCosmetic, Updatable {

    private double i = 0;
    private ArmorStand armorStand;
    private boolean running = false;
    private final ParticleDisplay effect = ParticleDisplay.of(XParticle.EFFECT).withLocationCaller(() -> armorStand.getEyeLocation());
    private final ParticleDisplay instantEffect = ParticleDisplay.of(XParticle.INSTANT_EFFECT).withLocationCaller(() -> armorStand.getEyeLocation());
    private final ParticleDisplay note = ParticleDisplay.of(XParticle.NOTE).withCount(0);
    private final ParticleDisplay dust = ParticleDisplay.of(XParticle.DUST);

    public GadgetDiscoBall(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        armorStand = (ArmorStand) getPlayer().getWorld().spawnEntity(getPlayer().getLocation().add(0, 3, 0), EntityType.ARMOR_STAND);
        armorStand.setMetadata("NO_INTER", new FixedMetadataValue(getUltraCosmetics(), ""));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setSmall(false);
        armorStand.setPersistent(false);
        armorStand.getEquipment().setHelmet(ItemFactory.create(XMaterial.LIGHT_BLUE_STAINED_GLASS, " "));
        running = true;
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), this::clean, 400);
    }

    @Override
    protected boolean checkRequirements(PlayerInteractEvent event) {
        Area area = new Area(getPlayer().getLocation(), 0, 4);
        if (!area.isEmpty()) {
            MessageManager.send(getPlayer(), "Gadgets.DiscoBall.Not-Space-Above");
            return false;
        }
        return true;
    }

    @Override
    public void onUpdate() {
        if (armorStand == null) {
            return;
        }
        if (!armorStand.isValid() || !running) {
            i = 0;
            clean();
            return;
        }
        armorStand.setHeadPose(armorStand.getHeadPose().add(0, 0.2, 0));

        armorStand.getEquipment().setHelmet(ItemFactory.getRandomStainedGlass());

        effect.spawn();
        instantEffect.spawn();
        Location loc = armorStand.getEyeLocation().add(MathUtils.randomDouble(-4, 4), MathUtils.randomDouble(-3, 3), MathUtils.randomDouble(-4, 4));
        note.withNoteColor(ThreadLocalRandom.current().nextInt(25)).spawn(loc);
        double angle, x, z;

        angle = 2 * Math.PI * i / 100;
        x = Math.cos(angle) * 4;
        z = Math.sin(angle) * 4;
        drawParticleLine(armorStand.getEyeLocation(), armorStand.getEyeLocation().add(x, 0, z), 50);
        i += 0.4;

        XTag<XMaterial> tag = null;
        Map<Block, XMaterial> updates = new HashMap<>();
        for (Block b : BlockUtils.getBlocksInRadius(armorStand.getEyeLocation().add(-.5d, -.5d, -.5d), 10, false)) {
            XMaterial mat = XMaterial.matchXMaterial(b.getType());
            if (XTag.WOOL.isTagged(mat)) {
                tag = XTag.WOOL;
            } else if (XTag.CARPETS.isTagged(mat)) {
                tag = XTag.CARPETS;
            }

            if (tag != null) {
                updates.put(b, ItemFactory.randomFromTag(tag));
                tag = null;
            }
        }

        BlockUtils.setToRestore(updates, 4);

        Player player = getPlayer();
        for (Entity ent : loc.getWorld().getNearbyEntities(armorStand.getEyeLocation().add(-.5d, -.5d, -.5d), 7.5, 7.5, 7.5)) {
            if (ent.isOnGround() && canAffect(ent, player)) {
                MathUtils.applyVelocity(ent, new Vector(0, 0.3, 0));
            }
        }
    }

    private void clean() {
        running = false;
        if (armorStand != null) {
            armorStand.remove();
            armorStand = null;
        }
    }

    @Override
    public void onClear() {
        clean();
    }

    public void drawParticleLine(Location a, Location b, int particles) {
        Location location = a.clone();
        Location target = b.clone();
        Vector link = target.toVector().subtract(location.toVector());
        float length = (float) link.length();
        link.normalize();

        float ratio = length / particles;
        Vector v = link.multiply(ratio);
        MathUtils.rotateAroundAxisX(v, i / 5);
        MathUtils.rotateAroundAxisZ(v, i / 5);
        Location loc = location.clone().subtract(v);
        for (int i = 0; i < particles; i++) {
            loc.add(v);
            dust.withColor(new Color(MathUtils.random(255), MathUtils.random(255), MathUtils.random(255))).spawn(loc);
        }
    }
}
