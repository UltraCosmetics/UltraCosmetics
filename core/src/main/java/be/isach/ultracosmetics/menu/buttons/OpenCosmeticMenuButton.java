package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.SuitCategory;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.Menus;
import be.isach.ultracosmetics.permissions.PermissionManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.LazyTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenCosmeticMenuButton implements Button {
    private final UltraCosmetics ultraCosmetics;
    protected final Category category;
    protected final PermissionManager pm;
    protected final Menus menus;
    protected final ItemStack baseStack;
    protected final Permission openPermission;

    public OpenCosmeticMenuButton(UltraCosmetics ultraCosmetics, Category category) {
        this.ultraCosmetics = ultraCosmetics;
        this.category = category;
        this.pm = ultraCosmetics.getPermissionManager();
        this.menus = ultraCosmetics.getMenus();
        this.openPermission = menus.getCategoryMenu(category).getPermission();
        this.baseStack = category.getItemStack();
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        ItemStack stack = baseStack.clone();
        List<String> loreList = new ArrayList<>();
        loreList.add("");
        Player player = ultraPlayer.getBukkitPlayer();
        if (!category.isEnabled()) {
            loreList.add(MessageManager.getLegacyMessage("Menu.Disabled-Button"));
        } else if (!player.hasPermission(openPermission)) {
            loreList.add(MessageManager.getLegacyMessage("No-Permission"));
        } else {
            String lore = MessageManager.getLegacyMessage("Menu." + category.getConfigPath() + ".Button.Lore",
                    TagResolver.resolver("unlocked", new LazyTag(() -> Component.text(calculateUnlocked(player))))
            );
            loreList.addAll(Arrays.asList(lore.split("\n")));
        }

        ItemMeta meta = stack.getItemMeta();
        meta.setLore(loreList);
        stack.setItemMeta(meta);
        return stack;
    }

    protected String calculateUnlocked(Player player) {
        int unlocked = 0;
        int total;
        if (category.isSuits()) {
            for (Category cat : Category.enabled()) {
                if (!cat.isSuits()) continue;
                unlocked += pm.getEnabledUnlocked(player, cat).size();
            }
            total = SuitCategory.enabled().size() * 4;
        } else {
            unlocked = pm.getEnabledUnlocked(player, category).size();
            total = category.getEnabled().size();
        }
        return unlocked + "/" + total;
    }

    @Override
    public void onClick(ClickData clickData) {
        if (category.isEnabled() && clickData.getClicker().getBukkitPlayer().hasPermission(openPermission)) {
            menus.getCategoryMenu(category).open(clickData.getClicker());
        }
    }
}
