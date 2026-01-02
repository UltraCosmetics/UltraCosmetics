package be.isach.ultracosmetics.cosmetics.deatheffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.DeathEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Effect;

public class DeathEffectDragonBreath extends DeathEffect {

    public DeathEffectDragonBreath(UltraPlayer owner, DeathEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void displayParticles() {
        Player player = getPlayer();
        player.getWorld().playEffect(player.getLocation(), Effect.DRAGON_BREATH, null);
    }
}
