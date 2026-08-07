package be.isach.ultracosmetics.menu;

import org.bukkit.inventory.ItemStack;

import java.util.function.BooleanSupplier;

public class PurchaseData {

    private int price;
    private BooleanSupplier canPurchase;
    private Runnable onPurchase;
    private Runnable onCancel;
    private ItemStack showcaseItem;

    public int getBasePrice() {
        return price;
    }

    public ItemStack getShowcaseItem() {
        return showcaseItem;
    }

    public boolean canPurchase() {
        if (canPurchase != null) {
            return canPurchase.getAsBoolean();
        }
        return true;
    }

    public void runOnPurchase() {
        onPurchase.run();
    }

    public void runOnCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    public void setBasePrice(int price) {
        this.price = price;
    }

    public void setCanPurchase(BooleanSupplier canPurchase) {
        this.canPurchase = canPurchase;
    }

    public void setOnPurchase(Runnable onPurchase) {
        this.onPurchase = onPurchase;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setShowcaseItem(ItemStack showcaseItem) {
        this.showcaseItem = showcaseItem;
    }

}
