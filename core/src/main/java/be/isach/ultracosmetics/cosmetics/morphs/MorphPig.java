package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MorphPig extends Morph implements PlayerAffectingCosmetic, Updatable {
    private final XSound.SoundPlayer sound;

    public MorphPig(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        sound = XSound.ENTITY_PIG_AMBIENT.record().withVolume(0.2f).withPitch(1.5f).soundPlayer()
                .forPlayers(getPlayer());
    }

    @Override
    public void onUpdate() {
        if (!canUseSkill || getOwner().canUse(cosmeticType) || !isAffectingPlayersEnabled()) {
            return;
        }
        Player player = getPlayer();
        for (Entity ent : player.getNearbyEntities(0.2, 0.2, 0.2)) {
            if (!canAffect(ent, player)) {
                continue;
            }
            getOwner().setCooldown(cosmeticType, 1, 0);
            sound.play();
            Vector v = new Vector(0, 0.6, 0);
            Vector vEnt = ent.getLocation().toVector().subtract(player.getLocation().toVector()).add(v);
            Vector vPig = player.getLocation().toVector().subtract(ent.getLocation().toVector()).add(v);
            vEnt.setY(0.5);
            vPig.setY(0.5);
            MathUtils.applyVelocity(ent, vEnt.multiply(0.75));
            MathUtils.applyVelocity(player, vPig.multiply(0.75));
        }
    }
}
