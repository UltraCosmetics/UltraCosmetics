package be.isach.ultracosmetics.menu;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.mysql.MySqlConnectionManager;
import be.isach.ultracosmetics.player.UltraPlayer;

import java.util.Set;

/**
 * Cosmetic menu actions that aren't tied to a specific menu implementation.
 *
 * <p>The inventory-based menus drive these through {@link Button}s and {@link ClickData},
 * which carry an {@link org.bukkit.inventory.ItemStack} and a {@link Menu}. The Bedrock
 * form menu has neither, so anything both need to do lives here instead of being written
 * twice and drifting apart.
 *
 * <p><b>Threading:</b> every method that mutates player state assumes the caller is
 * already on the correct thread. Inventory clicks arrive on the main thread, but Cumulus
 * form responses arrive on a netty thread, so callers on the Bedrock side must wrap these
 * in {@code getScheduler().runAtEntity(...)}.
 */
public final class CosmeticActions {

    private CosmeticActions() {
    }

    /**
     * @return {@code true} if this exact cosmetic is the one currently equipped in its
     *         category.
     */
    public static boolean isEquipped(UltraPlayer player, CosmeticType<?> type) {
        Category category = type.getCategory();
        return player.hasCosmetic(category) && player.getCosmetic(category).getType() == type;
    }

    /**
     * Equips the cosmetic, or unequips it if it was already the active one in its category.
     * Does not check permissions — callers should gate on {@link UltraPlayer#canEquip}.
     *
     * @return {@code true} if the cosmetic is equipped after this call
     */
    public static boolean toggleCosmetic(UltraCosmetics ultraCosmetics, UltraPlayer player, CosmeticType<?> type) {
        Category category = type.getCategory();
        if (isEquipped(player, type)) {
            player.removeCosmetic(category);
            return false;
        }
        type.equip(player, ultraCosmetics);
        return player.hasCosmetic(category);
    }

    /**
     * Removes the equipped cosmetic of every given category, or everything the player has
     * equipped when {@code categories} is null or empty.
     *
     * <p>Suit categories are special-cased: clearing one suit slot clears all four, since
     * a suit is presented as a single cosmetic.
     */
    public static void clearCategories(UltraPlayer player, Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            player.clear();
            return;
        }
        for (Category category : categories) {
            if (category.isSuits()) {
                for (Category suitCategory : Category.values()) {
                    if (suitCategory.isSuits()) {
                        player.removeCosmetic(suitCategory);
                    }
                }
            } else {
                player.removeCosmetic(category);
            }
        }
    }

    /**
     * Flips the "only show cosmetics I own" filter. Callers are responsible for redrawing,
     * since this changes the menu layout.
     */
    public static void toggleFilter(UltraPlayer player) {
        player.setFilteringByOwned(!player.isFilteringByOwned());
    }

    /**
     * Shared pet name length validation, applying both the configured display limit and
     * the database column limit.
     *
     * @param name the raw name, possibly containing MiniMessage tags
     * @return {@code true} if the name is too long to accept
     */
    public static boolean isPetNameTooLong(String name) {
        String stripped = MessageManager.getMiniMessage().stripTags(name);
        int maxLength = SettingsManager.getConfig().getInt("Max-Pet-Name-Length", -1);
        return (maxLength != -1 && stripped.length() > maxLength)
                || name.length() > MySqlConnectionManager.MAX_NAME_SIZE;
    }

    /**
     * Whether a cosmetic should be hidden from the player, delegating to the owning
     * category menu so the per-menu {@code No-Permission.Dont-Show-Item} setting and the
     * player's own-only filter are both respected.
     *
     * <p>Exists because {@link CosmeticMenu#shouldHideItem} is an instance method, so
     * menus that don't extend {@link CosmeticMenu} can't reach it directly.
     */
    public static boolean shouldHide(UltraCosmetics ultraCosmetics, UltraPlayer player, CosmeticType<?> type) {
        CosmeticMenu<?> menu = ultraCosmetics.getMenus().getCategoryMenu(type.getCategory());
        return menu != null && menu.shouldHideItem(player, type);
    }
}
