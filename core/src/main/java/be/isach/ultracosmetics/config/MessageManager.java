package be.isach.ultracosmetics.config;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.util.SmartLogger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class MessageManager {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacySection().toBuilder()
                    .hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static MessageManager instance;
    private final SettingsManager messagesConfig;
    private final MiniMessage miniMessage;
    private final BukkitAudiences audiences = BukkitAudiences.create(UltraCosmeticsData.get().getPlugin());
    // should be set to true by the time anybody else can read this
    private boolean success = false;

    public static String getLangFilename() {
        return "messages_" + UltraCosmeticsData.get().getLanguage();
    }

    /*
      Load the messages config
     */
    private MessageManager() {
        String langFile = getLangFilename();
        messagesConfig = new SettingsManager(langFile);
        if (!messagesConfig.success()) {
            miniMessage = null;
            return;
        }

        Reader reader = UltraCosmeticsData.get().getPlugin().getFileReader("messages/" + langFile + ".yml");
        loadMessages(YamlConfiguration.loadConfiguration(reader));
        messagesConfig.save();
        miniMessage = buildMinimessage(true);
        success = true;
    }

    private MiniMessage buildMinimessage(boolean includePrefix) {
        TagResolver.Builder tagBuilder = TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.font())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.newline())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.reset());
        if (includePrefix) {
            tagBuilder.resolver(Placeholder.parsed("prefix", messagesConfig.getString("Prefix") + "<reset>"));
        }
        return MiniMessage.builder().tags(tagBuilder.build()).build();
    }

    private void checkMessageExists(String messagePath) {
        if (!messagesConfig.fileConfiguration.isString(messagePath)) {
            throw new IllegalArgumentException("No such message key: " + messagePath);
        }
    }

    private @NotNull Component getMessageInternal(String messagePath, TagResolver.Single... placeholders) {
        checkMessageExists(messagePath);
        return miniMessage.deserialize(messagesConfig.getString(messagePath), placeholders);
    }

    private List<String> getLoreInternal(String messagePath, Style defaultStyle, TagResolver.Single... placeholders) {
        checkMessageExists(messagePath);
        // We have to do it line by line or the formatting won't carry over
        // into lore correctly.
        String[] parts = messagesConfig.getString(messagePath).split("\n");
        Style lastStyle = defaultStyle == null ? Style.empty() : defaultStyle;
        Component component;
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            component = miniMessage.deserialize(part, placeholders).applyFallbackStyle(lastStyle);
            result.add(toLegacy(component));
            lastStyle = component.style();
        }
        return result;
    }

    private void addMessageInternal(String path, String message) {
        if (messagesConfig.addDefault(path, message)) {
            // Has its own if-block to avoid the dead code warning
            if (CosmeticType.GENERATE_MISSING_MESSAGES) {
                UltraCosmeticsData.get().getPlugin().getSmartLogger().write("Adding message " + path);
            }
        }
    }

    /**
     * Set up the messages in the config.
     */
    private void loadMessages(YamlConfiguration defaults) {
        ConfigurationSection menuBlock = messagesConfig.getConfigurationSection("Menu");
        if (menuBlock != null && menuBlock.isString("Gadgets")) {
            upgradeCategoryStrings(menuBlock);
        }

        String oldGadgetKey = "Treasure-Chests-Loot.gadget";
        String newGadgetKey = "Treasure-Chests-Loot.Gadget";
        String gadgetMessage = messagesConfig.getString(oldGadgetKey);
        if (gadgetMessage != null) {
            messagesConfig.set(newGadgetKey, gadgetMessage);
            messagesConfig.set(oldGadgetKey, null);
        }
        if (messagesConfig.getString("Prefix") != null && messagesConfig.getString("Prefix").startsWith("&")) {
            minimessageMigration();
        }
        String noPermissionMessage = messagesConfig.getString("No-Permission");
        if (noPermissionMessage != null && noPermissionMessage.startsWith("<prefix> ")) {
            messagesConfig.set("No-Permission", noPermissionMessage.substring(9));
        }
        ConfigurationSection newLoots = messagesConfig.getConfigurationSection("Treasure-Chests-Loot-Messages");
        if (newLoots != null) {
            for (String key : newLoots.getKeys(false)) {
                if (newLoots.getString(key).contains("%")) {
                    newLoots.set(key, newLoots.getString(key).replaceAll("%(\\w+)%", "<$1>"));
                }
            }
        }
        for (String key : defaults.getKeys(true)) {
            addMessageInternal(key, defaults.getString(key));
        }
    }

    private void upgradeCategoryStrings(ConfigurationSection menuBlock) {
        messagesConfig.set("Menu", null);
        Map<Category, Map<String, String>> buttons = new HashMap<>();
        Map<Category, String> menuNames = new HashMap<>();
        for (Category cat : Category.values()) {
            Map<String, String> catSection = new HashMap<>();
            catSection.put("Name", menuBlock.getString(cat.getConfigPath()));
            buttons.put(cat, catSection);
            menuNames.put(cat, messagesConfig.getString("Menus." + cat.getConfigPath()));
        }
        addButton(buttons, menuBlock, Category.PETS, "Spawn", "Despawn");
        addButton(buttons, menuBlock, Category.GADGETS, "Activate", "Deactivate");
        addButton(buttons, menuBlock, Category.EFFECTS, "Summon", "Unsummon");
        addButton(buttons, menuBlock, Category.MOUNTS, "Spawn", "Despawn");
        addButton(buttons, menuBlock, Category.MORPHS, "Morph", "Unmorph");
        addButton(buttons, menuBlock, Category.HATS, "Equip", "Unequip");
        addButton(buttons, menuBlock, Category.SUITS_HELMET, "Equip", "Unequip");
        addButton(buttons, menuBlock, Category.EMOTES, "Equip", "Unequip");
        for (Entry<Category, Map<String, String>> catMap : buttons.entrySet()) {
            messagesConfig.set("Menu." + catMap.getKey().getConfigPath() + ".Title", menuNames.get(catMap.getKey()));
            for (Entry<String, String> translation : catMap.getValue().entrySet()) {
                messagesConfig.set("Menu." + catMap.getKey().getConfigPath() + ".Button." + translation.getKey(),
                        translation.getValue());
            }
        }
        messagesConfig.set("Menu.Main.Button.Name", menuBlock.getString("Main-Menu"));
        migrateKey("Menus.Main-Menu", "Menu.Main.Title");
        migrateKey("Menus.Buy-Ammo", "Menu.Buy-Ammo.Title");
        migrateKey("Menus.Rename-Pet", "Menu.Purchase-Rename.Title");
        migrateKey("Rename-Pet-Purchase", "Menu.Purchase-Rename.Button.Showcase");
        migrateKey("Rename-Pet", "Menu.Rename-Pet.Button.Name");
        migrateKey("Rename-Pet-Placeholder", "Menu.Rename-Pet.Placeholder");
        migrateKey("Rename-Pet-Title", "Menu.Rename-Pet.Title");

        migrateMiscButton(menuBlock, "Previous-Page");
        migrateMiscButton(menuBlock, "Next-Page");
        messagesConfig.set("Menus", null);

        // Only categories that don't already use "Equip" path
        migrateActivateMsg(Category.PETS, "Spawn", "Despawn");
        migrateActivateMsg(Category.EFFECTS, "Summon", "Unsummon");
        migrateActivateMsg(Category.MOUNTS, "Spawn", "Despawn");
        migrateActivateMsg(Category.MORPHS, "Morph", "Unmorph");

        migrateClearMsg("Cosmetics", "Cosmetics");
        for (Category cat : Category.values()) {
            String configName = getMessagesName(cat);
            migrateClearMsg(cat.getConfigPath(), configName.substring(0, configName.length() - 1));
        }
    }

    public String getMessagesName(Category category) {
        if (category.isSuits()) {
            return "Suits";
        }
        // Like configPath but value is different for Category.EFFECTS
        return category.name().charAt(0) +
                category.getConfigPath().substring(1).toLowerCase(Locale.ROOT).replace("_", "-");
    }

    private void addButton(Map<Category, Map<String, String>> buttons, ConfigurationSection menuBlock, Category cat,
                           String oldEquipKey, String oldUnequipKey) {
        buttons.get(cat).put("Tooltip-Equip", menuBlock.getString(oldEquipKey));
        buttons.get(cat).put("Tooltip-Unequip", menuBlock.getString(oldUnequipKey));
    }

    private void migrateMiscButton(ConfigurationSection section, String key) {
        messagesConfig.set("Menu.Misc.Button." + key, section.getString(key));
    }

    private void migrateActivateMsg(Category cat, String oldEquipKey, String oldUnequipKey) {
        migrateKey(cat.getConfigPath() + "." + oldEquipKey, cat.getConfigPath() + ".Equip");
        migrateKey(cat.getConfigPath() + "." + oldUnequipKey, cat.getConfigPath() + ".Unequip");
    }

    private void migrateClearMsg(String newKey, String oldKey) {
        migrateKey("Clear-" + oldKey, "Clear." + newKey);
    }

    private void migrateKey(String oldKey, String newKey) {
        messagesConfig.set(newKey, messagesConfig.getString(oldKey));
        messagesConfig.set(oldKey, null);
    }

    private void minimessageMigration() {
        SmartLogger log = UltraCosmeticsData.get().getPlugin().getSmartLogger();
        log.write(SmartLogger.LogLevel.WARNING,
                "Your messages file is using legacy color codes, it will be upgraded now");
        // Prefix hasn't been converted yet, so don't use it.
        migration(LEGACY_SERIALIZER, buildMinimessage(false));
    }

    private void migration(ComponentSerializer<Component, ?, String> deserializer,
                           ComponentSerializer<Component, ?, String> serializer) {
        ConfigurationSection config = messagesConfig.fileConfiguration;
        Pattern percentVarPattern = Pattern.compile("%([\\w-]+)%");
        for (String key : config.getKeys(true)) {
            if (!config.isString(key)) {
                continue;
            }
            // Doing it line by line prevents weird behavior like this:
            //    <italic><gray>TSUNAMI!
            //    </gray></italic><italic><gray>Run for your life!
            String[] raw = config.getString(key).split("\n");
            String[] converted = new String[raw.length];
            for (int i = 0; i < raw.length; i++) {
                converted[i] = serializer.serialize(deserializer.deserialize(raw[i]));
            }
            // Replace percents AFTER conversion, because MiniMessage
            // will escape placeholders it doesn't recognize at the moment.
            config.set(key, percentVarPattern.matcher(String.join("\n", converted)).replaceAll("<$1>"));
        }

        ConfigurationSection customItem =
                SettingsManager.getConfig().getConfigurationSection("No-Permission.Custom-Item");
        String name = customItem.getString("Name", "");
        String convertedName = serializer.serialize(deserializer.deserialize(name));
        customItem.set("Name", convertedName.replace("{cosmetic-name}", "<cosmetic>"));
        List<String> lore = new ArrayList<>();
        for (String loreItem : customItem.getStringList("Lore")) {
            lore.add(serializer.serialize(deserializer.deserialize(loreItem)));
        }
        customItem.set("Lore", lore);
        migrateConfigStrings(serializer, deserializer);
    }

    private void migrateConfigStrings(ComponentSerializer<Component, ?, String> serializer,
                                      ComponentSerializer<Component, ?, String> deserializer) {
        ConfigurationSection loots = SettingsManager.getConfig().getConfigurationSection("TreasureChests.Loots");
        for (String key : loots.getKeys(false)) {
            String path = key + ".Message.message";
            if (loots.isString(path)) {
                String message = serializer.serialize(deserializer.deserialize(loots.getString(path)));
                message = message.replaceAll("%(\\w+)%", "<$1>");
                messagesConfig.set("Treasure-Chests-Loot-Messages." + key, message);
                loots.set(path, null);
            }
        }
        ConfigurationSection noPermission = SettingsManager.getConfig().getConfigurationSection("No-Permission");
        for (String key : new String[]{"Yes", "No", "Showroom"}) {
            String path = "Lore-Message-" + key;
            if (noPermission.isString(path)) {
                String newKey = key;
                if (!key.equals("Showroom")) {
                    newKey = "Permission-" + key;
                }
                messagesConfig.set("Permission-Lore." + newKey,
                        serializer.serialize(deserializer.deserialize(noPermission.getString(path))));
                noPermission.set(path, null);
            }
        }
    }

    public static boolean load() {
        destroy();
        instance = new MessageManager();
        return instance.success;
    }

    private static MessageManager getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    /**
     * Add a message in the messages.yml file.
     *
     * @param path    The config path.
     * @param message The config value.
     */
    public static void addMessage(String path, String message) {
        getInstance().addMessageInternal(path, message);
    }

    /**
     * Gets a message.
     *
     * @param messagePath The path of the message in the config.
     * @return a message from a config path.
     */
    @NotNull
    public static Component getMessage(String messagePath, TagResolver.Single... placeholders) {
        return getInstance().getMessageInternal(messagePath, placeholders);
    }

    public static String toLegacy(Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

    public static String getLegacyMessage(String messagePath, TagResolver.Single... placeholders) {
        return toLegacy(getMessage(messagePath, placeholders));
    }

    public static List<String> getLore(String messagePath, Style defaultStyle, TagResolver.Single... placeholders) {
        return getInstance().getLoreInternal(messagePath, defaultStyle, placeholders);
    }

    public static void send(CommandSender sender, String messagePath, TagResolver.Single... placeholders) {
        Component message = getMessage(messagePath, placeholders);
        if (!Component.empty().equals(message)) {
            getInstance().audiences.sender(sender).sendMessage(message);
        }
    }

    public static BukkitAudiences getAudiences() {
        return getInstance().audiences;
    }

    public static MiniMessage getMiniMessage() {
        return getInstance().miniMessage;
    }

    public static void save() {
        getInstance().messagesConfig.save();
    }

    public static void destroy() {
        if (instance != null) {
            instance.audiences.close();
            instance = null;
        }
    }

    public static LegacyComponentSerializer legacySerializer() {
        return LEGACY_SERIALIZER;
    }
}
