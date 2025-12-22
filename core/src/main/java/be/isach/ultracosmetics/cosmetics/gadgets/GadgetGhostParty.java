package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.EntitySpawner;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an instance of a ghost party gadget summoned by a player.
 *
 * @author iSach
 * @since 10-18-2015
 */
public class GadgetGhostParty extends Gadget implements Updatable {

    private static final ItemStack GHOST_HEAD = ItemFactory.createSkull("68d2183640218ab330ac56d2aab7e29a9790a545f691619e38578ea4a69ae0b6", ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Ghost");
    private static final ItemStack GHOST_CHESTPLATE = ItemFactory.createColouredLeather(XMaterial.LEATHER_CHESTPLATE.get(), 255, 255, 255);
    private static final ItemStack DIAMOND_HOE = XMaterial.DIAMOND_HOE.parseItem();
    private static final ParticleDisplay CLOUD = ParticleDisplay.of(XParticle.CLOUD).offset(0.05);
    private EntitySpawner<Bat> bats = EntitySpawner.empty();
    private final Set<ArmorStand> ghosts = new HashSet<>();

    public GadgetGhostParty(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        Location loc = getPlayer().getLocation().add(0, 1, 0);
        bats = new EntitySpawner<>(EntityType.BAT, loc, 20, bat -> {
            bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 1));
            ArmorStand ghost = (ArmorStand) bat.getWorld().spawnEntity(bat.getLocation(), EntityType.ARMOR_STAND);
            ghost.setPersistent(false);
            ghost.setSmall(true);
            ghost.setGravity(false);
            ghost.setVisible(false);
            ghost.getEquipment().setHelmet(GHOST_HEAD);
            ghost.getEquipment().setChestplate(GHOST_CHESTPLATE);
            ghost.getEquipment().setItemInMainHand(DIAMOND_HOE);
            ghosts.add(ghost);
            bat.addPassenger(ghost);
        }, getUltraCosmetics());

        getUltraCosmetics().getScheduler().runAtLocationLater(loc, this::killBats, 160);
    }

    @EventHandler
    public void onPlayerInteractGhost(PlayerInteractEntityEvent event) {
        // For some reason, the server considers it to be the bats that are interacted with, not the ghosts.
        if (bats.contains(event.getRightClicked()) || ghosts.contains(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    private void killBats() {
        ghosts.forEach(ArmorStand::remove);
        bats.removeEntities();
    }

    @Override
    public void onUpdate() {
        for (Bat bat : bats.getEntities()) {
            CLOUD.spawn(bat.getLocation().add(0, 1.5, 0));
        }
    }

    @Override
    public void onClear() {
        killBats();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (bats.contains(event.getEntity()) || ghosts.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
