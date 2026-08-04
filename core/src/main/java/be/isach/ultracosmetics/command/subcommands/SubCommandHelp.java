package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.CommandManager;
import be.isach.ultracosmetics.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SubCommandHelp extends SubCommand {
    private final CommandManager commandManager;

    public SubCommandHelp(UltraCosmetics ultraCosmetics, CommandManager commandManager) {
        super("help", "Shows available UltraCosmetics commands", "", ultraCosmetics);
        this.commandManager = commandManager;
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        if (args.length > 1) {
            try {
                showHelp(sender, Math.min(Integer.parseInt(args[1]), getMaxPages(commandManager.getCommands().size())));
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        showHelp(sender, 1);
    }

    public void showHelp(CommandSender sender, int page) {
        List<SubCommand> available = new ArrayList<>();
        commandManager.getCommands().stream().filter(c -> sender.hasPermission(c.getPermission()))
                .forEach(available::add);
        if (available.isEmpty()) {
            CommandManager.sendNoPermissionMessage(sender);
            return;
        }
        sender.sendMessage("");
        sender.sendMessage(
                ChatColor.WHITE + "" + ChatColor.BOLD + "UltraCosmetics Help (/uc <page>) " + ChatColor.DARK_GRAY + "" +
                        ChatColor.BOLD + "(" + page + "/" + getMaxPages(available.size()) + ")");
        int from = 8 * (page - 1);
        int to = 8 * page;
        for (int i = from; i < to; i++) {
            if (i >= available.size()) {
                break;
            }
            SubCommand sub = available.get(i);
            sender.sendMessage(ChatColor.DARK_GRAY + "|  " + ChatColor.GRAY + sub.getUsage() + ChatColor.WHITE + " " +
                    ChatColor.ITALIC + sub.getDescription());
        }
    }

    private int getMaxPages(int commands) {
        int max = 8;
        // test cases:
        // 8 commands: cmds - 1 = 7, 7 / 8 = 0, 0 + 1 = 1
        // 9 commands: cmds - 1 = 8, 8 / 8 = 1, 1 + 1 = 2
        // 0 commands: cmds - 1 = -1, -1 / 8 = 0, 0 + 1 = 1
        return ((commands - 1) / max) + 1;
    }
}
