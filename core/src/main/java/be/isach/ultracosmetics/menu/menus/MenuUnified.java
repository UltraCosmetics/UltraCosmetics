package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.CommandManager;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.SuitCategory;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.menu.Menu;
import be.isach.ultracosmetics.menu.Menus;
import be.isach.ultracosmetics.menu.buttons.CategoryScrollButton;
import be.isach.ultracosmetics.menu.buttons.CategorySelectorButton;
import be.isach.ultracosmetics.menu.buttons.ClearCosmeticButton;
import be.isach.ultracosmetics.menu.buttons.CosmeticButton;
import be.isach.ultracosmetics.menu.buttons.EquipWholeSuitButton;
import be.isach.ultracosmetics.menu.buttons.FilterCosmeticsButton;
import be.isach.ultracosmetics.menu.buttons.KeysButton;
import be.isach.ultracosmetics.menu.buttons.OpenChestButton;
import be.isach.ultracosmetics.menu.buttons.UnifiedPageButton;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A single-window menu: categories are picked from the column on the left, and the
 * currently active category's cosmetics are shown on the right, instead of opening a
 * separate menu per category. Used instead of {@link MenuMain} when
 * {@code Categories.Unified-Menu.Enabled} is set to true.
 */
public class MenuUnified extends Menu {
    private static final int[] CATEGORY_SLOTS = {9, 18, 27, 36};
    private static final int SCROLL_UP_SLOT = 0;
    private static final int SCROLL_DOWN_SLOT = 45;
    private static final int DESCRIPTION_SLOT = 4;
    private static final int OPEN_CHEST_SLOT = 8;
    private static final int KEYS_SLOT = 53;
    private static final int PREV_PAGE_SLOT = 46;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int CLEAR_SLOT = 48;
    private static final int FILTER_SLOT = 49;
    // Suit sets use their own 6-wide layout (skipping the center column, which the
    // description occupies) spanning the whole-equip row plus the 4 piece rows.
    private static final int[] SUIT_COLUMNS = {1, 2, 3, 5, 6, 7};
    private static final int SUITS_PER_PAGE = SUIT_COLUMNS.length;

    private final Map<UUID, Category> activeCategory = new HashMap<>();
    private final Map<UUID, Integer> activePage = new HashMap<>();
    private final Map<UUID, Integer> categoryScrollOffset = new HashMap<>();
    private final Component title = MessageManager.getMessage("Menu.Unified.Title");

    public MenuUnified(UltraCosmetics ultraCosmetics) {
        super("main", ultraCosmetics);
    }

    @Override
    public void open(UltraPlayer player) {
        Category current = activeCategory.get(player.getUUID());
        open(player, current, activePage.getOrDefault(player.getUUID(), 1));
    }

    /**
     * Opens the unified menu on the given category and page, falling back to the first
     * category the player can see if {@code category} is null or no longer visible to them.
     */
    public void open(UltraPlayer player, Category category, int page) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (!bukkitPlayer.hasPermission(permission)) {
            CommandManager.sendNoPermissionMessage(bukkitPlayer);
            return;
        }

        List<Category> visible = getVisibleCategories(bukkitPlayer);
        if (category == null || !visible.contains(category)) {
            category = visible.isEmpty() ? null : visible.get(0);
        }
        activeCategory.put(player.getUUID(), category);

        int maxPages = category == null ? 1 : getMaxPages(category, player);
        if (page > maxPages) page = maxPages;
        if (page < 1) page = 1;
        activePage.put(player.getUUID(), page);

        // Reuse the currently-open inventory instead of opening a fresh one when the
        // player is already looking at this menu (switching category / turning page).
        // Otherwise Bukkit closes and reopens the window, producing a visible flicker.
        // Safe because the title is constant for MenuUnified.
        Inventory inventory = getReusableInventory(bukkitPlayer);
        boolean reuse = inventory != null;
        if (reuse) {
            clickRunnableMap.remove(inventory);
            inventory.clear();
        } else {
            inventory = createInventory(title);
        }

        putDescription(inventory);
        putCategorySelectors(inventory, player, visible);
        if (category != null) {
            putContent(inventory, player, category, page);
        }
        putFooter(inventory, player, category, page, maxPages);
        fillInventory(inventory);

        if (reuse) {
            bukkitPlayer.updateInventory();
        } else {
            bukkitPlayer.openInventory(inventory);
        }
    }

    private Inventory getReusableInventory(Player player) {
        Inventory top = player.getOpenInventory().getTopInventory();
        return clickRunnableMap.containsKey(top) ? top : null;
    }

    @Override
    public void refresh(UltraPlayer player) {
        open(player);
    }

    public Category getActiveCategory(UltraPlayer player) {
        return activeCategory.get(player.getUUID());
    }

    public int getActivePage(UltraPlayer player) {
        return activePage.getOrDefault(player.getUUID(), 1);
    }

    public void scrollCategories(UltraPlayer player, int delta) {
        int current = categoryScrollOffset.getOrDefault(player.getUUID(), 0);
        categoryScrollOffset.put(player.getUUID(), current + delta);
        refresh(player);
    }

    public void cleanupPlayer(UUID uuid) {
        activeCategory.remove(uuid);
        activePage.remove(uuid);
        categoryScrollOffset.remove(uuid);
    }

    private List<Category> getVisibleCategories(Player player) {
        List<Category> visible = new ArrayList<>();
        boolean suits = false;
        Menus menus = ultraCosmetics.getMenus();
        for (Category category : Category.enabled()) {
            if (category.isSuits()) {
                if (suits) continue;
                suits = true;
                category = Category.SUITS_HELMET;
            }
            if (player.hasPermission(menus.getCategoryMenu(category).getPermission())) {
                visible.add(category);
            }
        }
        return visible;
    }

    private void putDescription(Inventory inventory) {
        ItemStack stack = ItemFactory.getItemStackFromConfig("Categories.Unified-Menu.Description-Item");
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(MessageManager.toLegacy(title));
        meta.setLore(MessageManager.getLore("Menu.Unified.Description", null));
        stack.setItemMeta(meta);
        inventory.setItem(DESCRIPTION_SLOT, stack);
    }

    private void putCategorySelectors(Inventory inventory, UltraPlayer player, List<Category> visible) {
        if (visible.isEmpty()) return;

        int maxOffset = Math.max(0, visible.size() - CATEGORY_SLOTS.length);
        int offset = Math.max(0, Math.min(categoryScrollOffset.getOrDefault(player.getUUID(), 0), maxOffset));
        categoryScrollOffset.put(player.getUUID(), offset);

        for (int i = 0; i < CATEGORY_SLOTS.length && offset + i < visible.size(); i++) {
            Category category = visible.get(offset + i);
            putItem(inventory, CATEGORY_SLOTS[i], new CategorySelectorButton(this, category, ultraCosmetics), player);
        }
        if (offset > 0) {
            putItem(inventory, SCROLL_UP_SLOT, new CategoryScrollButton(this, true), player);
        }
        if (offset < maxOffset) {
            putItem(inventory, SCROLL_DOWN_SLOT, new CategoryScrollButton(this, false), player);
        }
    }

    private int getMaxPages(Category category, UltraPlayer player) {
        if (category.isSuits()) {
            CosmeticMenu<?> suitsMenu = ultraCosmetics.getMenus().getCategoryMenu(Category.SUITS_HELMET);
            int count = 0;
            for (SuitCategory suitCategory : SuitCategory.enabled()) {
                if (!suitsMenu.shouldHideItem(player, suitCategory.getHelmet())
                        || !suitsMenu.shouldHideItem(player, suitCategory.getChestplate())
                        || !suitsMenu.shouldHideItem(player, suitCategory.getLeggings())
                        || !suitsMenu.shouldHideItem(player, suitCategory.getBoots())) {
                    count++;
                }
            }
            return Math.max(1, ((count - 1) / SUITS_PER_PAGE) + 1);
        }
        return ultraCosmetics.getMenus().getCategoryMenu(category).getMaxPages(player);
    }

    private void putContent(Inventory inventory, UltraPlayer player, Category category, int page) {
        if (category.isSuits()) {
            putSuitsContent(inventory, player, page);
        } else {
            putGenericContent(inventory, player, ultraCosmetics.getMenus().getCategoryMenu(category), page);
        }
    }

    private <T extends CosmeticType<?>> void putGenericContent(Inventory inventory, UltraPlayer player, CosmeticMenu<T> categoryMenu, int page) {
        for (Map.Entry<Integer, T> entry : categoryMenu.getSlots(page, player).entrySet()) {
            CosmeticButton button = CosmeticButton.fromType(entry.getValue(), player, ultraCosmetics);
            putItem(inventory, entry.getKey(), button, player);
        }
    }

    private void putSuitsContent(Inventory inventory, UltraPlayer player, int page) {
        CosmeticMenu<?> suitsMenu = ultraCosmetics.getMenus().getCategoryMenu(Category.SUITS_HELMET);
        List<SuitCategory> enabled = SuitCategory.enabled();
        int from = (page - 1) * SUITS_PER_PAGE;
        int to = page * SUITS_PER_PAGE;
        for (int i = from; i < to && i < enabled.size(); i++) {
            SuitCategory suitCategory = enabled.get(i);
            int column = SUIT_COLUMNS[i % SUITS_PER_PAGE];
            boolean anyVisible = suitCategory.getPieces().stream().anyMatch(t -> !suitsMenu.shouldHideItem(player, t));
            if (anyVisible) {
                putItem(inventory, column, new EquipWholeSuitButton(suitCategory, ultraCosmetics), player);
            }
            int row = 1;
            for (SuitType type : suitCategory.getPieces()) {
                if (!suitsMenu.shouldHideItem(player, type)) {
                    putItem(inventory, column + row * 9, CosmeticButton.fromType(type, player, ultraCosmetics), player);
                }
                row++;
            }
        }
    }

    private void putFooter(Inventory inventory, UltraPlayer player, Category activeCat, int page, int maxPages) {
        if (activeCat != null) {
            if (page > 1) {
                putItem(inventory, PREV_PAGE_SLOT, new UnifiedPageButton(this, false), player);
            }
            if (page < maxPages) {
                putItem(inventory, NEXT_PAGE_SLOT, new UnifiedPageButton(this, true), player);
            }
            putItem(inventory, CLEAR_SLOT, new ClearCosmeticButton(activeCat), player);
        }
        putItem(inventory, FILTER_SLOT, new FilterCosmeticsButton(), player);
        if (UltraCosmeticsData.get().areTreasureChestsEnabled()) {
            putItem(inventory, OPEN_CHEST_SLOT, new OpenChestButton(ultraCosmetics), player);
            putItem(inventory, KEYS_SLOT, new KeysButton(ultraCosmetics), player);
        }
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer player) {
        // Rendering is fully handled by open(UltraPlayer, Category, int); this override
        // only exists to satisfy Menu's abstract contract and is never invoked.
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected Component getName() {
        return title;
    }
}
