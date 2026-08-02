package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.SettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class TextUtil {

    public static Component filterPlaceholderColors(Component placeholder) {
        if (UltraCosmeticsData.get().arePlaceholdersColored()) {
            return placeholder;
        }
        return stripColor(placeholder);
    }

    public static Component stripColor(Component component) {
        return Component.text(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static String formatNumber(long number) {
        String separator = SettingsManager.getConfig().getString("Thousands-Separator", "");
        if (separator.isEmpty()) {
            return String.valueOf(number);
        }
        // Replacing commas allows separators to be longer than one character, if that's desired
        return String.format("%,d", number).replace(",", separator);
    }
}
