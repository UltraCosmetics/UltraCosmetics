package be.isach.ultracosmetics.menu;

import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A config-defined visual grouping of cosmetics shown in the unified menu. Cosmetics
 * keep their real {@link Category} for permissions/commands/messages; this class only
 * changes how they're laid out and grouped in {@link be.isach.ultracosmetics.menu.menus.MenuUnified}.
 */
public class VirtualCategory {

    /** Recognized values for the {@code Extras:} map in the config. */
    public enum ExtraButton {
        RENAME_PET, TOGGLE_GADGETS, TOGGLE_MORPH_SELF_VIEW, CLEAR;

        public static ExtraButton fromString(String raw) {
            if (raw == null) return null;
            switch (raw.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "")) {
                case "renamepet": return RENAME_PET;
                case "togglegadgets": return TOGGLE_GADGETS;
                case "togglemorphselfview": case "togglemorph": return TOGGLE_MORPH_SELF_VIEW;
                case "clearcosmetic": case "clear": return CLEAR;
                default: return null;
            }
        }
    }

    private final String id;
    private final String displayName;
    private final int slot;
    private final XMaterial itemOverride;
    private final List<CosmeticType<?>> cosmetics;
    private final Set<Category> coveredCategories;
    private final Map<Integer, ExtraButton> extras;

    private VirtualCategory(String id, String displayName, int slot, XMaterial itemOverride,
                            List<CosmeticType<?>> cosmetics, Set<Category> coveredCategories,
                            Map<Integer, ExtraButton> extras) {
        this.id = id;
        this.displayName = displayName;
        this.slot = slot;
        this.itemOverride = itemOverride;
        this.cosmetics = cosmetics;
        this.coveredCategories = coveredCategories;
        this.extras = extras;
    }

    public Map<Integer, ExtraButton> getExtras() {
        return extras;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlot() {
        return slot;
    }

    /**
     * @return the material used for this button (both active and inactive states pick
     *         their own material from config; this override only replaces the inactive
     *         one). {@code null} if the default should be used.
     */
    public XMaterial getItemOverride() {
        return itemOverride;
    }

    public List<CosmeticType<?>> getCosmetics() {
        return cosmetics;
    }

    /**
     * @return the set of real {@link Category} values whose cosmetics this virtual
     *         category contains. Used by the clear button to know what to remove.
     */
    public Set<Category> getCoveredCategories() {
        return coveredCategories;
    }

    /**
     * Loads the virtual categories from {@code Categories.Unified-Menu.Virtual-Categories}.
     * Skips malformed entries (missing slot, missing/unknown cosmetics) with a warning.
     *
     * @return the list of loaded virtual categories in config order, or an empty list if
     *         the section is missing or empty.
     */
    public static List<VirtualCategory> loadFromConfig() {
        ConfigurationSection section = SettingsManager.getConfig()
                .getConfigurationSection("Categories.Unified-Menu.Virtual-Categories");
        if (section == null) return Collections.emptyList();

        List<VirtualCategory> result = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;

            if (!entry.isInt("Slot")) {
                warn("Virtual category '" + key + "' is missing 'Slot', skipping.");
                continue;
            }
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot > 53) {
                warn("Virtual category '" + key + "' has out-of-range Slot " + slot + " (must be 0-53), skipping.");
                continue;
            }

            String displayName = entry.getString("Name", key);
            XMaterial itemOverride = null;
            if (entry.isString("Item")) {
                itemOverride = ItemFactory.getNullableXMaterialFromConfig(
                        "Categories.Unified-Menu.Virtual-Categories." + key + ".Item");
            }

            List<String> names = entry.getStringList("Cosmetics");
            List<CosmeticType<?>> resolved = new ArrayList<>();
            Set<Category> covered = EnumSet.noneOf(Category.class);
            for (String name : names) {
                CosmeticType<?> type = resolveByName(name, key);
                if (type == null) continue;
                resolved.add(type);
                covered.add(type.getCategory());
            }

            Map<Integer, ExtraButton> extras = parseExtras(entry, key);

            result.add(new VirtualCategory(key, displayName, slot, itemOverride, resolved, covered, extras));
        }
        return result;
    }

    private static Map<Integer, ExtraButton> parseExtras(ConfigurationSection entry, String key) {
        Map<Integer, ExtraButton> extras = new LinkedHashMap<>();
        ConfigurationSection extraSection = entry.getConfigurationSection("Extras");
        if (extraSection == null) return extras;
        for (String slotKey : extraSection.getKeys(false)) {
            int slot;
            try {
                slot = Integer.parseInt(slotKey);
            } catch (NumberFormatException e) {
                warn("Virtual category '" + key + "': Extras key '" + slotKey + "' is not a number, skipping.");
                continue;
            }
            if (slot < 0 || slot > 53) {
                warn("Virtual category '" + key + "': Extras slot " + slot + " out of range 0-53, skipping.");
                continue;
            }
            ExtraButton type = ExtraButton.fromString(extraSection.getString(slotKey));
            if (type == null) {
                warn("Virtual category '" + key + "': Extras slot " + slot + " has unknown button '"
                        + extraSection.getString(slotKey) + "'. Allowed: renamepet, togglegadgets, togglemorphselfview, clear.");
                continue;
            }
            extras.put(slot, type);
        }
        return extras;
    }

    /**
     * Resolves a cosmetic entry from the {@code Cosmetics:} list. Two forms are accepted:
     * <ul>
     *   <li>{@code name} — searches every category in enum order and returns the first
     *       match. Ambiguous names like {@code Witch} or {@code Love} will resolve to
     *       whichever category comes first, which may not be what the user wanted.</li>
     *   <li>{@code prefix:name} — restricts the search to the given category. The prefix
     *       can be the short alias ({@code p}, {@code mor}, {@code pe}, {@code g},
     *       {@code ef}, {@code h}, {@code e}, {@code mou}, {@code d}), the enum name
     *       ({@code PROJECTILE_EFFECTS}), or the config path ({@code Projectile-Effects}).
     *       Case- and dash/underscore-insensitive.</li>
     * </ul>
     * Emits a warning and returns {@code null} for unknown prefix or unknown cosmetic.
     */
    private static CosmeticType<?> resolveByName(String entry, String key) {
        int colon = entry.indexOf(':');
        if (colon > 0 && colon < entry.length() - 1) {
            String prefixRaw = entry.substring(0, colon);
            String name = entry.substring(colon + 1);
            Category cat = resolveCategoryPrefix(prefixRaw);
            if (cat == null) {
                warn("Virtual category '" + key + "': unknown category prefix '" + prefixRaw + "' in '" + entry
                        + "'. Use the short alias (p, mor, pe, g, ef, h, e, mou, d), the enum name"
                        + " (PROJECTILE_EFFECTS), or the config path (Projectile-Effects).");
                return null;
            }
            CosmeticType<?> type = CosmeticType.valueOf(cat, name);
            if (type == null) {
                warn("Virtual category '" + key + "': unknown cosmetic '" + name + "' in category "
                        + cat.name() + ", ignoring.");
            }
            return type;
        }
        for (Category cat : Category.values()) {
            CosmeticType<?> type = CosmeticType.valueOf(cat, entry);
            if (type != null) return type;
        }
        warn("Virtual category '" + key + "': unknown cosmetic '" + entry + "', ignoring.");
        return null;
    }

    private static Category resolveCategoryPrefix(String raw) {
        String norm = raw.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        for (Category cat : Category.values()) {
            if (norm.equals(cat.getPrefix())) return cat;
            if (norm.equals(cat.name().toLowerCase(Locale.ROOT).replace("_", ""))) return cat;
            if (norm.equals(cat.getConfigPath().toLowerCase(Locale.ROOT).replace("-", ""))) return cat;
        }
        return null;
    }

    private static void warn(String message) {
        Bukkit.getLogger().warning("[UltraCosmetics] " + message);
        try {
            be.isach.ultracosmetics.UltraCosmeticsData.get().getPlugin()
                    .getSmartLogger().write(LogLevel.WARNING, message);
        } catch (Throwable ignored) {
            // SmartLogger may not be available during early init; fall back to Bukkit logger above.
        }
    }
}
