package be.isach.ultracosmetics.cosmetics;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.suits.ArmorSlot;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ArmorCosmetic<T extends CosmeticType<?>> extends Cosmetic<T> {
    protected final Map<Attribute, Double> attributes;
    protected ItemStack itemStack;

    public ArmorCosmetic(UltraPlayer owner, T type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        attributes = getAttributes();
    }

    @Override
    public void clear() {
        super.clear();
        setArmorItem(null);
    }

    @Override
    protected boolean tryEquip() {
        return trySetSlot();
    }

    protected boolean trySetSlot() {
        // Remove current equipped armor piece
        getOwner().removeCosmetic(Category.suitsFromSlot(getArmorSlot()));

        if (getArmorSlot() == ArmorSlot.HELMET) {
            getOwner().removeCosmetic(Category.HATS);
            getOwner().removeCosmetic(Category.EMOTES);
        }

        // If the user's armor slot is still occupied after we've removed all related cosmetics,
        // give up and ask the user to free up the slot.
        if (getArmorItem() != null && getArmorItem().getType() != Material.AIR) {
            getOwner().sendMessage(MessageManager.getMessage(getOccupiedSlotKey()));
            return false;
        }
        if (itemStack != null) {
            writeAttributes(itemStack);
            ItemFactory.applyCosmeticMarker(itemStack);
            setArmorItem(itemStack);
        }
        return true;
    }

    protected void writeAttributes(ItemStack stack) {
        if (attributes.isEmpty()) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasAttributeModifiers()) {
            meta.removeAttributeModifier(getArmorSlot().toBukkit());
        }
        int count = 0;
        for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
            AttributeModifier mod = ItemFactory.createAttributeModifier("custom_mod_" + count++,
                    entry.getValue(), AttributeModifier.Operation.ADD_NUMBER, getArmorSlot().toBukkit());
            meta.addAttributeModifier(entry.getKey(), mod);
        }
        stack.setItemMeta(meta);
    }

    private Map<Attribute, Double> getAttributes() {
        Map<Attribute, Double> attrs = new HashMap<>();
        // Category defaults
        loadAttributes(attrs, SettingsManager.getConfig().getConfigurationSection("Attribute-Bonus." + getCategory().toString()));
        // By loading this one second, any values found here will override the ones present in the defaults section
        loadAttributes(attrs, SettingsManager.getConfig().getConfigurationSection(getOptionPath("Attribute-Bonus")));
        return attrs;
    }

    private void loadAttributes(Map<Attribute, Double> attrs, ConfigurationSection section) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                Optional<XAttribute> attr = XAttribute.of(key);
                if (attr.isEmpty() || !attr.get().isSupported()) {
                    continue;
                }
                attrs.put(attr.get().get(), section.getDouble(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    protected ItemStack getArmorItem() {
        switch (getArmorSlot()) {
            case BOOTS:
                return getPlayer().getInventory().getBoots();
            case LEGGINGS:
                return getPlayer().getInventory().getLeggings();
            case CHESTPLATE:
                return getPlayer().getInventory().getChestplate();
            case HELMET:
                return getPlayer().getInventory().getHelmet();
            default:
                return null;
        }
    }

    protected void setArmorItem(ItemStack item) {
        switch (getArmorSlot()) {
            case BOOTS:
                getPlayer().getInventory().setBoots(item);
                break;
            case LEGGINGS:
                getPlayer().getInventory().setLeggings(item);
                break;
            case CHESTPLATE:
                getPlayer().getInventory().setChestplate(item);
                break;
            case HELMET:
                getPlayer().getInventory().setHelmet(item);
                break;
        }
    }

    /**
     * Returns the ArmorCosmetic's itemstack.
     *
     * @return the ArmorCosmetic's itemstack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        handleClick(event);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer() == getPlayer() && isItemThis(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
            handleDrop();
        }
    }

    // Prevent 1.19.4 armor item switching
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != getPlayer() || !event.hasItem()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (canSwapToOurSlot(event.getItem())) {
            event.setUseItemInHand(Event.Result.DENY);
        }
        if (isItemThis(event.getItem())) {
            event.setUseItemInHand(Event.Result.DENY);
            clear();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean canSwapToOurSlot(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        Material type = item.getType();
        if (type.name().endsWith("_" + getArmorSlot())) {
            return true;
        }
        EquipmentSlot slot = getArmorSlot().toBukkit();
        if (type == XMaterial.ELYTRA.get() && slot == EquipmentSlot.CHEST) {
            return true;
        }
        try {
            EquippableComponent equip = item.getItemMeta().getEquippable();
            return equip.isSwappable() && equip.getSlot() == slot;
        } catch (NoSuchMethodError ignored) {
            return false;
        }
    }

    private void handleDrop() {
        if (SettingsManager.getConfig().getBoolean("Remove-Gadget-With-Drop")) {
            clear();
        }
    }

    private void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        // The cursor check here is vital, otherwise the item stays in the helmet slot
        // but gets duplicated elsewhere in the inventory.
        if (player == getPlayer() && (isItemThis(current) || isItemThis(event.getCursor()))) {
            event.setCancelled(true);
            // player.updateInventory();
            if (event.getAction().name().contains("DROP")) {
                handleDrop();
            }
            if (player.getGameMode() == GameMode.CREATIVE) {
                player.closeInventory();
            }
        }
    }

    protected boolean isItemThis(ItemStack is) {
        return is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName()
                && is.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName());
    }

    protected abstract ArmorSlot getArmorSlot();

    protected String getOccupiedSlotKey() {
        return "Must-Remove." + getCategory().getConfigPath();
    }
}
