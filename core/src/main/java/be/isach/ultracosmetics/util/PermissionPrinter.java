package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.SuitCategory;
import be.isach.ultracosmetics.cosmetics.type.SuitType;
import be.isach.ultracosmetics.menu.Menu;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class PermissionPrinter {

    /**
     * Print permissions to permissions.txt
     */
    public static void printPermissions(UltraCosmetics ultraCosmetics) {
        // file used to be called 'permissions.yml' so delete the old one to prevent confusion
        File oldPermissions = new File(ultraCosmetics.getDataFolder(), "permissions.yml");
        // doesn't throw an exception if it didn't exist
        oldPermissions.delete();

        PrintWriter writer;
        try {
            writer = new PrintWriter(new File(ultraCosmetics.getDataFolder(), "permissions.txt"), "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        LocalDate date = LocalDate.now();
        String dateString = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));

        writer.println("UltraCosmetics v" + ultraCosmetics.getDescription().getVersion() + " permissions.");
        writer.println();
        writer.println("Generated automatically on " + dateString);
        writer.println();
        writer.println("### General permissions, enabled by default:");
        writer.println("  - ultracosmetics.receivechest");
        writer.println("  - ultracosmetics.openmenu");
        writer.println();
        writer.println("### Treasure Chests:");
        writer.println("  - ultracosmetics.treasurechests.buykey (enabled by default)");
        writer.println();
        writer.println("### Menu permissions:");
        writer.println("  - " + Menu.ALL_MENUS_PERMISSION.getName() + " (enabled by default)");
        for (Permission perm : Menu.getMenuPermissions()) {
            writer.println("  - " + perm.getName());
        }
        writer.println("### Bypass perms:");
        writer.println("  - ultracosmetics.bypass.disabledcommands");
        writer.println("  - ultracosmetics.bypass.cooldown (granted to no one by default)");
        writer.println();
        writer.println("### Commands:");
        writer.println("  - ultracosmetics.command.*");
        for (SubCommand subCommand : ultraCosmetics.getCommandManager().getCommands()) {
            writer.print("  - " + subCommand.getPermission().getName());
            if (subCommand.isDefault()) {
                writer.print(" (enabled by default)");
            }
            writer.println();
        }
        writer.println();
        writer.println("### Other:");
        writer.println("  - ultracosmetics.allcosmetics");
        writer.println("  - ultracosmetics.updatenotify");

        for (Category cat : Category.values()) {
            if (cat.isSuits()) {
                continue;
            }
            writer.println();
            writer.println("### " + cat.getConfigPath().replace("-", " ") + ":");
            writer.println("  - " + cat.getPermission() + ".*");
            for (CosmeticType<?> type : cat.getValues()) {
                writer.println("  - " + type.getPermission().getName());
            }
        }
        writer.println();
        writer.println("### Suits:");
        writer.println("  - ultracosmetics.suits.*");
        for (SuitCategory cat : SuitCategory.values()) {
            writer.println("  - ultracosmetics.suits." + cat.getConfigName().toLowerCase(Locale.ROOT) + ".*");
            for (SuitType suitType : cat.getPieces()) {
                writer.println("    - " + suitType.getPermission().getName());
            }
        }

        writer.close();
    }

}
