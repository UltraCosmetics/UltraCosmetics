package be.isach.ultracosmetics.menu;

import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ClickData {

    private final Menu menu;
    private final UltraPlayer clicker;
    private final ClickType click;
    private final ItemStack clicked;
    private final int slot;

    public ClickData(Menu menu, UltraPlayer clicker, ClickType click, ItemStack clicked, int slot) {
        this.menu = menu;
        this.clicker = clicker;
        this.click = click;
        this.clicked = clicked;
        this.slot = slot;
    }

    public Menu getMenu() {
        return menu;
    }

    public ItemStack getClicked() {
        return clicked;
    }

    public int getSlot() {
        return slot;
    }

    public ClickType getClick() {
        return click;
    }

    public UltraPlayer getClicker() {
        return clicker;
    }
}
