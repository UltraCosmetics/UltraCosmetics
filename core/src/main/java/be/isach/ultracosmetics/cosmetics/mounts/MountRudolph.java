package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.PlayerUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.awt.Color;

/**
 * Created by sacha on 1/03/17.
 */
public class MountRudolph extends MountAbstractHorse {
    private static final ItemStack DEAD_BUSH = XMaterial.DEAD_BUSH.parseItem();
    private final ParticleDisplay nose = ParticleDisplay.of(XParticle.DUST).withColor(Color.RED);
    private ArmorStand left, right;

    public MountRudolph(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void setupEntity() {
        super.setupEntity();
        left = spawnArmorStand(false);
        right = spawnArmorStand(true);
        moveAntlers();
    }

    private ArmorStand spawnArmorStand(boolean right) {
        ArmorStand armorStand = getEntity().getWorld().spawn(getEyeLocation(), ArmorStand.class);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setArms(true);
        armorStand.setVisible(false);
        if (!right) {
            armorStand.setRightArmPose(new EulerAngle(Math.PI, Math.PI / 4, -Math.PI / 4));
        } else {
            armorStand.setRightArmPose(new EulerAngle(Math.PI, -Math.PI / 4, Math.PI / 4));
        }
        armorStand.getEquipment().setItemInMainHand(DEAD_BUSH);
        armorStand.setPersistent(false);
        armorStand.setMetadata("C_AD_ArmorStand", new FixedMetadataValue(getUltraCosmetics(), getPlayer().getUniqueId().toString()));
        return armorStand;
    }

    @Override
    public void onUpdate() {
        if (left != null && right != null) moveAntlers();
    }

    private void moveAntlers() {
        Location location = getEyeLocation();

        double radians = Math.toRadians(location.getYaw());
        Vector leftVector = new Vector(Math.cos(radians), 0, Math.sin(radians)).multiply(0.5);
        Vector rightVector = leftVector.clone().multiply(0.4);
        leftVector.multiply(1.6);

        location.add(PlayerUtils.getHorizontalDirection(getPlayer(), 0.75)).subtract(0, 1.7, 0);

        getUltraCosmetics().getScheduler().teleportAsync(left, location.clone().add(leftVector));
        getUltraCosmetics().getScheduler().teleportAsync(right, location.clone().add(rightVector));

        Location noseLocation = getEyeLocation();
        double y = noseLocation.getY();
        noseLocation.add(noseLocation.getDirection().multiply(1.15));
        noseLocation.setY(y + 0.127);
        nose.spawn(noseLocation);
        // Improves update time for antlers, but not a critical feature
        if (!UltraCosmeticsData.get().getVersionManager().isUsingNMS()) return;
        new Thread(() -> {
            for (Player player : getPlayer().getWorld().getPlayers()) {
                if (noseLocation.distanceSquared(player.getLocation()) > 32 * 32) continue;
                UltraCosmeticsData.get().getVersionManager().getEntityUtil().sendTeleportPacket(player, right);
                UltraCosmeticsData.get().getVersionManager().getEntityUtil().sendTeleportPacket(player, left);
            }
        }).start();
    }

    @Override
    public void onClear() {
        super.onClear();

        if (left != null) left.remove();
        if (right != null) right.remove();
    }

    private Location getEyeLocation() {
        return ((LivingEntity) entity).getEyeLocation();
    }
}
