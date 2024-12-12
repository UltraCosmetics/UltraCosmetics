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

public class DeathEffectFirework extends DeathEffect {
    private static final Material FIREWORK_ROCKET = XMaterial.FIREWORK_ROCKET.parseMaterial();
    private final FireworkMeta meta;

    public DeathEffectFirework(UltraPlayer owner, DeathEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(FIREWORK_ROCKET);
        meta.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.RED).build());
    }

    @Override
    public void displayParticles() {
        Player player = getPlayer();
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        firework.setFireworkMeta(meta);
        firework.setMetadata("uc_firework", new FixedMetadataValue(UltraCosmeticsData.get().getPlugin(), true));
        getUltraCosmetics().getScheduler().runAtEntityLater(getPlayer(), firework::detonate, 5);
    }
}
