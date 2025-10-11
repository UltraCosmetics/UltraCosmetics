package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Clear {@link be.isach.ultracosmetics.command.SubCommand SubCommand}.
 *
 * @author iSach
 * @author RadBuilder
 * @since 12-21-2015
 */
public class SubCommandToggle extends SubCommand {
    private static final String ERROR_PREFIX = " " + ChatColor.RED + ChatColor.BOLD;

    public SubCommandToggle(UltraCosmetics ultraCosmetics) {
        super("toggle", "Toggles a cosmetic.", "[<type> <cosmetic>] [player]", ultraCosmetics, true);
    }

    @Override
    protected void onExePlayer(Player sender, String[] args) {
        // Usage patterns:
        // /uc toggle
        // /uc toggle Player
        // /uc toggle hats a
        // /uc toggle hats a Player
        if (args.length < 1 || args.length > 4) {
            badUsage(sender);
            return;
        }

        Player target;
        if (args.length == 2 || args.length == 4) {
            // null check later
            target = Bukkit.getPlayer(args[args.length - 1]);
        } else {
            target = sender;
        }

        if (args.length < 3) {
            toggleAll(sender, target);
        } else {
            toggle(sender, target, args[1].toLowerCase(Locale.ROOT), args[2].toLowerCase(Locale.ROOT));
        }
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 4) {
            badUsage(sender, "/uc toggle [<type> <cosmetic>] <player>");
            return;
        }

        toggle(sender, Bukkit.getPlayer(args[3]), args[1].toLowerCase(Locale.ROOT), args[2].toLowerCase(Locale.ROOT));
    }

    private UltraPlayer commonChecks(CommandSender sender, Player targetPlayer) {
        if (sender != targetPlayer && !sender.hasPermission(getPermission().getName() + ".others")) {
            MessageManager.send(sender, "No-Permission");
            return null;
        }

        UltraPlayer target = ultraCosmetics.getPlayerManager().getUltraPlayer(targetPlayer);
        if (target == null) {
            MessageManager.send(sender, "Invalid-Player");
            return null;
        }

        if (!SettingsManager.isAllowedWorld(target.getBukkitPlayer().getWorld())) {
            MessageManager.send(sender, "World-Disabled");
            return null;
        }

        return target;
    }

    private void toggleAll(CommandSender sender, Player targetPlayer) {
        UltraPlayer target = commonChecks(sender, targetPlayer);
        if (target == null) {
            return;
        }

        if (!target.getProfile().hasAnyEquipped()) {
            error(sender, "Please specify the cosmetic to toggle");
            return;
        }

        if (target.hasCosmeticsEquipped()) {
            target.withPreserveEquipped(target::clear);
            MessageManager.send(sender, "Cosmetics-Toggled-Off");
        } else {
            target.getProfile().equip();
            MessageManager.send(sender, "Cosmetics-Toggled-On");
        }
    }

    private void toggle(CommandSender sender, Player targetPlayer, String type, String cosm) {
        UltraPlayer target = commonChecks(sender, targetPlayer);
        if (target == null) {
            return;
        }

        Optional<Category> categories = Arrays.stream(Category.values()).filter(category -> category.isEnabled() && category.toString().toLowerCase(Locale.ROOT).startsWith(type)).findFirst();
        if (categories.isEmpty()) {
            MessageManager.send(sender, "Invalid-Category");
            return;
        }
        Category category = categories.get();
        CosmeticType<?> matchingType = findCosmetic(category, cosm);
        if (matchingType == null) {
            MessageManager.send(sender, "Invalid-Cosmetic");
            return;
        }
        if (target.getCosmetic(category) != null && matchingType == target.getCosmetic(category).getType()) {
            target.removeCosmetic(category);
        } else {
            matchingType.equip(target, ultraCosmetics);
        }
    }

    private CosmeticType<?> findCosmetic(Category category, String partialName) {
        for (CosmeticType<?> type : category.getEnabled()) {
            if (type.toString().equalsIgnoreCase(partialName)) {
                return type;
            }
        }
        for (CosmeticType<?> type : category.getEnabled()) {
            if (type.toString().startsWith(partialName.toUpperCase(Locale.ROOT))) {
                return type;
            }
        }
        return null;
    }

    @Override
    protected void tabComplete(CommandSender sender, String[] args, List<String> options) {
        if (args.length == 2) {
            addCategories(options);
        } else if (args.length == 3) {
            Category cat = Category.fromString(args[1]);

            if (cat == null || !cat.isEnabled()) return;

            for (CosmeticType<?> cosm : cat.getEnabled()) {
                options.add(cosm.toString());
            }
        } else if (args.length == 4) {
            addPlayers(options);
        }
    }
}
