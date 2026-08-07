package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.menus.MenuUnified;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.inventory.ItemStack;

/**
 * Changes the page of the active category's content in {@link MenuUnified}.
 */
public class UnifiedPageButton implements Button {
    private final MenuUnified menu;
    private final int modifier;
    private final ItemStack stack;

    public UnifiedPageButton(MenuUnified menu, boolean next) {
        this.menu = menu;
        this.modifier = next ? 1 : -1;
        this.stack = ItemFactory.rename(
                ItemFactory.getItemStackFromConfig("Categories." + (next ? "Next-Page-Item" : "Previous-Page-Item")),
                MessageManager.getMessage("Menu.Misc.Button." + (next ? "Next-Page" : "Previous-Page"))
        );
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        return stack;
    }

    @Override
    public void onClick(ClickData clickData) {
        menu.changePage(clickData.getClicker(), modifier);
    }
}
