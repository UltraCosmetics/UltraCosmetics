package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubCommandSelfView extends SubCommand {

    public SubCommandSelfView(UltraCosmetics ultraCosmetics) {
        super("selfview", "Toggle Morph Self View", "", ultraCosmetics, true);
    }

    @Override
    protected void onExePlayer(Player sender, String[] args) {
        if (!SettingsManager.isAllowedWorld(sender.getWorld())) {
            MessageManager.send(sender, "World-Disabled");
            return;
        }

        UltraPlayer customPlayer = ultraCosmetics.getPlayerManager().getUltraPlayer(sender);
        customPlayer.setSeeSelfMorph(!customPlayer.canSeeSelfMorph());
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        notAllowed(sender);
    }
}
