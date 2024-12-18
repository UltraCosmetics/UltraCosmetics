package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

/**
 * Represents an instance of a polar bear morph summoned by a player.
 *
 * @author RadBuilder
 * @since 07-03-2017
 */
public class MorphPolarBear extends MorphLeftClickCooldown implements Updatable {
    private final ParticleDisplay display = ParticleDisplay.of(XParticle.CLOUD).withCount(6).offset(0.3, 0.1, 0.3).withExtra(0.4);
    private boolean active;
    private Location location;
    private Vector vector;

    public MorphPolarBear(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics, 15);
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {
        event.setCancelled(true);
        vector = getPlayer().getLocation().getDirection().normalize().multiply(0.3).setY(-1);
        location = getPlayer().getLocation().add(vector);
        active = true;
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> active = false, 40);
    }

    @Override
    public void onUpdate() {
        if (!canUseSkill) return;
        if (!active) {
            location = null;
            vector = null;
            return;
        }
        if (location.getBlock().getType().isSolid()) {
            location.add(0, 1, 0);
        }

        if (BlockUtils.isAir(location.clone().subtract(0, 1, 0).getBlock().getType())) {
            if (!location.clone().getBlock().getType().toString().contains("SLAB")) location.add(0, -1, 0);
        }

        for (int i = 0; i < 3; i++) {
            UltraCosmeticsData.get().getVersionManager().getEntityUtil().sendBlizzard(getPlayer(), location, ent -> false, vector);
        }
        display.spawn(location.clone().subtract(0, 0.5, 0));

        location.add(vector);
    }

    @Override
    protected void onClear() {
        if (!canUseSkill) return;
        active = false;
        if (getOwner() != null && getPlayer() != null) {
            UltraCosmeticsData.get().getVersionManager().getEntityUtil().clearBlizzard(getPlayer());
        }
    }
}
