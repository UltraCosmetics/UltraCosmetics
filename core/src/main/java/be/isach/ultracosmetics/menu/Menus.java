package be.isach.ultracosmetics.menu;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.events.UCKeyPurchaseEvent;
import be.isach.ultracosmetics.menu.buttons.KeysButton;
import be.isach.ultracosmetics.menu.menus.*;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.TextUtil;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores menus.
 *
 * @author iSach
 * @since 08-23-2016
 */
public class Menus {

    private final UltraCosmetics ultraCosmetics;
    private final Map<Category, CosmeticMenu<?>> categoryMenus = new HashMap<>();
    private Menu mainMenu;
    private MenuPurchaseFactory menuPurchaseFactory = StandardMenuPurchase::new;

    public Menus(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        categoryMenus.put(Category.EMOTES, new MenuEmotes(ultraCosmetics));
        categoryMenus.put(Category.GADGETS, new MenuGadgets(ultraCosmetics));
        categoryMenus.put(Category.EFFECTS, new MenuParticleEffects(ultraCosmetics));
        categoryMenus.put(Category.HATS, new MenuHats(ultraCosmetics));
        categoryMenus.put(Category.MORPHS, new MenuMorphs(ultraCosmetics));
        categoryMenus.put(Category.MOUNTS, new MenuMounts(ultraCosmetics));
        categoryMenus.put(Category.PETS, new MenuPets(ultraCosmetics));
        categoryMenus.put(Category.PROJECTILE_EFFECTS, new MenuProjectileEffects(ultraCosmetics));
        categoryMenus.put(Category.DEATH_EFFECTS, new MenuDeathEffects(ultraCosmetics));
        MenuSuits ms = new MenuSuits(ultraCosmetics);
        categoryMenus.put(Category.SUITS_HELMET, ms);
        categoryMenus.put(Category.SUITS_CHESTPLATE, ms);
        categoryMenus.put(Category.SUITS_LEGGINGS, ms);
        categoryMenus.put(Category.SUITS_BOOTS, ms);
        this.mainMenu = new MenuMain(ultraCosmetics);
        // Load the class so it's available on disable, when we can't load more classes.
        // Otherwise sometimes errors occur when hotswapping the jar
        new CosmeticsInventoryHolder();
    }

    public Menu getMainMenu() {
        return mainMenu;
    }

    public void setMainMenu(Menu menu) {
        this.mainMenu = menu;
    }

    /**
     * Opens UC's main menu OR runs the custom main menu command specified in config.yml
     *
     * @param ultraPlayer The player to show the menu to
     */
    public void openMainMenu(UltraPlayer ultraPlayer) {
        if (ultraCosmetics.getConfig().getBoolean("Categories.Back-To-Main-Menu-Custom-Command.Enabled")) {
            String command = ultraCosmetics.getConfig().getString("Categories.Back-To-Main-Menu-Custom-Command.Command").replace("/", "").replace("{player}", ultraPlayer.getBukkitPlayer().getName()).replace("{playeruuid}", ultraPlayer.getUUID().toString());
            Bukkit.dispatchCommand(ultraCosmetics.getServer().getConsoleSender(), command);
            return;
        }
        mainMenu.open(ultraPlayer);
    }

    public CosmeticMenu<?> getCategoryMenu(Category category) {
        return categoryMenus.get(category);
    }

    public void setCategoryMenu(Category category, CosmeticMenu<?> menu) {
        categoryMenus.put(category, menu);
    }

    /**
     * Opens Ammo Purchase Menu.
     */
    public void openAmmoPurchaseMenu(GadgetType type, UltraPlayer player, Runnable menuReturnFunc) {
        int price = ultraCosmetics.getEconomyHandler().calculateDiscountPrice(player.getBukkitPlayer(), type.getAmmoPrice());
        String itemName = MessageManager.getLegacyMessage("Buy-Ammo-Description",
                Placeholder.unparsed("amount", String.valueOf(type.getResultAmmoAmount())),
                Placeholder.unparsed("price", TextUtil.formatNumber(price)),
                Placeholder.component("gadgetname", type.getName())
        );
        ItemStack display = ItemFactory.create(type.getMaterial(), itemName);
        PurchaseData pd = new PurchaseData();
        pd.setBasePrice(type.getAmmoPrice());
        pd.setShowcaseItem(display);
        pd.setOnPurchase(() -> {
            player.addAmmo(type, type.getResultAmmoAmount());
            menuReturnFunc.run();
        });
        pd.setOnCancel(menuReturnFunc);
        MenuPurchase mp = menuPurchaseFactory.createPurchaseMenu(ultraCosmetics, MessageManager.getMessage("Menu.Buy-Ammo.Title"), pd);
        player.getBukkitPlayer().openInventory(mp.getInventory(player));
    }

    public MenuPurchaseFactory getMenuPurchaseFactory() {
        return menuPurchaseFactory;
    }

    public void setMenuPurchaseFactory(MenuPurchaseFactory factory) {
        this.menuPurchaseFactory = factory;
    }

    /**
     * Opens the Key Purchase Menu.
     */
    public void openKeyPurchaseMenu(UltraPlayer ultraPlayer) {
        if (!ultraCosmetics.getEconomyHandler().isUsingEconomy()) return;
        Player player = ultraPlayer.getBukkitPlayer();

        int price = SettingsManager.getConfig().getInt("TreasureChests.Key-Price");
        if (price < 1) return;

        if (!player.hasPermission("ultracosmetics.treasurechests.buykey")) {
            MessageManager.send(player, "No-Buy-Key-Permission");
            return;
        }
        int discountPrice = ultraCosmetics.getEconomyHandler().calculateDiscountPrice(player, price);
        TagResolver.Single pricePlaceholder = Placeholder.unparsed("price", TextUtil.formatNumber(discountPrice));
        ItemStack itemStack = ItemFactory.create(KeysButton.getKeyItem(), MessageManager.getLegacyMessage("Buy-Treasure-Key-ItemName", pricePlaceholder));

        PurchaseData pd = new PurchaseData();
        pd.setBasePrice(discountPrice);
        pd.setShowcaseItem(itemStack);
        pd.setCanPurchase(() -> {
            UCKeyPurchaseEvent event = new UCKeyPurchaseEvent(ultraPlayer, discountPrice);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            pd.setBasePrice(event.getPrice());
            return true;
        });
        Menus menus = ultraCosmetics.getMenus();
        pd.setOnPurchase(() -> {
            ultraPlayer.addKey();
            menus.openMainMenu(ultraPlayer);
        });
        pd.setOnCancel(() -> menus.openMainMenu(ultraPlayer));
        MenuPurchase mp = menus.getMenuPurchaseFactory().createPurchaseMenu(ultraCosmetics, MessageManager.getMessage("Buy-Treasure-Key"), pd);
        ultraCosmetics.getScheduler().runAtEntityLater(player, () -> player.openInventory(mp.getInventory(ultraPlayer)), 1);
    }
}
