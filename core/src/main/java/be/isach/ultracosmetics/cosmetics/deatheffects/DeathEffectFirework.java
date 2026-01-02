package be.isach.ultracosmetics.cosmetics.deatheffects;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.type.DeathEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeathEffectFirework extends DeathEffect {
    private static final Material FIREWORK_ROCKET = XMaterial.FIREWORK_ROCKET.get();
    private final FireworkMeta meta;

    public DeathEffectFirework(UltraPlayer owner, DeathEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(FIREWORK_ROCKET);

        List<Color> colors = List.of(
                Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.BLUE, Color.PURPLE, Color.FUCHSIA, Color.TEAL
        );

        Color randColor = colors.get(ThreadLocalRandom.current().nextInt(colors.size()));

        meta.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(randColor).build());
    }

    @Override
    public void displayParticles() {
        Player player = getPlayer();
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        firework.setFireworkMeta(meta);
        firework.setMetadata("uc_firework", new FixedMetadataValue(UltraCosmeticsData.get().getPlugin(), true));
        getUltraCosmetics().getScheduler().runAtLocationLater(player.getLocation(), firework::detonate, 5);
    }
}
