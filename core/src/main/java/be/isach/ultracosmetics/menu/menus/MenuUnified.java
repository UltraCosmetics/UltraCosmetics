package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.CommandManager;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.SuitCategory;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.menu.CosmeticActions;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.menu.Menu;
import be.isach.ultracosmetics.menu.Menus;
import be.isach.ultracosmetics.menu.VirtualCategory;
import be.isach.ultracosmetics.menu.buttons.CategoryScrollButton;
import be.isach.ultracosmetics.menu.buttons.CategorySelectorButton;
import be.isach.ultracosmetics.menu.buttons.ClearCosmeticButton;
import be.isach.ultracosmetics.menu.buttons.CosmeticButton;
import be.isach.ultracosmetics.menu.buttons.EquipWholeSuitButton;
import be.isach.ultracosmetics.menu.buttons.FilterCosmeticsButton;
import be.isach.ultracosmetics.menu.buttons.KeysButton;
import be.isach.ultracosmetics.menu.buttons.OpenChestButton;
import be.isach.ultracosmetics.menu.buttons.RenamePetButton;
import be.isach.ultracosmetics.menu.buttons.ToggleGadgetsButton;
import be.isach.ultracosmetics.menu.buttons.ToggleMorphSelfViewButton;
import be.isach.ultracosmetics.menu.buttons.UnifiedPageButton;
import be.isach.ultracosmetics.menu.buttons.VirtualCategorySelectorButton;
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
    // 4-row content grid used for cosmetic buttons.
    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43,
    };
    private static final int ITEMS_PER_PAGE = CONTENT_SLOTS.length;
    // Suit sets use their own 6-wide layout (skipping the center column, which the
    // description occupies) spanning the whole-equip row plus the 4 piece rows.
    private static final int[] SUIT_COLUMNS = {1, 2, 3, 5, 6, 7};
    private static final int SUITS_PER_PAGE = SUIT_COLUMNS.length;

    private final Map<UUID, Category> activeCategory = new HashMap<>();
    private final Map<UUID, VirtualCategory> activeVirtual = new HashMap<>();
    private final Map<UUID, Integer> activePage = new HashMap<>();
    private final Map<UUID, Integer> categoryScrollOffset = new HashMap<>();
    private final Component title = MessageManager.getMessage("Menu.Unified.Title");
    private final List<VirtualCategory> virtualCategories;
    private final boolean showScrollUp;

    public MenuUnified(UltraCosmetics ultraCosmetics) {
        super("main", ultraCosmetics);
        this.virtualCategories = VirtualCategory.loadFromConfig();
        this.showScrollUp = SettingsManager.getConfig()
                .getBoolean("Categories.Unified-Menu.Show-Scroll-Up", true);
    }

    private boolean useVirtual() {
        return !virtualCategories.isEmpty();
    }

    public List<VirtualCategory> getVirtualCategories() {
        return virtualCategories;
    }

    @Override
    public void open(UltraPlayer player) {
        int page = activePage.getOrDefault(player.getUUID(), 1);
        if (useVirtual()) {
            openVirtual(player, activeVirtual.get(player.getUUID()), page);
        } else {
            open(player, activeCategory.get(player.getUUID()), page);
        }
    }

    /**
     * Opens the unified menu on the given built-in category and page, falling back to the
     * first category the player can see if {@code category} is null or no longer visible.
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
        activeVirtual.remove(player.getUUID());

        int maxPages = category == null ? 1 : getMaxPages(category, player);
        page = clampPage(page, maxPages);
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
        putFooter(inventory, player, category == null ? null : java.util.EnumSet.of(category), page, maxPages);
        fillInventory(inventory);
        if (reuse) {
            bukkitPlayer.updateInventory();
        } else {
            bukkitPlayer.openInventory(inventory);
        }
    }

    /**
     * Opens the unified menu on the given virtual category and page. Used when the
     * config defines {@code Categories.Unified-Menu.Virtual-Categories}.
     */
    public void openVirtual(UltraPlayer player, VirtualCategory category, int page) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (!bukkitPlayer.hasPermission(permission)) {
            CommandManager.sendNoPermissionMessage(bukkitPlayer);
            return;
        }

        if (category == null || !virtualCategories.contains(category)) {
            category = virtualCategories.isEmpty() ? null : virtualCategories.get(0);
        }
        activeVirtual.put(player.getUUID(), category);
        activeCategory.remove(player.getUUID());

        int maxPages = category == null ? 1 : getVirtualMaxPages(category, player);
        page = clampPage(page, maxPages);
        activePage.put(player.getUUID(), page);

        Inventory inventory = getReusableInventory(bukkitPlayer);
        boolean reuse = inventory != null;
        if (reuse) {
            clickRunnableMap.remove(inventory);
            inventory.clear();
        } else {
            inventory = createInventory(title);
        }
        putDescription(inventory);
        putVirtualCategorySelectors(inventory, player);
        if (category != null) {
            putVirtualContent(inventory, player, category, page);
        }
        putFooter(inventory, player, category == null ? null : category.getCoveredCategories(), page, maxPages);
        if (category != null) {
            putVirtualExtras(inventory, player, category);
        }
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

    private void putVirtualExtras(Inventory inventory, UltraPlayer player, VirtualCategory category) {
        for (Map.Entry<Integer, VirtualCategory.ExtraButton> entry : category.getExtras().entrySet()) {
            int slot = entry.getKey();
            switch (entry.getValue()) {
                case RENAME_PET:
                    if (!SettingsManager.getConfig().getBoolean("Pets-Rename.Enabled")) continue;
                    if (SettingsManager.getConfig().getBoolean("Pets-Rename.Permission-Required")
                            && !player.getBukkitPlayer().hasPermission("ultracosmetics.pets.rename")) continue;
                    putItem(inventory, slot, new RenamePetButton(ultraCosmetics), player);
                    break;
                case TOGGLE_GADGETS:
                    if (!SettingsManager.getConfig().getBoolean("Categories.Gadgets.Allow-Disable-Gadgets", true)) continue;
                    putItem(inventory, slot, new ToggleGadgetsButton(), player);
                    break;
                case TOGGLE_MORPH_SELF_VIEW:
                    putItem(inventory, slot, new ToggleMorphSelfViewButton(), player);
                    break;
                case CLEAR:
                    putItem(inventory, slot, new ClearCosmeticButton(category.getCoveredCategories()), player);
                    break;
            }
        }
    }

    @Override
    public void refresh(UltraPlayer player) {
        open(player);
    }

    public Category getActiveCategory(UltraPlayer player) {
        return activeCategory.get(player.getUUID());
    }

    public VirtualCategory getActiveVirtualCategory(UltraPlayer player) {
        return activeVirtual.get(player.getUUID());
    }

    public int getActivePage(UltraPlayer player) {
        return activePage.getOrDefault(player.getUUID(), 1);
    }

    public void scrollCategories(UltraPlayer player, int delta) {
        int current = categoryScrollOffset.getOrDefault(player.getUUID(), 0);
        categoryScrollOffset.put(player.getUUID(), current + delta);
        refresh(player);
    }

    /**
     * Advances the current page (positive) or goes back (negative) preserving whichever
     * category kind ({@link Category} or {@link VirtualCategory}) is active.
     */
    public void changePage(UltraPlayer player, int delta) {
        int page = getActivePage(player) + delta;
        if (useVirtual()) {
            openVirtual(player, activeVirtual.get(player.getUUID()), page);
        } else {
            open(player, activeCategory.get(player.getUUID()), page);
        }
    }

    public void cleanupPlayer(UUID uuid) {
        activeCategory.remove(uuid);
        activeVirtual.remove(uuid);
        activePage.remove(uuid);
        categoryScrollOffset.remove(uuid);
    }

    private int clampPage(int page, int maxPages) {
        if (page > maxPages) page = maxPages;
        if (page < 1) page = 1;
        return page;
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
        if (offset > 0 && showScrollUp) {
            putItem(inventory, SCROLL_UP_SLOT, new CategoryScrollButton(this, true), player);
        }
        if (offset < maxOffset) {
            putItem(inventory, SCROLL_DOWN_SLOT, new CategoryScrollButton(this, false), player);
        }
    }

    private void putVirtualCategorySelectors(Inventory inventory, UltraPlayer player) {
        for (VirtualCategory virtual : virtualCategories) {
            putItem(inventory, virtual.getSlot(), new VirtualCategorySelectorButton(this, virtual), player);
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
        int visible = 0;
        for (CosmeticType<?> type : CosmeticType.enabledOf(category)) {
            if (!ultraCosmetics.getMenus().getCategoryMenu(category).shouldHideItem(player, type)) {
                visible++;
            }
        }
        return Math.max(1, ((visible - 1) / ITEMS_PER_PAGE) + 1);
    }

    private int getVirtualMaxPages(VirtualCategory virtual, UltraPlayer player) {
        int visible = 0;
        for (CosmeticType<?> type : virtual.getCosmetics()) {
            if (!isHidden(player, type)) visible++;
        }
        return Math.max(1, ((visible - 1) / ITEMS_PER_PAGE) + 1);
    }

    private boolean isHidden(UltraPlayer player, CosmeticType<?> type) {
        return CosmeticActions.shouldHide(ultraCosmetics, player, type);
    }

    private void putContent(Inventory inventory, UltraPlayer player, Category category, int page) {
        if (category.isSuits()) {
            putSuitsContent(inventory, player, page);
        } else {
            putGenericContent(inventory, player, category, page);
        }
    }

    private void putGenericContent(Inventory inventory, UltraPlayer player, Category category, int page) {
        CosmeticMenu<?> categoryMenu = ultraCosmetics.getMenus().getCategoryMenu(category);
        List<CosmeticType<?>> visible = new ArrayList<>();
        for (CosmeticType<?> type : CosmeticType.enabledOf(category)) {
            if (!categoryMenu.shouldHideItem(player, type)) {
                visible.add(type);
            }
        }
        int start = ITEMS_PER_PAGE * (page - 1);
        for (int i = 0; i < ITEMS_PER_PAGE && start + i < visible.size(); i++) {
            CosmeticButton button = CosmeticButton.fromType(visible.get(start + i), player, ultraCosmetics);
            putItem(inventory, CONTENT_SLOTS[i], button, player);
        }
    }

    private void putVirtualContent(Inventory inventory, UltraPlayer player, VirtualCategory virtual, int page) {
        List<CosmeticType<?>> visible = new ArrayList<>();
        for (CosmeticType<?> type : virtual.getCosmetics()) {
            if (!isHidden(player, type)) visible.add(type);
        }
        int start = ITEMS_PER_PAGE * (page - 1);
        for (int i = 0; i < ITEMS_PER_PAGE && start + i < visible.size(); i++) {
            CosmeticButton button = CosmeticButton.fromType(visible.get(start + i), player, ultraCosmetics);
            putItem(inventory, CONTENT_SLOTS[i], button, player);
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

    private void putFooter(Inventory inventory, UltraPlayer player, java.util.Set<Category> clearTargets,
                           int page, int maxPages) {
        if (clearTargets != null && !clearTargets.isEmpty()) {
            if (page > 1) {
                putItem(inventory, PREV_PAGE_SLOT, new UnifiedPageButton(this, false), player);
            }
            if (page < maxPages) {
                putItem(inventory, NEXT_PAGE_SLOT, new UnifiedPageButton(this, true), player);
            }
            putItem(inventory, CLEAR_SLOT, new ClearCosmeticButton(clearTargets), player);
        }
        putItem(inventory, FILTER_SLOT, new FilterCosmeticsButton(), player);
        if (UltraCosmeticsData.get().areTreasureChestsEnabled()) {
            putItem(inventory, OPEN_CHEST_SLOT, new OpenChestButton(ultraCosmetics), player);
            putItem(inventory, KEYS_SLOT, new KeysButton(ultraCosmetics), player);
        }
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer player) {
        // Rendering is fully handled by open(UltraPlayer, Category, int) and
        // openVirtual(...); this override only exists to satisfy Menu's abstract
        // contract and is never invoked.
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
