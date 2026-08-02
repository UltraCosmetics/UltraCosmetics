package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

public class MountGlacialSteed extends MountAbstractHorse {
    private static final ItemStack BOOTS = XMaterial.LEATHER_BOOTS.parseItem();

    static {
        BOOTS.addEnchantment(Enchantment.FROST_WALKER, 2);
    }

    private final ParticleDisplay display;

    public MountGlacialSteed(UltraPlayer owner, MountType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        display = ParticleDisplay.of(XParticle.ITEM_SNOWBALL).offset(0.4, 0.2, 0.4).withCount(5)
                .withLocationCaller(() -> entity.getLocation().add(0, 1, 0));
    }

    @Override
    public void setupEntity() {
        super.setupEntity();
        ((Horse) entity).setColor(Horse.Color.WHITE);
        if (BOOTS != null) {
            ((Horse) entity).getEquipment().setBoots(BOOTS, true);
        }
    }

    @Override
    public void onUpdate() {
        display.spawn();
    }
}
