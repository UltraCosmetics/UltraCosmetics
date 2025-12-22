package be.isach.ultracosmetics.menu.buttons;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.menu.Button;
import be.isach.ultracosmetics.menu.ClickData;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class CommandButton implements Button {
    private final ItemStack stack;
    private final String command;
    private final boolean closeAfterClick;

    public CommandButton(ItemStack stack, String command, boolean closeAfterClick) {
        this.stack = stack;
        this.command = command;
        this.closeAfterClick = closeAfterClick;
    }

    @Override
    public ItemStack getDisplayItem(UltraPlayer ultraPlayer) {
        return stack;
    }

    @Override
    public void onClick(ClickData clickData) {
        if (closeAfterClick) {
            clickData.getClicker().getBukkitPlayer().closeInventory();
        }
        if (command == null || command.isBlank()) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", clickData.getClicker().getBukkitPlayer().getName()));
    }

    private static void migrate(ConfigurationSection section) {
        // Migrate item format to XItemStack
        ConfigurationSection item = section.createSection("item");
        BiConsumer<String, String> migrate = (before, after) -> {
            item.set(after, section.get(before));
            section.set(before, null);
        };
        migrate.accept("Material", "material");
        migrate.accept("Amount", "amount");
        migrate.accept("Name", "name");
        migrate.accept("Lore", "lore");
        migrate.accept("CustomModelData", "custom-model-data");
    }

    public static CommandButton deserialize(ConfigurationSection section, UltraCosmetics ultraCosmetics) {
        if (section.isString("Material")) {
            migrate(section);
        }
        if (!section.isConfigurationSection("item")) {
            throw new IllegalArgumentException("No item defined");
        }
        ItemStack stack = ItemFactory.parseXItemStack(section.getConfigurationSection("item"));
        boolean closeAfterClick = section.getBoolean("CloseAfterClick");
        String command = section.getString("Command");
        return new CommandButton(stack, command, closeAfterClick);
    }
}
