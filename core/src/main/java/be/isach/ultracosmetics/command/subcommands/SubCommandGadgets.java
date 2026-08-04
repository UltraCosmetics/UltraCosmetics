package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandGadgets extends SubCommand {

    public SubCommandGadgets(UltraCosmetics ultraCosmetics) {
        super("gadgets", "Toggle Gadgets", "", ultraCosmetics, true);
    }

    @Override
    protected void onExePlayer(Player sender, String[] args) {
        if (!SettingsManager.getConfig().getBoolean("Categories.Gadgets.Allow-Disable-Gadgets", true)) {
            MessageManager.send(sender, "Cannot-Disable-Gadgets");
            return;
        }

        if (!SettingsManager.isAllowedWorld(sender.getWorld())) {
            MessageManager.send(sender, "World-Disabled");
            return;
        }

        UltraPlayer ultraPlayer = ultraCosmetics.getPlayerManager().getUltraPlayer(sender);
        boolean enabled = !ultraPlayer.hasGadgetsEnabled();
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes")) {
                enabled = true;
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off") ||
                    args[1].equalsIgnoreCase("no")) {
                enabled = false;
            }
        }
        ultraPlayer.setGadgetsEnabled(enabled);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        notAllowed(sender);
    }

    @Override
    protected void tabComplete(CommandSender sender, String[] args, List<String> options) {
        if (args.length == 2) {
            options.add("true");
            options.add("false");
            options.add("on");
            options.add("off");
        }
    }
}
