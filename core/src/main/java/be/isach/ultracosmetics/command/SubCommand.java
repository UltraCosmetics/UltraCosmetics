package be.isach.ultracosmetics.command;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public abstract class SubCommand {

    private final String name;
    private final String description;
    private final Permission permission;
    private final String usage;
    private final boolean defaultPerm;
    protected final UltraCosmetics ultraCosmetics;

    public SubCommand(String name, String description, String usage, UltraCosmetics ultraCosmetics) {
        this(name, description, usage, ultraCosmetics, false);
    }

    public SubCommand(String name, String description, String usage, UltraCosmetics ultraCosmetics,
                      boolean defaultPerm) {
        this.name = name;
        this.description = description;
        this.permission = registerPermission("ultracosmetics.command." + name, defaultPerm);
        this.usage = "/uc " + name + " " + usage;
        this.defaultPerm = defaultPerm;
        this.ultraCosmetics = ultraCosmetics;
    }

    /**
     * Checks if the given String is an alias of this command.
     *
     * @param arg The String to check.
     * @return {@code true} if the String is an alias.
     */
    public boolean is(String arg) {
        return name.equalsIgnoreCase(arg);
    }

    /**
     * Get the name of the command
     *
     * @return The name of the command
     */
    public String getName() {
        return name;
    }

    /**
     * Get the usage message of this command.
     *
     * @return The usage of this command.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Get the description of this command.
     *
     * @return The description of this command.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the permission of this command.
     *
     * @return The permission of this command.
     */
    public Permission getPermission() {
        return permission;
    }

    public boolean isDefault() {
        return defaultPerm;
    }

    private Permission registerPermission(String strPerm, boolean defaultPerm) {
        Permission perm = new Permission(strPerm);
        try {
            Bukkit.getPluginManager().addPermission(perm);
        } catch (IllegalArgumentException e) {
            // Ignore; the user has been warned about reloading the plugin elsewhere
            return Bukkit.getPluginManager().getPermission(strPerm);
        }
        perm.setDefault(defaultPerm ? PermissionDefault.TRUE : PermissionDefault.OP);
        return perm;
    }

    /**
     * Called when the sub command is executed by a player.
     *
     * @param sender The player who executed the command.
     * @param args   The args of the command. (Includes the subcommand alias).
     */
    protected void onExePlayer(Player sender, String[] args) {
        onExeAnyone(sender, args);
    }

    /**
     * Called when the sub command is executed by someone other than a player,
     * or when the command does not distinguish between players and non-players.
     *
     * @param sender The sender who executed the command.
     * @param args   The args of the command. (Includes the subcommand alias).
     */
    protected abstract void onExeAnyone(CommandSender sender, String[] args);

    /**
     * Called when a CommandSender attempts to tab-complete a command.
     *
     * @param sender  The CommandSender attempting the tab-complete
     * @param args    The current args in the chat window
     * @param options The options that should be displayed to the player. Modify this. Options will be lowercased and filtered by caller.
     */
    protected void tabComplete(CommandSender sender, String[] args, List<String> options) {
    }

    protected void addCategories(List<String> options) {
        for (Category category : Category.enabled()) {
            options.add(category.toString());
        }
    }

    protected void addPlayers(List<String> options) {
        Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
    }

    /**
     * Called when a command is used from console but only works on players
     *
     * @param commandSender The sender who needs to be informed about this
     */
    protected void notAllowed(CommandSender commandSender) {
        MessageManager.send(commandSender, "Not-Allowed-From-Console");
    }

    protected void badUsage(CommandSender sender) {
        badUsage(sender, getUsage());
    }

    protected void badUsage(CommandSender sender, String usage) {
        error(sender, "Incorrect Usage. " + usage);
    }

    protected void error(CommandSender sender, String error) {
        MessageManager.getAudiences().sender(sender).sendMessage(
                Component.empty().append(MessageManager.getMessage("Prefix")).appendSpace()
                        .append(Component.text(error, NamedTextColor.RED, TextDecoration.BOLD))
        );
    }
}
