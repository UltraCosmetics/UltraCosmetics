package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.EntitySpawner;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

/**
 * Represents an instance of an enderman morph summoned by a player.
 *
 * @author iSach
 * @since 08-26-2015
 */
public class MorphEnderman extends MorphNoFall {
    private static final double COOLDOWN = 3.5;
    private final String mode = SettingsManager.getConfig().getString(getOptionPath("Mode"), "Ray trace");

    public MorphEnderman(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onEquip() {
        super.onEquip();
        if (canUseSkill) {
            getPlayer().setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!canUseSkill || player != getPlayer() || player.getGameMode() == GameMode.CREATIVE || event.getPlayer().isFlying()) {
            return;
        }
        player.setFlying(false);
        event.setCancelled(true);
        if (!getOwner().getAndSetCooldown(cosmeticType, COOLDOWN, 0)) {
            return;
        }
        if (mode.equalsIgnoreCase("Fast")) {
            if (!fast(player)) return;
        } else if (mode.equalsIgnoreCase("Enderpearl")) {
            player.launchProjectile(EnderPearl.class, player.getLocation().getDirection());
        } else { // Ray trace
            rayTrace(player);
        }
    }

    private void rayTrace(Player player) {
        Block b = player.getTargetBlock(null, 17);
        Location loc = b.getLocation();
        loc.setPitch(player.getLocation().getPitch());
        loc.setYaw(player.getLocation().getYaw());
        getUltraCosmetics().getScheduler().teleportAsync(player, loc);
        spawnFireworks(loc);
    }

    private void spawnFireworks(Location loc) {
        EntitySpawner.spawnFireworks(loc.add(0.5, 0, 0.5), Color.BLACK, Color.BLACK);
    }

    private boolean fast(Player player) {
        Vector v = player.getLocation().getDirection();
        v.setY(0);
        v.normalize().multiply(16);
        Location loc = player.getLocation().add(v);
        if (!BlockUtils.isAir(loc.getBlock().getType())) {
            getUltraCosmetics().getScheduler().teleportAsync(player, loc);
            spawnFireworks(loc.add(0.5, 0, 0.5));
            return true;
        }
        return false;
    }

    @Override
    public void onClear() {
        if (canUseSkill && getPlayer().getGameMode() != GameMode.CREATIVE) {
            getPlayer().setAllowFlight(false);
        }
    }
}
