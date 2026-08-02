package be.isach.ultracosmetics.menu.menus;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.menu.CosmeticMenu;
import be.isach.ultracosmetics.menu.buttons.RenamePetButton;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.inventory.Inventory;

public class MenuPets extends CosmeticMenu<PetType> {

    public MenuPets(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics, Category.PETS);
    }

    @Override
    protected void putItems(Inventory inventory, UltraPlayer ultraPlayer, int page) {
        addPetRenameItem(inventory, ultraPlayer);
    }

    private void addPetRenameItem(Inventory inventory, UltraPlayer player) {
        if (!SettingsManager.getConfig().getBoolean("Pets-Rename.Enabled")) {
            return;
        }
        if (SettingsManager.getConfig().getBoolean("Pets-Rename.Permission-Required")
                && !player.getBukkitPlayer().hasPermission("ultracosmetics.pets.rename")) {
            return;
        }
        int slot = inventory.getSize() - (getCategory().hasGoBackArrow() ? 4 : 6);
        putItem(inventory, slot, new RenamePetButton(ultraCosmetics), player);
    }
}
