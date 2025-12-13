package be.isach.ultracosmetics.command;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.subcommands.*;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.util.Problem;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Command manager.
 *
 * @author iSach
 * @since 12-20-2015
 */
public class CommandManager implements CommandExecutor {
    private final UltraCosmetics ultraCosmetics;
    /**
     * List of the registered commands.
     */
    private final List<SubCommand> commands = new ArrayList<>();
    private final SubCommandHelp help;
    private final SubCommand menu;

    public CommandManager(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        PluginCommand cmd = ultraCosmetics.getCommand("ultracosmetics");
        cmd.setExecutor(this);
        cmd.setTabCompleter(new UCTabCompleter(this));
        help = new SubCommandHelp(ultraCosmetics, this);
        menu = new SubCommandMenu(ultraCosmetics);
        registerCommands();
    }

    /**
     * Registers a command.
     *
     * @param meCommand The command to register.
     */
    public void registerCommand(SubCommand meCommand) {
        commands.add(meCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Set<Problem> problems = ultraCosmetics.getSevereProblems();
        if (!problems.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Plugin is currently disabled because:");
            // Don't use MessageManager's audiences, we can't be sure it initialized
            try (BukkitAudiences audiences = BukkitAudiences.create(ultraCosmetics)) {
                Audience audience = audiences.sender(sender);
                problems.forEach(p -> audience.sendMessage(p.getSummary().color(NamedTextColor.RED)));
                SubCommandTroubleshoot.sendSupportMessage(sender);
                return true;
            }
        }

        if (args.length == 0) {
            if (sender instanceof Player && SettingsManager.getConfig().getBoolean("Open-Menu-On-Base-Command")) {
                menu.onExePlayer((Player) sender, new String[] {});
            } else {
                help.showHelp(sender, 1);
            }
            return true;
        }

        for (SubCommand meCommand : commands) {
            if (meCommand.is(args[0])) {

                if (!sender.hasPermission(meCommand.getPermission())) {
                    sendNoPermissionMessage(sender);
                    return true;
                }

                if (sender instanceof Player) {
                    meCommand.onExePlayer((Player) sender, args);
                } else {
                    meCommand.onExeAnyone(sender, args);
                }
                return true;
            }
        }
        help.showHelp(sender, 1);
        return true;
    }

    public List<SubCommand> getCommands() {
        return commands;
    }

    public void registerCommands() {
        registerCommand(help);
        registerCommand(menu);
        registerCommand(new SubCommandGadgets(ultraCosmetics));
        registerCommand(new SubCommandSelfView(ultraCosmetics));
        // registerCommand(new SubCommandPurge(ultraCosmetics));
        registerCommand(new SubCommandGive(ultraCosmetics));
        registerCommand(new SubCommandToggle(ultraCosmetics));
        registerCommand(new SubCommandClear(ultraCosmetics));
        registerCommand(new SubCommandTreasure(ultraCosmetics));
        registerCommand(new SubCommandTreasureNotification(ultraCosmetics));
        registerCommand(new SubCommandMigrate(ultraCosmetics));
        registerCommand(new SubCommandReward(ultraCosmetics));
        registerCommand(new SubCommandReload(ultraCosmetics));
        registerCommand(new SubCommandUpdate(ultraCosmetics));
        registerCommand(new SubCommandTroubleshoot(ultraCosmetics));
        registerCommand(new SubCommandPermission(ultraCosmetics));
        registerCommand(new SubCommandRename(ultraCosmetics));
        registerCommand(new SubCommandWhyDoIHave(ultraCosmetics));
        registerCommand(new SubCommandDebug(ultraCosmetics));
    }

    public static void sendNoPermissionMessage(CommandSender sender) {
        Component prefix = MessageManager.getMessage("Prefix");
        Component noPermission = MessageManager.getMessage("No-Permission");
        MessageManager.getAudiences().sender(sender).sendMessage(Component.empty().append(prefix).appendSpace().append(noPermission));
    }
}
