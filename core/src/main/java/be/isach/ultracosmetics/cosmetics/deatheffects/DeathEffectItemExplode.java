package be.isach.ultracosmetics.cosmetics.deatheffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.DeathEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DeathEffectItemExplode extends DeathEffect {
    private static final List<XMaterial> materials;

    static {
        List<XMaterial> mats = List.of(XMaterial.GLOW_SQUID_SPAWN_EGG,
                XMaterial.TROPICAL_FISH_SPAWN_EGG,
                XMaterial.GLOW_BERRIES,
                XMaterial.CHORUS_FRUIT,
                XMaterial.PINK_DYE,
                XMaterial.RED_DYE,
                XMaterial.ORANGE_DYE,
                XMaterial.LIME_DYE,
                XMaterial.LIGHT_BLUE_DYE,
                XMaterial.PURPLE_DYE,
                XMaterial.RESIN_BRICK,
                XMaterial.NETHER_WART,
                XMaterial.FERMENTED_SPIDER_EYE,
                XMaterial.MAGMA_CREAM,
                XMaterial.CAKE,
                XMaterial.GOLDEN_APPLE,
                XMaterial.BLUE_EGG,
                XMaterial.HONEYCOMB);
        materials = mats.stream().filter(XMaterial::isSupported).toList();
    }

    private final List<Item> items = new ArrayList<>();
    private final XSound.SoundPlayer explode;
    private final XSound.SoundPlayer hurt;

    public DeathEffectItemExplode(UltraPlayer owner, DeathEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);

        this.explode = XSound.ENTITY_GENERIC_EXPLODE.record().withVolume(0.3f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
        this.hurt = XSound.ENTITY_CHICKEN_HURT.record().withVolume(1.4f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
    }

    @Override
    public void displayParticles() {
        Player player = getPlayer();
        explode.play();
        hurt.play();

        for (int i = 0; i < 30; i++) {
            XMaterial randMaterial = materials.get(RANDOM.nextInt(materials.size()));
            items.add(ItemFactory.createUnpickableItemVariance(randMaterial, player.getLocation(), RANDOM, 1));
        }
        Location loc = items.get(0).getLocation();
        getUltraCosmetics().getScheduler().runAtLocationLater(loc, () -> items.forEach(Item::remove), 50);
    }

    @Override
    public void onClear() {
        items.forEach(Item::remove);
        items.clear();
    }

}
