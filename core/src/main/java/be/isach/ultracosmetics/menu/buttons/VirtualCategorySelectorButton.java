package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.VirtualCategory;
import be.isach.ultracosmetics.menu.menus.MenuUnified;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A category tab in {@link MenuUnified} backed by a config-defined {@link VirtualCategory}.
 * Its item reflects whether the virtual category it represents is the one currently shown.
 */
public class VirtualCategorySelectorButton implements Button {
    private final MenuUnified menu;
    private final VirtualCategory category;
    private final XMaterial activeMaterial;
    private final XMaterial inactiveMaterial;

    public VirtualCategorySelectorButton(MenuUnified menu, VirtualCategory category) {
        this.menu = menu;
        this.category = category;
        this.activeMaterial = ItemFactory.getXMaterialFromConfig("Categories.Unified-Menu.Active-Category-Item");
        XMaterial override = category.getItemOverride();
        this.inactiveMaterial = override != null ? override
                : ItemFactory.getXMaterialFromConfig("Categories.Unified-Menu.Inactive-Category-Item");
    }

    public VirtualCategory getCategory() {
        return category;
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        boolean active = menu.getActiveVirtualCategory(ultraPlayer) == category;
        ItemStack stack = (active ? activeMaterial : inactiveMaterial).parseItem();
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(renderName(category.getDisplayName()));
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Accepts both MiniMessage tags ({@code <gray>foo</gray>}) and legacy ampersand codes
     * ({@code &7foo}). Detects MiniMessage by the presence of a {@code <...>} tag; falls
     * back to legacy otherwise.
     */
    public static String renderName(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        if (raw.indexOf('<') >= 0 && raw.indexOf('>') > raw.indexOf('<')) {
            Component component = MessageManager.getMiniMessage().deserialize(raw);
            return MessageManager.toLegacy(component);
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    @Override
    public void onClick(ClickData clickData) {
        menu.openVirtual(clickData.getClicker(), category, 1);
    }
}
