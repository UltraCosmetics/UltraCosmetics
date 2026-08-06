package be.isach.ultracosmetics.menu;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.CommandManager;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.menu.buttons.ClearCosmeticButton;
import be.isach.ultracosmetics.menu.buttons.CosmeticButton;
import be.isach.ultracosmetics.menu.buttons.FilterCosmeticsButton;
import be.isach.ultracosmetics.menu.buttons.MainMenuButton;
import be.isach.ultracosmetics.menu.buttons.NextPageButton;
import be.isach.ultracosmetics.menu.buttons.PreviousPageButton;
import be.isach.ultracosmetics.permissions.PermissionManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * A cosmetic menu.
 *
 * @author iSach
 * @since 08-09-2016
 */
public abstract class CosmeticMenu<T extends CosmeticType<?>> extends Menu {

    public static final int[] COSMETICS_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    /**
     * Accuracy not guaranteed, specifically for suits.
     */
    protected final Category category;
    protected final PermissionManager pm = ultraCosmetics.getPermissionManager();
    private final Map<UUID, Integer> lastUsedPages = new HashMap<>();
    protected final boolean hideNoPermissionItems = SettingsManager.getConfig().getBoolean("No-Permission.Dont-Show-Item");

    public CosmeticMenu(UltraCosmetics ultraCosmetics, Category category) {
        super(category.getConfigPath(), ultraCosmetics);
        this.category = category;
    }

    @Override
    public void open(UltraPlayer player) {
        open(player, 1);
    }

    public void open(UltraPlayer player, int page) {
        if (!category.isEnabled()) {
            throw new IllegalStateException("Cannot show menu for disabled category");
        }
        if (!player.getBukkitPlayer().hasPermission(permission)) {
            CommandManager.sendNoPermissionMessage(player.getBukkitPlayer());
            return;
        }
        final int maxPages = getMaxPages(player);
        if (page > maxPages) {
            page = maxPages;
        }
        if (page < 1) {
            page = 1;
        }
        lastUsedPages.put(player.getUUID(), page);

        Inventory inventory = createInventory(maxPages == 1 ? getName() : getName(page, player));
        boolean hasUnlockable = hasUnlockable(player);

        // Cosmetic types.
        Map<Integer, T> slots = getSlots(page, player);
        for (Entry<Integer, T> entry : slots.entrySet()) {
            int slot = entry.getKey();
            T cosmeticType = entry.getValue();

            if (shouldHideItem(player, cosmeticType)) continue;
            CosmeticButton button = CosmeticButton.fromType(cosmeticType, player, ultraCosmetics);
            putItem(inventory, slot, button, player);
        }

        if (page > 1) {
            putItem(inventory, getSize() - 18, new PreviousPageButton(), player);
        }
        if (page < maxPages) {
            putItem(inventory, getSize() - 10, new NextPageButton(), player);
        }

        putItem(inventory, inventory.getSize() - 5, new ClearCosmeticButton(category), player);

        if (getCategory().hasGoBackArrow()) {
            putItem(inventory, inventory.getSize() - 6, new MainMenuButton(ultraCosmetics), player);
        }

        if (hasUnlockable && !hideNoPermissionItems) {
            putItem(inventory, inventory.getSize() - 3, new FilterCosmeticsButton(), player);
        }

        putItems(inventory, player, page);
        fillInventory(inventory);
        player.getBukkitPlayer().openInventory(inventory);
    }

    @Override
    public void refresh(UltraPlayer player) {
        open(player, getCurrentPage(player));
    }

    /**
     * @param ultraPlayer The menu owner
     * @return The current page of the menu opened by ultraPlayer
     */
    public int getCurrentPage(UltraPlayer ultraPlayer) {
        return lastUsedPages.getOrDefault(ultraPlayer.getUUID(), 1);
    }

    /**
     * Gets the max amount of pages.
     *
     * @return the maximum amount of pages.
     */
    public int getMaxPages(UltraPlayer player) {
        int i = 0;
        for (CosmeticType<?> type : CosmeticType.enabledOf(category)) {
            if (!shouldHideItem(player, type)) {
                i++;
            }
        }
        return Math.max(1, ((i - 1) / 21) + 1);
    }

    public int getItemsPerPage() {
        return 21;
    }

    /**
     * @param page The page to open.
     * @return The name of the menu with page detailed.
     */
    protected Component getName(int page, UltraPlayer ultraPlayer) {
        return Component.empty().append(getName()).appendSpace()
                .append(Component.text("(" + page + "/" + getMaxPages(ultraPlayer) + ")", NamedTextColor.GRAY, TextDecoration.ITALIC));
    }

    @Override
    protected int getSize() {
        // I think for consistency it's better to have all menus max size
        return 54;
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer ultraPlayer) {
        // --
    }

    /**
     * @return The name of the menu.
     */
    @Override
    protected Component getName() {
        return MessageManager.getMessage("Menu." + category.getConfigPath() + ".Title");
    }

    public Category getCategory() {
        return category;
    }

    /**
     * Puts items in the inventory.
     *
     * @param inventory   Inventory.
     * @param ultraPlayer Inventory Owner.
     * @param page        Page to open.
     */
    protected void putItems(Inventory inventory, UltraPlayer ultraPlayer, int page) {
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, T> getSlots(int page, UltraPlayer player) {
        int start = 21 * (page - 1);
        int limit = 21;
        int current = 0;
        Map<Integer, T> slots = new HashMap<>();
        List<T> enabled = new ArrayList<>();
        CosmeticType.enabledOf(category).forEach(t -> enabled.add((T) t));
        enabled.removeIf(k -> shouldHideItem(player, k));
        for (int i = start; current < limit && i < enabled.size(); i++) {
            slots.put(COSMETICS_SLOTS[current++ % 21], enabled.get(i));
        }
        return slots;
    }


    public boolean shouldHideItem(UltraPlayer player, CosmeticType<?> cosmeticType) {
        return (hideNoPermissionItems || player.isFilteringByOwned()) && !player.canEquip(cosmeticType);
    }

    public boolean hasUnlockable(UltraPlayer player) {
        if (ultraCosmetics.getWorldGuardManager().isInShowroom(player.getBukkitPlayer())) return false;
        for (CosmeticType<?> type : CosmeticType.enabledOf(category)) {
            if (!player.canEquip(type)) {
                return true;
            }
        }
        return false;
    }
}
