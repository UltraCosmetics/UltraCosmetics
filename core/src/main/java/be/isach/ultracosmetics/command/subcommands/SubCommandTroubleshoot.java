package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.util.Problem;
import be.isach.ultracosmetics.util.UpdateManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class SubCommandTroubleshoot extends SubCommand {

    public SubCommandTroubleshoot(UltraCosmetics ultraCosmetics) {
        super("troubleshoot", "Identifies issues with UltraCosmetics", "", ultraCosmetics);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        Set<Problem> problems = ultraCosmetics.getProblems();
        if (problems.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "UltraCosmetics is not currently aware of any problems :)");
        } else {
            sender.sendMessage(ChatColor.RED + "UltraCosmetics currently has " + problems.size() + " minor problems.");
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "(Click on a problem to see its wiki page)");
            }
            Audience audience = MessageManager.getAudiences().sender(sender);
            problems.forEach(p -> audience.sendMessage(p.getSummary().color(NamedTextColor.YELLOW)));
        }
        sendSupportMessage(sender);
    }

    public static void sendSupportMessage(CommandSender sender) {
        // Update checker isn't initialized if config.yml failed to load, so fall back
        // to a placeholder version string instead of crashing the command.
        UpdateManager updateChecker = UltraCosmeticsData.get().getPlugin().getUpdateChecker();
        String version = updateChecker != null
                ? updateChecker.getCurrentVersion().versionClassifierCommit()
                : "unknown (plugin not fully initialized)";
        sender.sendMessage("You are running UC " + version + " on " + Bukkit.getName() + " " + Bukkit.getVersion());
        Component discordMessage = Component.text("If you need help, click here to join the support Discord", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://discord.gg/mDSbzGPykk"));
        MessageManager.getAudiences().sender(sender).sendMessage(discordMessage);
        sender.sendMessage(ChatColor.GREEN + "When you join, share a screenshot of this message.");
    }
}
