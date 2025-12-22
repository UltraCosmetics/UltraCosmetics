package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * Represents an instance of a llama pet summoned by a player.
 *
 * @author RadBuilder
 * @since 07-02-2017
 */
public class PetLlama extends Pet {
    public PetLlama(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics, XMaterial.WHITE_WOOL);
    }

    @Override
    protected boolean customize(String customization) {
        String[] parts = customization.split(":", 2);
        Color color;
        Material carpet = null;
        try {
            color = Color.valueOf(parts[0].toUpperCase(Locale.ROOT));
            if (parts.length > 1) {
                carpet = Material.matchMaterial(parts[1]);
                if (carpet == null || !carpet.isItem()) return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        Llama llama = (Llama) entity;
        llama.setColor(color);
        if (carpet != null) {
            llama.getInventory().setDecor(new ItemStack(carpet));
        }
        return true;
    }
}
