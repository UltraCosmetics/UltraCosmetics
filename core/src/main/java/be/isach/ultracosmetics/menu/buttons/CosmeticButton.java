package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.*;
import be.isach.ultracosmetics.menu.*;
import be.isach.ultracosmetics.menu.buttons.togglecosmetic.*;
import be.isach.ultracosmetics.permissions.PermissionManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class CosmeticButton implements Button {
    protected final UltraCosmetics ultraCosmetics;
    protected final PermissionManager pm;
    protected final CosmeticType<?> cosmeticType;
    private final int price;
    private final boolean ignoreTooltip;
    private final boolean allowPurchase = SettingsManager.getConfig().getBoolean("No-Permission.Allow-Purchase");
    private final boolean usePurchasePermissions = SettingsManager.getConfig().getBoolean("No-Permission.Use-Purchase-Permissions");
    private final Component noPermissionMessage = MessageManager.getMessage("No-Permission");
    private ItemStack stack = null;

    public static CosmeticButton fromType(CosmeticType<?> cosmeticType, UltraPlayer ultraPlayer, UltraCosmetics ultraCosmetics) {
        if (SettingsManager.getConfig().getBoolean("No-Permission.Custom-Item.enabled") && !ultraPlayer.canEquip(cosmeticType)) {
            return new CosmeticNoPermissionButton(ultraCosmetics, cosmeticType);
        }
        switch (cosmeticType.getCategory()) {
            case PETS:
                return new TogglePetCosmeticButton(ultraCosmetics, (PetType) cosmeticType);
            case GADGETS:
                return new ToggleGadgetCosmeticButton(ultraCosmetics, (GadgetType) cosmeticType);
            case MORPHS:
                return new ToggleMorphCosmeticButton(ultraCosmetics, (MorphType) cosmeticType);
            case EMOTES:
                return new ToggleEmoteCosmeticButton(ultraCosmetics, (EmoteType) cosmeticType);
        }
        return new ToggleCosmeticButton(ultraCosmetics, cosmeticType);
    }

    public CosmeticButton(UltraCosmetics ultraCosmetics, CosmeticType<?> cosmeticType, boolean ignoreTooltip) {
        this.ultraCosmetics = ultraCosmetics;
        pm = ultraCosmetics.getPermissionManager();
        this.cosmeticType = cosmeticType;
        this.price = SettingsManager.getConfig().getInt(cosmeticType.getConfigPath() + ".Purchase-Price");
        this.ignoreTooltip = ignoreTooltip;
    }

    @Override
    public void onClick(ClickData clickData) {
        boolean success = handleClick(clickData);
        if (success && UltraCosmeticsData.get().shouldCloseAfterSelect()) {
            clickData.getClicker().getBukkitPlayer().closeInventory();
        }
    }

    protected ItemStack generateDisplayItem(UltraPlayer ultraPlayer) {
        ItemStack stack = getBaseItem(ultraPlayer);
        addPurchaseLore(stack, ultraPlayer);
        return stack;
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        if (stack == null) {
            stack = generateDisplayItem(ultraPlayer);
        }
        return stack;
    }

    protected abstract ItemStack getBaseItem(UltraPlayer ultraPlayer);

    /**
     * Handles clicking on cosmetics in the GUI
     *
     * @param data The ClickData from the event
     * @return true if closing the inventory now is OK
     */
    protected boolean handleClick(ClickData data) {
        UltraPlayer ultraPlayer = data.getClicker();
        ItemStack clicked = data.getClicked();
        if (data.getClick().isRightClick()) {
            if (ultraPlayer.canEquip(cosmeticType)) {
                handleRightClick(data);
                return false;
            }
        }

        if (ignoreTooltip || startsWithColorless(clicked.getItemMeta().getDisplayName(), cosmeticType.getCategory().getActivateTooltip())) {
            if (ultraPlayer.canEquip(cosmeticType)) {
                cosmeticType.equip(ultraPlayer, ultraCosmetics);
                if (ultraPlayer.hasCosmetic(cosmeticType.getCategory())) {
                    return handleActivate(data);
                }
                return true;
            }
            if (!allowPurchase || price <= 0) {
                ultraPlayer.sendMessage(noPermissionMessage);
                return true;
            }
            if (usePurchasePermissions && !ultraPlayer.canPurchase(cosmeticType)) {
                ultraPlayer.sendMessage(noPermissionMessage);
                return true;
            }
            String itemName = MessageManager.getLegacyMessage("Buy-Cosmetic-Description",
                    Placeholder.unparsed("price", TextUtil.formatNumber(ultraCosmetics.getEconomyHandler().calculateDiscountPrice(ultraPlayer.getBukkitPlayer(), price))),
                    Placeholder.component("gadgetname", cosmeticType.getName())
            );
            ItemStack display = ItemFactory.rename(cosmeticType.getItemStack(), itemName);
            PurchaseData pd = new PurchaseData();
            pd.setBasePrice(price);
            pd.setShowcaseItem(display);
            pd.setOnPurchase(() -> {
                pm.setPermission(ultraPlayer, cosmeticType);
                // Delay by five ticks so the command processes
                // TODO: Remove this?
                ultraCosmetics.getScheduler().runAtEntityLater(ultraPlayer.getBukkitPlayer(), () -> {
                    cosmeticType.equip(ultraPlayer, ultraCosmetics);
                    data.getMenu().refresh(ultraPlayer);
                }, 5);
            });
            pd.setOnCancel(() -> data.getMenu().refresh(ultraPlayer));
            Component title = MessageManager.getMessage("Menu.Purchase-Cosmetic.Title",
                    Placeholder.component("cosmetic", cosmeticType.getName())
            );
            MenuPurchaseFactory mpFactory = ultraCosmetics.getMenus().getMenuPurchaseFactory();
            MenuPurchase mp = mpFactory.createPurchaseMenu(ultraCosmetics, title, pd);
            ultraPlayer.getBukkitPlayer().openInventory(mp.getInventory(ultraPlayer));
            return false; // We just opened another inventory, don't close it
        } else if (startsWithColorless(clicked.getItemMeta().getDisplayName(), cosmeticType.getCategory().getDeactivateTooltip())) {
            ultraPlayer.removeCosmetic(cosmeticType.getCategory());
            if (!UltraCosmeticsData.get().shouldCloseAfterSelect()) {
                data.getMenu().refresh(ultraPlayer);
            }
        }
        return true;
    }

    private void addPurchaseLore(ItemStack stack, UltraPlayer player) {
        if (price > 0 && !player.canEquip(cosmeticType) && allowPurchase) {
            if(player.canPurchase(cosmeticType)) {
                ItemMeta meta = stack.getItemMeta();
                List<String> lore = meta.getLore();
                lore.add("");
                int discountPrice = ultraCosmetics.getEconomyHandler().calculateDiscountPrice(player.getBukkitPlayer(), price);
                lore.add(MessageManager.getLegacyMessage("Click-To-Purchase", Placeholder.unparsed("price", String.valueOf(discountPrice))));
                meta.setLore(lore);
                stack.setItemMeta(meta);
            }
        }
    }

    protected boolean handleActivate(ClickData clickData) {
        if (!UltraCosmeticsData.get().shouldCloseAfterSelect()) {
            clickData.getMenu().refresh(clickData.getClicker());
        }
        return true;
    }

    protected void handleRightClick(ClickData clickData) {
    }

    protected boolean startsWithColorless(String a, Component b) {
        return ChatColor.stripColor(a).startsWith(PlainTextComponentSerializer.plainText().serialize(b));
    }
}
