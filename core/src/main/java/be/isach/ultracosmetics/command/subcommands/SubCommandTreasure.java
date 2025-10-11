package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.treasurechests.TreasureLocation;
import be.isach.ultracosmetics.treasurechests.TreasureRandomizer;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.version.VersionManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Treasure {@link be.isach.ultracosmetics.command.SubCommand SubCommand}.
 *
 * @author iSach
 * @since 12-22-2015
 */
public class SubCommandTreasure extends SubCommand {

    public SubCommandTreasure(UltraCosmetics ultraCosmetics) {
        super("treasure", "Starts Treasure Chest.", "[player] [<x> <y> <z>] [world]", ultraCosmetics);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        if (args.length < 2 && !(sender instanceof Player)) {
            MessageManager.send(sender, "Must-Specify-Player");
            return;
        }

        if (args.length > 6) {
            badUsage(sender);
            return;
        }

        Player opener;
        // form: /uc treasure
        if (args.length == 1) {
            opener = (Player) sender;
            // form: /uc treasure (player) [...]
        } else {
            opener = Bukkit.getPlayer(args[1]);
            if (opener == null) {
                MessageManager.send(sender, "Invalid-Player");
                return;
            }
        }

        UltraPlayer ultraPlayer = UltraCosmeticsData.get().getPlugin().getPlayerManager().getUltraPlayer(opener);

        if (ultraPlayer.getKeys() <= 0) {
            opener.closeInventory();
            ultraCosmetics.getMenus().openKeyPurchaseMenu(ultraPlayer);
            return;
        }

        boolean structureEnabled = !SettingsManager.getConfig().getString("TreasureChests.Mode", "").equalsIgnoreCase("simple");
        // form: /uc treasure (player)
        if (args.length < 3) {
            if (!checkWorld(sender, opener.getWorld())) return;
            if (structureEnabled && ultraCosmetics.getTreasureChestManager().tryOpenChest(opener)) {
                return;
            }
            ultraPlayer.removeKey();
            TreasureRandomizer tr = new TreasureRandomizer(opener, opener.getLocation().subtract(1, 0, 1), true);
            for (int i = 0; i < SettingsManager.getConfig().getInt("TreasureChests.Count", 4); i++) {
                tr.giveRandomThing(null, false);
            }
            return;
        }

        if (!structureEnabled) {
            MessageManager.send(sender, "Structure-Chests-Disabled");
            return;
        }

        int x, y, z;

        try {
            x = Integer.parseInt(args[2]);
            y = Integer.parseInt(args[3]);
            z = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Invalid coordinates!");
            return;
        }

        World world;
        // form: /uc treasure (player) (x) (y) (z) (world)
        if (args.length > 5) {
            world = Bukkit.getWorld(args[5]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "World " + args[5] + " doesn't exist!");
                return;
            }
            // form: /uc treasure (player) (x) (y) (z)
        } else {
            world = opener.getWorld();
        }

        // Don't accept equal to world boundaries because treasure chests have to place blocks on player Y-1 through Y+1
        if (y >= VersionManager.getWorldMaxHeight(world) || y <= VersionManager.getWorldMinHeight(world)) {
            MessageManager.send(sender, "Chest-Location.Invalid");
            return;
        }

        Location location = new Location(world, x + 0.5, y, z + 0.5);
        Block block = location.getBlock();
        if (!isAir(block)) {
            MessageManager.send(sender, "Chest-Location.In-Ground");
            for (int i = y; i < VersionManager.getWorldMaxHeight(world); i++) {
                if (isAir(block.getWorld().getBlockAt(x, i, z))) {
                    suggest(x, i, z, sender);
                    break;
                }
            }
            return;
        }

        if (isAir(block.getRelative(BlockFace.DOWN))) {
            MessageManager.send(sender, "Chest-Location.In-Air");
            for (int i = y; i > VersionManager.getWorldMinHeight(world); i--) {
                if (!isAir(block.getWorld().getBlockAt(x, i, z))) {
                    // we found the ground, back up 1
                    suggest(x, i + 1, z, sender);
                    break;
                }
            }
            return;
        }

        ultraCosmetics.getTreasureChestManager().tryOpenChest(opener, TreasureLocation.fromLocation(location));
    }

    private boolean checkWorld(CommandSender sender, World world) {
        if (SettingsManager.isAllowedWorld(world)) return true;
        MessageManager.send(sender, "World-Disabled");
        return false;
    }

    private void suggest(int x, int y, int z, CommandSender sender) {
        MessageManager.send(sender, "Chest-Location.Suggestion",
                Placeholder.unparsed("location", x + "," + y + "," + z)
        );
    }

    private boolean isAir(Block block) {
        return BlockUtils.isAir(block.getType());
    }

    @Override
    protected void tabComplete(CommandSender sender, String[] args, List<String> options) {
        if (args.length == 2) {
            addPlayers(options);
        } else if (args.length == 6) {
            for (World world : Bukkit.getWorlds()) {
                options.add(world.getName());
            }
        }

    }
}
