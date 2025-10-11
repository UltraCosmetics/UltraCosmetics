package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubCommandPermission extends SubCommand {

    public SubCommandPermission(UltraCosmetics ultraCosmetics) {
        super("permission", "Unlocks or locks a cosmetic for you or another user", "<add|remove> <category|*> <cosmetic|*> [player]", ultraCosmetics);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        if (args.length < 4) {
            badUsage(sender);
            return;
        }

        boolean unlock;
        if (args[1].equalsIgnoreCase("add")) {
            unlock = true;
        } else if (args[1].equalsIgnoreCase("remove")) {
            if (!ultraCosmetics.getPermissionManager().isUnsetSupported()) {
                // ChatColor.RED to remove bold
                error(sender, ChatColor.RED + "Removing cosmetic permissions through UC is only supported when UC is storing unlocked cosmetics.");
                error(sender, ChatColor.RED + "Please use your permission plugin commands to remove cosmetic permissions instead.");
                return;
            }
            unlock = false;
        } else {
            badUsage(sender);
            return;
        }

        Player target;
        if (args.length == 4) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                MessageManager.send(sender, "Must-Specify-Player");
                return;
            }
        } else {
            target = Bukkit.getPlayer(args[4]);
            if (target == null) {
                MessageManager.send(sender, "Invalid-Player");
                return;
            }
        }
        Set<CosmeticType<?>> cosmetics = new HashSet<>();
        if (args[2].equals("*")) {
            if (!args[3].equals("*")) {
                error(sender, "Cannot set specific cosmetic in wildcard category");
                return;
            }
            Category.forEachCosmetic(cosmetics::add);
        } else {
            Category cat = Category.fromString(args[2]);
            if (cat == null) {
                error(sender, "No such category: " + args[2]);
                return;
            }
            if (args[3].equals("*")) {
                cosmetics.addAll(cat.getValues());
            } else {
                CosmeticType<?> type = cat.valueOfType(args[3]);
                if (type == null) {
                    error(sender, "No such cosmetic: " + args[3]);
                    return;
                }
                cosmetics.add(type);
            }
        }

        if (unlock) {
            ultraCosmetics.getPermissionManager().setPermissions(target, cosmetics);
        } else {
            ultraCosmetics.getPermissionManager().unsetPermissions(target, cosmetics);
        }

        sender.sendMessage(ChatColor.GREEN + "Success!");
    }

    @Override
    protected void tabComplete(CommandSender sender, String[] args, List<String> options) {
        if (args.length == 2) {
            options.add("add");
            options.add("remove");
        } else if (args.length == 3) {
            addCategories(options);
            options.add("*");
        } else if (args.length == 4) {
            Category cat = Category.fromString(args[2]);
            if (cat == null || !cat.isEnabled()) return;
            for (CosmeticType<?> type : cat.getEnabled()) {
                options.add(type.getConfigName());
            }
            options.add("*");
        } else if (args.length == 5) {
            addPlayers(options);
        }
    }

}
