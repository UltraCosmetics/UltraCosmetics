package be.isach.ultracosmetics.hook;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.gadgets.Gadget;
import be.isach.ultracosmetics.menu.CosmeticsInventoryHolder;
import be.isach.ultracosmetics.menu.MenuItemHandler;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import de.jeff_media.chestsort.api.ChestSortAPI;
import de.jeff_media.chestsort.api.ChestSortEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestSortHook implements Listener {
    private final ItemStack menuItem = MenuItemHandler.getMenuItem();
    private final UltraPlayerManager pm;

    public ChestSortHook(UltraCosmetics ultraCosmetics) {
        pm = ultraCosmetics.getPlayerManager();
    }

    public void setUnsortable(Inventory inventory) {
        ChestSortAPI.setUnsortable(inventory);
    }

    @EventHandler
    public void onChestSort(ChestSortEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (event.getInventory().getHolder() instanceof CosmeticsInventoryHolder) {
            event.setCancelled(true);
            return;
        }
        if (menuItem != null) event.setUnmovable(menuItem);
        Gadget gadget = pm.getUltraPlayer((Player) event.getPlayer()).getCurrentGadget();
        if (gadget != null) {
            gadget.updateItemStack();
            event.setUnmovable(gadget.getItemStack());
        }
    }
}
