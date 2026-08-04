package be.isach.ultracosmetics.hook;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PlaceholderExpansion {

    private final UltraCosmetics ultraCosmetics;

    public PlaceholderHook(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        UltraPlayer ultraPlayer = ultraCosmetics.getPlayerManager().getUltraPlayer(player);
        if (identifier.startsWith("ammo_")) {
            String gadget = identifier.substring(5);
            GadgetType type = CosmeticType.valueOf(Category.GADGETS, gadget);
            if (type == null) {
                return null;
            }
            return String.valueOf(ultraPlayer.getAmmo(type));
        }
        if (identifier.startsWith("current_")) {
            identifier = identifier.substring(8);
            if (identifier.startsWith("suit_")) {
                identifier = "suits_" + identifier.substring(5);
            }
            identifier = identifier.replace("particleeffect", "effect");
            Category category = Category.fromString(identifier);
            if (category == null) {
                return null;
            }
            Cosmetic<?> cosmetic = ultraPlayer.getCosmetic(category);
            if (cosmetic == null) {
                return "None";
            }
            return MessageManager.toLegacy(cosmetic.getTypeName());
        }
        switch (identifier) {
            // Keys, and user-specific settings
            case "keys":
                return "" + ultraPlayer.getKeys();
            case "gadgets_enabled":
                return "" + ultraPlayer.hasGadgetsEnabled();
            case "morph_selfview":
                return "" + ultraPlayer.canSeeSelfMorph();
            case "treasurechest_active":
                return "" + (ultraPlayer.getCurrentTreasureChest() != null);
        }
        return null;
    }

    private String currentName(UltraPlayer ultraPlayer, Category category) {
        Cosmetic<?> cosmetic = ultraPlayer.getCosmetic(category);
        if (cosmetic == null) {
            return "None";
        }
        return MessageManager.toLegacy(cosmetic.getTypeName());
    }

    @Override
    public String getIdentifier() {
        return "ultracosmetics";
    }

    @Override
    public String getAuthor() {
        return ultraCosmetics.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return ultraCosmetics.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    public static String parsePlaceholders(Player player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }
}
