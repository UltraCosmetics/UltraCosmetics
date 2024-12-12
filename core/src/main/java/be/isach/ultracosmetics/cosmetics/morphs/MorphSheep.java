package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XSound;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import org.bukkit.DyeColor;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Represents an instance of a sheep morph summoned by a player.
 *
 * @author RadBuilder
 * @since 07-03-2017
 */
public class MorphSheep extends MorphLeftClickCooldown {
    private final XSound.SoundPlayer sound;

    public MorphSheep(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics, 3);
        sound = XSound.ENTITY_SHEEP_AMBIENT.record().publicSound(true).soundPlayer().forPlayers(getPlayer());
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {
        event.setCancelled(true);
        sound.play();
        SheepWatcher sheepWatcher = (SheepWatcher) getDisguise().getWatcher();

        final int[] count = {0};

        getUltraCosmetics().getScheduler().runAtEntityTimer(getPlayer(), (task) -> {
            if (count[0] > 9) {
                task.cancel();
                return;
            }
            sheepWatcher.setColor(DyeColor.values()[RANDOM.nextInt(DyeColor.values().length)]);
            count[0]++;
        }, 0, 2);
    }
}
