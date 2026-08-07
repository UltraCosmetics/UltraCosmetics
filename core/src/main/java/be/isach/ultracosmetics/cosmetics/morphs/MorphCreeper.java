package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MorphCreeper extends Morph implements PlayerAffectingCosmetic, Updatable {
    private int charge = 0;
    private final XSound.SoundPlayer chargeSound;
    private final XSound.SoundPlayer explodeSound;
    private final ParticleDisplay display;

    public MorphCreeper(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        chargeSound = XSound.ENTITY_CREEPER_PRIMED.record().withVolume(1.4f).withPitch(1.5f).soundPlayer()
                .forPlayers(getPlayer());
        explodeSound = XSound.ENTITY_GENERIC_EXPLODE.record().withVolume(1.4f).withPitch(1.5f).soundPlayer()
                .forPlayers(getPlayer());
        display = ParticleDisplay.of(XParticle.EXPLOSION_EMITTER).withEntity(getPlayer());
    }

    @Override
    public void onUpdate() {
        if (!canUseSkill) {
            return;
        }
        CreeperWatcher creeperWatcher = (CreeperWatcher) disguise.getWatcher();
        if (getPlayer().isSneaking()) {
            creeperWatcher.setIgnited(true);
            if (charge + 4 <= 100) {
                charge += 4;
            }
            chargeSound.play();
        } else {
            if (creeperWatcher.isIgnited()) {
                // Reset disguise
                onEquip();
            }
            if (charge == 100) {
                display.spawn();
                explodeSound.play();

                Player player = getPlayer();
                for (Entity ent : player.getNearbyEntities(3, 3, 3)) {
                    if (!canAffect(ent, player)) {
                        continue;
                    }
                    Vector vector = getVector(ent);
                    MathUtils.applyVelocity(ent, vector.multiply(1.3D).add(new Vector(0, 1.4D, 0)));
                }
                ActionBar.clearActionBar(getPlayer());
                charge = 0;
                return;
            }
            if (charge > 0) {
                charge -= 4;
            }
        }
        if (charge > 0 && charge < 100) {
            if (charge < 5) {
                ActionBar.clearActionBar(getPlayer());
            } else {
                ActionBar.sendActionBar(getPlayer(), MessageManager.getLegacyMessage("Morphs.Creeper.charging",
                        Placeholder.unparsed("chargelevel", String.valueOf(charge)))
                );
            }
        } else if (charge == 100) {
            ActionBar.sendActionBar(getPlayer(), MessageManager.getLegacyMessage("Morphs.Creeper.release-to-explode"));
        }
    }

    @NotNull
    private Vector getVector(Entity ent) {
        double dX = getPlayer().getLocation().getX() - ent.getLocation().getX();
        double dY = getPlayer().getLocation().getY() - ent.getLocation().getY();
        double dZ = getPlayer().getLocation().getZ() - ent.getLocation().getZ();
        double yaw = Math.atan2(dZ, dX);
        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);

        return new Vector(x, z, y);
    }
}
