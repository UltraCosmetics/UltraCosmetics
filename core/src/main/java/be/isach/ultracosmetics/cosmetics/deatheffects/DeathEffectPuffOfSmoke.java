package be.isach.ultracosmetics.cosmetics.deatheffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.DeathEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;

public class DeathEffectPuffOfSmoke extends DeathEffect {
    public DeathEffectPuffOfSmoke(UltraPlayer owner, DeathEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        display.withCount(75).offset(0.5, 0.5, 0.5).withLocationCaller(() -> targetLocation.clone().add(0, 1, 0));
    }
}
