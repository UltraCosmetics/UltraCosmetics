package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.CosmeticActions;
import be.isach.ultracosmetics.menu.Menu;
import be.isach.ultracosmetics.menu.MenuPurchase;
import be.isach.ultracosmetics.menu.MenuPurchaseFactory;
import be.isach.ultracosmetics.menu.PurchaseData;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.TextUtil;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class RenamePetButton implements Button {
    private final Component activePetNeeded = MessageManager.getMessage("Active-Pet-Needed");
    private final ItemStack stack = ItemFactory.getItemStackFromConfig("Categories.Rename-Pet-Item");
    private final UltraCosmetics ultraCosmetics;

    public RenamePetButton(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        if (ultraPlayer.getCurrentPet() == null) {
            return ItemFactory.rename(this.stack.clone(), activePetNeeded);
        }
        Component name = MessageManager.getMessage("Menu.Rename-Pet.Button.Name",
                Placeholder.component("petname", ultraPlayer.getCurrentPet().getTypeName())
        );
        return ItemFactory.rename(this.stack.clone(), name);
    }

    @Override
    public void onClick(ClickData clickData) {
        UltraPlayer player = clickData.getClicker();
        if (player.getCurrentPet() == null) {
            player.sendMessage(activePetNeeded);
            player.getBukkitPlayer().closeInventory();
            return;
        }
        renamePet(ultraCosmetics, player, clickData.getMenu());
    }

    public static void renamePet(UltraCosmetics ultraCosmetics, final UltraPlayer ultraPlayer, Menu returnMenu) {
        String oldName = ultraPlayer.getProfile().getPetName(ultraPlayer.getCurrentPet().getType());
        if (oldName == null) {
            oldName = MessageManager.getLegacyMessage("Menu.Rename-Pet.Placeholder");
        }
        try {
            new AnvilGUI.Builder().plugin(ultraCosmetics)
                    .itemLeft(XMaterial.PAPER.parseItem())
                    .text(oldName)
                    .title(MessageManager.getLegacyMessage("Menu.Rename-Pet.Title"))
                    .onClick((slot, state) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();
                        String text = state.getText();
                        if (CosmeticActions.isPetNameTooLong(text)) {
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(MessageManager.getLegacyMessage("Too-Long")));
                        }
                        if (!text.isEmpty() && ultraCosmetics.getEconomyHandler().isUsingEconomy()
                                && SettingsManager.getConfig().getBoolean("Pets-Rename.Requires-Money.Enabled")) {
                            return Collections.singletonList(AnvilGUI.ResponseAction.openInventory(buyRenamePet(ultraPlayer, text, returnMenu)));
                        } else {
                            ultraPlayer.setPetName(ultraPlayer.getCurrentPet().getType(), text);
                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        }
                    }).open(ultraPlayer.getBukkitPlayer());
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            ultraPlayer.sendMessage(MessageManager.getMessage("Use-Rename-Pet-Command"));
        }
    }

    public static Inventory buyRenamePet(UltraPlayer ultraPlayer, final String name, Menu returnMenu) {
        int price = SettingsManager.getConfig().getInt("Pets-Rename.Requires-Money.Price");
        int discountPrice = UltraCosmeticsData.get().getPlugin().getEconomyHandler().calculateDiscountPrice(ultraPlayer.getBukkitPlayer(), price);
        Component renameTitle = MessageManager.getMessage("Menu.Purchase-Rename.Button.Showcase",
                Placeholder.unparsed("price", TextUtil.formatNumber(discountPrice)),
                Placeholder.component("name", MessageManager.getMiniMessage().deserialize(name)));
        ItemStack showcaseItem = ItemFactory.create(XMaterial.NAME_TAG, renameTitle);

        PurchaseData purchaseData = new PurchaseData();
        purchaseData.setBasePrice(price);
        purchaseData.setShowcaseItem(showcaseItem);
        purchaseData.setOnPurchase(() -> {
            ultraPlayer.setPetName(ultraPlayer.getCurrentPet().getType(), name);
            if (returnMenu != null) {
                returnMenu.open(ultraPlayer);
            }
        });
        if (returnMenu != null) {
            purchaseData.setOnCancel(() -> returnMenu.open(ultraPlayer));
        }

        MenuPurchaseFactory mpFactory = UltraCosmeticsData.get().getPlugin().getMenus().getMenuPurchaseFactory();
        MenuPurchase menu = mpFactory.createPurchaseMenu(UltraCosmeticsData.get().getPlugin(), MessageManager.getMessage("Menu.Purchase-Rename.Title"), purchaseData);
        return menu.getInventory(ultraPlayer);
    }
}
