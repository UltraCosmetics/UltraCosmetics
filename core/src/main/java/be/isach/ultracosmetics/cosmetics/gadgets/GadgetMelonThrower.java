package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Effect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an instance of a melon thrower gadget summoned by a player.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class GadgetMelonThrower extends Gadget implements PlayerAffectingCosmetic, Updatable {
    private Item melon = null;
    private final Set<Item> melonSlices = new HashSet<>();
    private final XSound.SoundPlayer eatSound = XSound.ENTITY_PLAYER_BURP.record().withVolume(1.4f).withPitch(1.5f).soundPlayer();
    private final XSound.SoundPlayer useSound = XSound.ENTITY_GENERIC_EXPLODE.record().withVolume(1.4f).withPitch(1.5f).soundPlayer();

    public GadgetMelonThrower(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @EventHandler
    public void onTakeUpMelon(EntityPickupItemEvent event) {
        if (!melonSlices.contains(event.getItem())) return;
        // Should be done anyway by PlayerListener, but just to be safe
        event.setCancelled(true);
        if (event.getEntityType() == EntityType.PLAYER
                && event.getItem().getTicksLived() > 5
                && canAffect(event.getEntity(), getPlayer())) {
            event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 2));
            eatSound.atLocation(getPlayer().getLocation()).play();
            event.getItem().remove();
            melonSlices.remove(event.getItem());

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (melonSlices.contains(event.getEntity()) || melonSlices.contains(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @Override
    protected boolean checkRequirements(PlayerInteractEvent event) {
        // Check if the current melon has finished exploding.
        if (melon != null && melon.isValid()) {
            MessageManager.send(getPlayer(), "Gadgets.MelonThrower.Wait-For-Finish");
            return false;
        }
        return true;
    }

    @Override
    protected void onRightClick() {
        Player player = getPlayer();
        useSound.atLocation(player.getLocation()).play();
        melon = ItemFactory.createUnpickableItemDirectional(XMaterial.MELON, player, 1.3);
    }

    @Override
    public void onUpdate() {
        if (melon == null || !melon.isValid()) {
            melon = null;
            return;
        }
        if (melon.isOnGround()) {
            melon.getWorld().playEffect(melon.getLocation(), Effect.STEP_SOUND, 103);
            for (int i = 0; i < 8; i++) {
                melonSlices.add(ItemFactory.createUnpickableItemVariance(XMaterial.MELON_SLICE, melon.getLocation(), RANDOM, 0.75));
            }

            getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
                for (Item slice : melonSlices) {
                    slice.remove();
                }
                melonSlices.clear();
            }, 100);

            melon.remove();
            melon = null;
        }
    }

    @Override
    public void onClear() {
        if (melon != null) {
            melon.remove();
        }

        for (Item slice : melonSlices) {
            slice.remove();
        }
        melonSlices.clear();
    }
}
