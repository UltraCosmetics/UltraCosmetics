package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.MathUtils;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.XTag;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents an instance of a color bomb gadget summoned by a player.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class GadgetColorBomb extends Gadget implements PlayerAffectingCosmetic, Updatable {

    private Item bomb;
    private final Set<Item> items = new HashSet<>();
    private boolean running = false;
    private final XSound.SoundPlayer sound = XSound.ENTITY_CHICKEN_EGG.record().withVolume(0.2f).soundPlayer();
    // Can't use withEntity directly because it requires that the entity not currently be null
    private final ParticleDisplay particle = new ParticleDisplay().withLocationCaller(() -> bomb.getLocation()).withExtra(0.2);

    public GadgetColorBomb(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        bomb = ItemFactory.createUnpickableItemDirectional(ItemFactory.randomFromTag(XTag.WOOL), getPlayer(), 0.7532);
    }

    @Override
    public void onUpdate() {
        if (bomb == null || !bomb.isValid()) return;
        if (!running && bomb.isOnGround()) {
            running = true;
            bomb.setVelocity(new Vector(0, 0, 0));
            getUltraCosmetics().getScheduler().runAtEntityLater(bomb, this::onClear, 100);
        }

        if (!running) return;

        switch (RANDOM.nextInt(5)) {
            default:
                particle.withParticle(XParticle.FIREWORK);
                break;
            case 3:
                particle.withParticle(XParticle.FLAME);
                break;
            case 4:
                particle.withParticle(XParticle.WITCH);
                break;
        }

        particle.spawn();

        Iterator<Item> iter = items.iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            if (item.getTicksLived() > 15) {
                item.remove();
                iter.remove();
            }
        }

        getUltraCosmetics().getScheduler().runAtEntity(getPlayer(), (task) -> {
            if (bomb == null) {
                return;
            }

            Vector velocity = new Vector(0, 0.5, 0).add(MathUtils.getRandomCircleVector().multiply(0.1));
            Item item = ItemFactory.spawnUnpickableItem(ItemFactory.randomItemFromTag(XTag.WOOL), bomb.getLocation().add(0, 0.15, 0), velocity);
            items.add(item);
            sound.atLocation(item.getLocation()).play();

            Player player = getPlayer();
            for (Entity entity : bomb.getNearbyEntities(1.5, 1, 1.5)) {
                if (entity instanceof Player && canAffect(entity, player)) {
                    MathUtils.applyVelocity(entity, new Vector(0, 0.5, 0).add(MathUtils.getRandomCircleVector().multiply(0.1)));
                }
            }
        });
    }

    @Override
    public void onClear() {
        if (bomb != null) {
            bomb.remove();
            bomb = null;
        }

        for (Item item : items) {
            item.remove();
        }
        items.clear();

        running = false;
    }
}
