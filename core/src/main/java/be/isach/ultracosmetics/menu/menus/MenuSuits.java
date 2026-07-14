package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.SuitCategory;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.menu.buttons.EquipWholeSuitButton;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suit {@link be.isach.ultracosmetics.menu.Menu Menu}.
 *
 * @author iSach
 * @since 08-23-2016
 */
public final class MenuSuits extends CosmeticMenu<SuitType> {

    private static final int[] SLOTS = new int[] {10, 11, 12, 13, 14, 15, 16};

    public MenuSuits(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.SUITS_HELMET);
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer player, int page) {
        int from = (page - 1) * getItemsPerPage();
        int to = page * getItemsPerPage();
        List<SuitCategory> enabled = SuitCategory.enabled();
        for (int i = from; i < to && i < enabled.size(); i++) {
            SuitCategory cat = enabled.get(i);
            if (!hideNoPermissionItems || cat.getPieces().stream().anyMatch(player::canEquip)) {
                putItem(inventory, SLOTS[i % getItemsPerPage()] - 9, new EquipWholeSuitButton(cat, ultraCosmetics), player);
            }
        }
    }

    @Override
    public Map<Integer, SuitType> getSlots(int page, UltraPlayer player) {
        int from = (page - 1) * getItemsPerPage();
        int to = page * getItemsPerPage();
        Map<Integer, SuitType> slots = new HashMap<>();
        List<SuitCategory> enabled = SuitCategory.enabled();
        for (int i = from; i < to && i < enabled.size(); i++) {
            SuitCategory cat = enabled.get(i);
            int row = 0;
            // always in order of: helmet, chestplate, leggings, boots.
            // places the suit parts in columns
            for (SuitType type : cat.getPieces()) {
                slots.put(SLOTS[i % getItemsPerPage()] + row++ * 9, type);
            }
        }
        return slots;
    }

    @Override
    public int getItemsPerPage() {
        return 7;
    }

    @Override
    public int getMaxPages(UltraPlayer player) {
        int i = 0;
        for (SuitCategory cat : SuitCategory.enabled()) {
            if (!shouldHideItem(player, cat.getHelmet())
                    || !shouldHideItem(player, cat.getChestplate())
                    || !shouldHideItem(player, cat.getLeggings())
                    || !shouldHideItem(player, cat.getBoots())) {
                i++;
            }
        }
        return Math.max(1, ((i - 1) / getItemsPerPage()) + 1);
    }

    @Override
    public boolean hasUnlockable(UltraPlayer player) {
        for (SuitCategory cat : SuitCategory.enabled()) {
            if (!player.canEquip(cat.getHelmet())
                    || !player.canEquip(cat.getChestplate())
                    || !player.canEquip(cat.getLeggings())
                    || !player.canEquip(cat.getBoots())) {
                return true;
            }
        }
        return false;
    }
}
