package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.suits.ArmorSlot;
import be.isach.ultracosmetics.cosmetics.suits.Suit;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class SuitType extends CosmeticType<Suit> {

    private final ArmorSlot slot;
    private final SuitCategory category;
    private final ItemStack item;

    protected SuitType(ItemStack stack, ArmorSlot slot, SuitCategory category) {
        super(Category.suitsFromSlot(slot), category.getConfigName(), XMaterial.matchXMaterial(stack),
                category.getSuitClass(), false);
        this.slot = slot;
        this.category = category;
        this.item = stack;
        // delay permission registration until we've loaded slot and category fields
        registerPermission();
        category.setupConfig(SettingsManager.getConfig(), getConfigPath());
    }

    @Override
    public Component getName() {
        return MessageManager.getMessage(
                "Suits." + getConfigName() + "." + slot.toString().toLowerCase(Locale.ROOT) + "-name");
    }

    @Override
    protected String getPermissionSuffix() {
        return category.getConfigName().toLowerCase(Locale.ROOT) + "." + slot.toString().toLowerCase(Locale.ROOT);
    }

    public ArmorSlot getSlot() {
        return slot;
    }

    public SuitCategory getSuitCategory() {
        return category;
    }

    @Override
    public ItemStack getItemStack() {
        return item.clone();
    }
}
