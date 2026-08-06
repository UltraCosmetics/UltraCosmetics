package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.menus.MenuUnified;
import be.isach.ultracosmetics.permissions.PermissionManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.LazyTag;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A category tab on the left side of {@link MenuUnified}. Its item reflects whether
 * the category it represents is the one currently being shown to the viewer.
 */
public class CategorySelectorButton implements Button {
    private final MenuUnified menu;
    private final Category category;
    private final PermissionManager pm;
    private final XMaterial activeMaterial;
    private final XMaterial inactiveMaterial;

    public CategorySelectorButton(MenuUnified menu, Category category, UltraCosmetics ultraCosmetics) {
        this.menu = menu;
        this.category = category;
        this.pm = ultraCosmetics.getPermissionManager();
        this.activeMaterial = ItemFactory.getXMaterialFromConfig("Categories.Unified-Menu.Active-Category-Item");
        this.inactiveMaterial = ItemFactory.getXMaterialFromConfig("Categories.Unified-Menu.Inactive-Category-Item");
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        boolean active = menu.getActiveCategory(ultraPlayer) == category;
        ItemStack stack = (active ? activeMaterial : inactiveMaterial).parseItem();

        List<String> loreList = new ArrayList<>();
        loreList.add("");
        Player player = ultraPlayer.getBukkitPlayer();
        String lore = MessageManager.getLegacyMessage("Menu." + category.getConfigPath() + ".Button.Lore",
                TagResolver.resolver("unlocked", new LazyTag(() -> Component.text(OpenCosmeticMenuButton.calculateUnlocked(pm, category, player))))
        );
        loreList.addAll(Arrays.asList(lore.split("\n")));

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(MessageManager.getLegacyMessage("Menu." + category.getConfigPath() + ".Button.Name"));
        meta.setLore(loreList);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onClick(ClickData clickData) {
        menu.open(clickData.getClicker(), category, 1);
    }
}
