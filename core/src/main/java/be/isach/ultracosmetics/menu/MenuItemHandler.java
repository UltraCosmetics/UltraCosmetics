package be.isach.ultracosmetics.menu;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.util.InventoryViewHelper;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.UnmovableItemProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MenuItemHandler implements UnmovableItemProvider {
    private final UltraCosmetics ultraCosmetics;
    private final UltraPlayerManager pm;
    private final int slot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
    private final boolean menuOnClick = SettingsManager.getConfig().getBoolean("Menu-Item.Open-Menu-On-Inventory-Click");
    private final ItemStack menuItem = ItemFactory.getMenuItem();

    public MenuItemHandler(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        this.pm = ultraCosmetics.getPlayerManager();
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public boolean itemMatches(ItemStack stack) {
        return menuItem.isSimilar(stack);
    }

    @Override
    public void handleDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void moveItem(int slot, Player player) {
        PlayerInventory inv = player.getInventory();
        // If there's something in the menu item slot, move it to
        // where the menu item was before and clear the menu item slot.
        if (inv.getItem(this.slot) != null) {
            inv.setItem(slot, inv.getItem(this.slot));
            inv.setItem(this.slot, null);
        }
        pm.getUltraPlayer(player).giveMenuItem();
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        UltraPlayer ultraPlayer = pm.getUltraPlayer(event.getPlayer());
        // apparently can happen if a player disconnected while on a pressure plate
        if (ultraPlayer == null) return;
        // Avoid triggering this when clicking in the inventory
        InventoryType t = InventoryViewHelper.getType(event.getPlayer());
        if (t != InventoryType.CRAFTING && t != InventoryType.CREATIVE) {
            return;
        }
        if (ultraPlayer.getCurrentTreasureChest() != null) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        ultraCosmetics.getMenus().openMainMenu(ultraPlayer);
    }

    @Override
    public void handleClick(Player player) {
        if (menuOnClick) {
            // if it's not delayed by one tick, the client holds the item in cursor slot until they open their inventory again
            ultraCosmetics.getScheduler().runAtEntityLater(player, () -> ultraCosmetics.getMenus().openMainMenu(pm.getUltraPlayer(player)), 1);
        }
    }
}
