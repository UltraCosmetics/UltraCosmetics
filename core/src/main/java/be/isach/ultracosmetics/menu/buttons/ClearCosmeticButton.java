package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ClearCosmeticButton implements Button {
    private final ItemStack stack;
    private final Set<Category> categories;

    public ClearCosmeticButton(Category category) {
        this.categories = category == null ? null : EnumSet.of(category);
        stack = ItemFactory.rename(ItemFactory.getItemStackFromConfig("Categories.Clear-Cosmetic-Item"),
                MessageManager.getMessage("Clear." + (category == null ? "Cosmetics" : category.getConfigPath())));
    }

    public ClearCosmeticButton() {
        this.categories = null;
        stack = ItemFactory.rename(ItemFactory.getItemStackFromConfig("Categories.Clear-Cosmetic-Item"),
                MessageManager.getMessage("Clear.Cosmetics"));
    }

    /**
     * Clears the given set of real categories when clicked. Used by the unified menu
     * when a virtual category groups cosmetics from more than one built-in category.
     */
    public ClearCosmeticButton(Set<Category> categories) {
        this.categories = categories == null || categories.isEmpty() ? null : EnumSet.copyOf(categories);
        stack = ItemFactory.rename(ItemFactory.getItemStackFromConfig("Categories.Clear-Cosmetic-Item"),
                MessageManager.getMessage("Clear.Cosmetics"));
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        return stack;
    }

    @Override
    public void onClick(ClickData clickData) {
        UltraPlayer clicker = clickData.getClicker();
        if (categories == null) {
            clicker.clear();
            return;
        }
        for (Category cat : categories) {
            if (cat.isSuits()) {
                for (Category suitCat : Category.values()) {
                    if (suitCat.isSuits()) {
                        clicker.removeCosmetic(suitCat);
                    }
                }
            } else {
                clicker.removeCosmetic(cat);
            }
        }
        clickData.getMenu().refresh(clicker);
    }

    // Kept for clarity, not currently used externally.
    @SuppressWarnings("unused")
    private static Set<Category> immutableOrNull(Set<Category> s) {
        return s == null ? null : Collections.unmodifiableSet(s);
    }
}
