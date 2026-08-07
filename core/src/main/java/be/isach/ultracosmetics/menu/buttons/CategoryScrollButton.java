package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.menus.MenuUnified;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.inventory.ItemStack;

/**
 * Scrolls the category column of {@link MenuUnified} up or down, shown only when there
 * are more categories than fit in the visible column at once.
 */
public class CategoryScrollButton implements Button {
    private final MenuUnified menu;
    private final int modifier;
    private final ItemStack stack;

    public CategoryScrollButton(MenuUnified menu, boolean up) {
        this.menu = menu;
        this.modifier = up ? -1 : 1;
        this.stack = ItemFactory.rename(
                ItemFactory.getItemStackFromConfig("Categories.Unified-Menu." + (up ? "Scroll-Up-Item" : "Scroll-Down-Item")),
                MessageManager.getMessage("Menu.Misc.Button." + (up ? "Scroll-Up-Categories" : "Scroll-Down-Categories"))
        );
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        return stack;
    }

    @Override
    public void onClick(ClickData clickData) {
        menu.scrollCategories(clickData.getClicker(), modifier);
    }
}
