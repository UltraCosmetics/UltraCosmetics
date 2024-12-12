package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.EntitySpawner;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Color;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a chickenator gadget summoned by a player.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class GadgetChickenator extends Gadget {

    private final List<Item> items = new ArrayList<>();
    private final XSound.SoundPlayer ambient;
    private final XSound.SoundPlayer explode;
    private final XSound.SoundPlayer hurt;

    public GadgetChickenator(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        this.ambient = XSound.ENTITY_CHICKEN_AMBIENT.record().withVolume(1.4f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
        this.explode = XSound.ENTITY_GENERIC_EXPLODE.record().withVolume(0.3f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
        this.hurt = XSound.ENTITY_CHICKEN_HURT.record().withVolume(1.4f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
    }

    @Override
    protected void onRightClick() {
        final Chicken chicken = (Chicken) getPlayer().getWorld().spawnEntity(getPlayer().getEyeLocation(), EntityType.CHICKEN);
        chicken.setNoDamageTicks(500);
        chicken.setVelocity(getPlayer().getLocation().getDirection().multiply(Math.PI / 1.5));
        ambient.play();
        explode.play();
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> {
            EntitySpawner.spawnFireworks(chicken.getLocation(), Color.WHITE, Color.WHITE);
            hurt.play();
            chicken.remove();
            for (int i = 0; i < 30; i++) {
                items.add(ItemFactory.createUnpickableItemVariance(XMaterial.COOKED_CHICKEN, chicken.getLocation(), RANDOM, 1));
            }
            getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), () -> items.forEach(Item::remove), 50);
        }, 9);
        getPlayer().updateInventory();
    }

    @Override
    public void onClear() {
        for (Item i : items) {
            i.remove();
        }
        items.clear();
    }
}
