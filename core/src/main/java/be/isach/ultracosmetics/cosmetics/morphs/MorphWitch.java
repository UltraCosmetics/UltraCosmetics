package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class MorphWitch extends MorphLeftClickCooldown {
    public MorphWitch(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics, 2);
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event) {
        event.setCancelled(true);
        ThrownPotion potion = getPlayer().launchProjectile(ThrownPotion.class);

        // Randomize color
        ItemStack stack = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(stack.getType());
        meta.setColor(Color.fromRGB(RANDOM.nextInt(0x1000000)));
        stack.setItemMeta(meta);
        potion.setItem(stack);
    }
}
