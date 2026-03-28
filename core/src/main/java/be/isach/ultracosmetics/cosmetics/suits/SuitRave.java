package be.isach.ultracosmetics.cosmetics.suits;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 * Represents an instance of a rave suit summoned by a player.
 *
 * @author iSach
 * @since 12-20-2015
 */
public class SuitRave extends Suit implements Updatable {
    private final int[] colors = new int[] {255, 0, 0};
    private final int updateInterval = SettingsManager.getConfig().getInt(getOptionPath("Update-Delay-In-Creative"), 1);
    private int tick = 0;

    public SuitRave(UltraPlayer owner, SuitType suitType, UltraCosmetics ultraCosmetics) {
        super(owner, suitType, ultraCosmetics);
    }

    @Override
    public void onEquip() {
        // If the player has another rave suit part equipped already,
        // sync up the update timers.
        for (ArmorSlot slot : ArmorSlot.values()) {
            if (slot == getArmorSlot()) continue;
            Suit part = getOwner().getCurrentSuit(slot);
            if (part instanceof SuitRave) {
                tick = ((SuitRave) part).getTick();
                break;
            }
        }
    }

    @Override
    public void onUpdate() {
        if (colors[0] == 255 && colors[1] < 255 && colors[2] == 0) colors[1] += 15;
        if (colors[1] == 255 && colors[0] > 0 && colors[2] == 0) colors[0] -= 15;
        if (colors[1] == 255 && colors[2] < 255 && colors[0] == 0) colors[2] += 15;
        if (colors[2] == 255 && colors[1] > 0 && colors[0] == 0) colors[1] -= 15;
        if (colors[2] == 255 && colors[0] < 255 && colors[1] == 0) colors[0] += 15;
        if (colors[0] == 255 && colors[2] > 0 && colors[1] == 0) colors[2] -= 15;

        refresh();
    }

    private void refresh() {
        if (getPlayer().getGameMode() == GameMode.CREATIVE && ++tick < updateInterval) return;
        tick = 0;
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) getItemStack().getItemMeta();
        itemMeta.setColor(Color.fromRGB(colors[0], colors[1], colors[2]));
        getItemStack().setItemMeta(itemMeta);
        updateArmorItem();
    }

    public int getTick() {
        return tick;
    }
}
