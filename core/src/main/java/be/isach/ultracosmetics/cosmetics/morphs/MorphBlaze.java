package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;

public class MorphBlaze extends MorphNoFall implements Updatable {
    private final XSound.SoundPlayer sound;
    private final ParticleDisplay flameDisplay;
    private final ParticleDisplay lavaDisplay;

    public MorphBlaze(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        sound = XSound.BLOCK_FIRE_EXTINGUISH.record().withVolume(0.1f).withPitch(1.5f).soundPlayer()
                .forPlayers(getPlayer());
        flameDisplay = ParticleDisplay.of(XParticle.FLAME).withEntity(getPlayer());
        lavaDisplay = ParticleDisplay.of(XParticle.LAVA).withEntity(getPlayer());
    }

    @Override
    public void onUpdate() {
        if (canUseSkill && getPlayer().isSneaking()) {
            flameDisplay.spawn();
            lavaDisplay.spawn();
            sound.play();
            getPlayer().setVelocity(getPlayer().getEyeLocation().getDirection().multiply(1));
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (canUseSkill && event.getPlayer() == getPlayer() && getOwner().getCurrentMorph() == this &&
                event.getReason().contains("Flying")) {
            event.setCancelled(true);
        }
    }
}
