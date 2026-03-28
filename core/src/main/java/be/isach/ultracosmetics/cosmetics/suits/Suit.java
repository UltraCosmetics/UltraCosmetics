package be.isach.ultracosmetics.cosmetics.suits;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.ArmorCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

/**
 * Represents an instance of a suit summoned by a player.
 *
 * @author iSach
 * @since 12-20-2015
 */
public class Suit extends ArmorCosmetic<SuitType> {
    protected boolean fullSuitEquipped = false;

    public Suit(UltraPlayer ultraPlayer, SuitType suitType, UltraCosmetics ultraCosmetics) {
        super(ultraPlayer, suitType, ultraCosmetics);
        setupItemStack();
    }

    protected boolean isFullSuit() {
        for (ArmorSlot slot : ArmorSlot.values()) {
            Suit part = getOwner().getCurrentSuit(slot);
            if (part == null || part.getType().getSuitCategory() != getType().getSuitCategory()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        if (getPlayer() == null || getOwner().getCurrentSuit(getArmorSlot()) != this) {
            return;
        }
        if (getArmorSlot() == ArmorSlot.CHESTPLATE) {
            boolean full = isFullSuit();
            if (!fullSuitEquipped && full) {
                chestplateOnFullSuitEquipped();
            } else if (fullSuitEquipped && !full) {
                chestplateOnFullSuitUnequipped();
            }
            fullSuitEquipped = full;
        }
        ((Updatable) this).onUpdate();
    }

    protected void setupItemStack() {
        setItemStack(ItemFactory.rename(getType().getItemStack(), getTypeName(), "", MessageManager.getLegacyMessage("Suits.Suit-Part-Lore")));
    }

    @Override
    protected void onEquip() {
    }

    /**
     * Called on the first update when the entire suit is equipped. Dispatched only to the chestplate.
     * Suit must implement {@code Updatable} for this to be detected.
     */
    protected void chestplateOnFullSuitEquipped() {
    }

    /**
     * Called when a suit piece is removed after the whole suit has been equipped.
     */
    protected void chestplateOnFullSuitUnequipped() {
    }

    protected void updateMeta(Consumer<ItemMeta> func) {
        ItemMeta meta = getItemStack().getItemMeta();
        func.accept(meta);
        getItemStack().setItemMeta(meta);
        updateArmorItem();
    }

    /**
     * Get Suit Armor Slot.
     *
     * @return Suit Armor Slot.
     */
    @Override
    public ArmorSlot getArmorSlot() {
        return cosmeticType.getSlot();
    }

    @Override
    public String getOccupiedSlotKey() {
        return "Must-Remove.Suits." + getArmorSlot().toString();
    }

    @Override
    protected String getOptionPath(String key) {
        return getType().getSuitCategory().getConfigPath() + "." + key;
    }
}
