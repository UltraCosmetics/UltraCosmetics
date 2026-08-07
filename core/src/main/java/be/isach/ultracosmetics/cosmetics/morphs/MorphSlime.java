package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public class MorphSlime extends MorphNoFall {
    private final double jumpSpeed = SettingsManager.getConfig().getDouble(getOptionPath("Jump-Speed"), 2.3);

    public MorphSlime(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (canUseSkill && event.getPlayer() == getPlayer() && getOwner().getCurrentMorph() == this
                && event.getReason().equalsIgnoreCase("Flying is not enabled on this server")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (canUseSkill && event.getPlayer() == getPlayer() && getOwner().getCurrentMorph() == this
                && getOwner().getAndSetCooldown(cosmeticType, 4, 0)) {
            MathUtils.applyVelocity(getPlayer(), new Vector(0, jumpSpeed, 0));
        }
    }

    @Override
    protected void onEquip() {
        super.onEquip();
        SlimeWatcher slimeWatcher = (SlimeWatcher) disguise.getWatcher();
        slimeWatcher.setSize(3);
    }
}
