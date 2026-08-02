package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XEntityType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class GadgetFirework extends Gadget {
    private static final EntityType FIREWORK_ENTITY = XEntityType.FIREWORK_ROCKET.get();

    public GadgetFirework(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected void onRightClick() {
        Firework fw = (Firework) getPlayer().getWorld().spawnEntity(getPlayer().getLocation(), FIREWORK_ENTITY);
        FireworkMeta fwm = fw.getFireworkMeta();

        int rt = RANDOM.nextInt(5);
        FireworkEffect.Type type = FireworkEffect.Type.values()[rt];

        Color c1 = Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
        Color c2 = Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));

        FireworkEffect effect = FireworkEffect.builder().flicker(RANDOM.nextBoolean())
                .withColor(c1).withFade(c2).with(type)
                .trail(RANDOM.nextBoolean()).build();

        fwm.addEffect(effect);

        fwm.setPower(RANDOM.nextInt(3));

        fw.setFireworkMeta(fwm);
        fw.setMetadata("uc_firework", new FixedMetadataValue(getUltraCosmetics(), true));
    }
}
