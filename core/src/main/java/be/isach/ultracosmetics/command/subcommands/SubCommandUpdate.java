package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.util.UpdateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SubCommandUpdate extends SubCommand {

    public SubCommandUpdate(UltraCosmetics ultraCosmetics) {
        super("update", "Updates UltraCosmetics", "", ultraCosmetics);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        UpdateManager updateManager = ultraCosmetics.getUpdateChecker();
        if (updateManager == null) {
            sender.sendMessage(ChatColor.RED + "Version checking must be enabled to update.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Current version: " + updateManager.getCurrentVersion().versionClassifierCommit());
        if (args.length > 1 && args[1].equalsIgnoreCase("force")) {
            sender.sendMessage(ChatColor.RED + "Ignoring version check and downloading anyway. This is not recommended and may result in a downgrade.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Update status: " + updateManager.getStatus());
            if (!updateManager.isOutdated()) {
                return;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "UltraCosmetics " + updateManager.getSpigotVersion() + " is available to download.");
        sender.sendMessage(ChatColor.YELLOW + "Requesting update...");

        ultraCosmetics.getScheduler().runAsync((task) -> {
            boolean success = updateManager.update();
            if (success) {
                sender.sendMessage(ChatColor.GREEN + "Update succeeded, please restart the server to complete the update.");
            } else {
                sender.sendMessage(ChatColor.RED + "Update failed or aborted, please check the console for more information.");
            }
        });
    }

}
