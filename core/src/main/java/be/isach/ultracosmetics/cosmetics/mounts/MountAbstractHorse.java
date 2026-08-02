package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MountType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.inventory.ItemStack;

public abstract class MountAbstractHorse extends Mount {
    private static final ItemStack SADDLE = XMaterial.SADDLE.parseItem();

    public MountAbstractHorse(UltraPlayer ultraPlayer, MountType type, UltraCosmetics ultraCosmetics) {
        super(ultraPlayer, type, ultraCosmetics);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setupEntity() {
        AbstractHorse horse = (AbstractHorse) entity;
        horse.setTamed(true);
        horse.setDomestication(1);
        horse.getInventory().setSaddle(SADDLE);
        horse.setJumpStrength(0.7);
    }
}
