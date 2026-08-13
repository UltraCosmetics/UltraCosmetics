package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.menu.CosmeticActions;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class FilterCosmeticsButton implements Button {
    public FilterCosmeticsButton() {

    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        Component filterItemName;
        if (ultraPlayer.isFilteringByOwned()) {
            filterItemName = MessageManager.getMessage("Disable-Filter-By-Owned");
        } else {
            filterItemName = MessageManager.getMessage("Enable-Filter-By-Owned");
        }
        return ItemFactory.create(XMaterial.HOPPER, filterItemName);
    }

    @Override
    public void onClick(ClickData clickData) {
        UltraPlayer player = clickData.getClicker();
        CosmeticActions.toggleFilter(player);
        // Refresh inventory completely because it changes the layout
        clickData.getMenu().refresh(player);
    }
}
