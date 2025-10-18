package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class KeysButton extends TreasureButton {
    private static final XMaterial KEY_ITEM;

    static {
        XMaterial chosen = XMaterial.TRIPWIRE_HOOK;
        if (XMaterial.TRIAL_KEY.isSupported()) {
            try {
                if (XMaterial.TRIAL_KEY.get().isEnabledByFeature(Bukkit.getWorlds().get(0))) {
                    chosen = XMaterial.TRIAL_KEY;
                }
            } catch (NoSuchMethodError error) {
                // Must be after paper removed isEnabledByFeature so it's fine
                chosen = XMaterial.TRIAL_KEY;
            }
        }
        KEY_ITEM = chosen;
    }

    private final String itemName = MessageManager.getLegacyMessage("Treasure-Keys");
    private final UltraCosmetics ultraCosmetics;
    private final XSound.SoundPlayer noKeysSound;

    public KeysButton(UltraCosmetics ultraCosmetics) {
        super(ultraCosmetics);
        this.ultraCosmetics = ultraCosmetics;
        noKeysSound = XSound.BLOCK_ANVIL_LAND.record().withVolume(0.2f).withPitch(1.2f).soundPlayer();
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        Component yourKeysMessage = MessageManager.getMessage("Your-Keys",
                Placeholder.unparsed("keys", String.valueOf(ultraPlayer.getKeys()))
        );
        return ItemFactory.create(KEY_ITEM, itemName, "", MessageManager.toLegacy(yourKeysMessage), buyKeyMessage);
    }

    @Override
    public void onClick(ClickData clickData) {
        UltraPlayer clicker = clickData.getClicker();
        if (!canBuyKeys) {
            noKeysSound.forPlayers(clicker.getBukkitPlayer());
            return;
        }
        clicker.getBukkitPlayer().closeInventory();
        ultraCosmetics.getMenus().openKeyPurchaseMenu(clicker);
    }
}
