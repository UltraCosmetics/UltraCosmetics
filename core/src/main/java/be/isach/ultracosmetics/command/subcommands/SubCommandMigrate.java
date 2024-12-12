package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.mysql.tables.PlayerDataTable;
import be.isach.ultracosmetics.player.profile.PlayerData;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubCommandMigrate extends SubCommand {

    public SubCommandMigrate(UltraCosmetics ultraCosmetics) {
        super("migrate", "Moves player data from flatfile to MySQL and vice versa", "<flatfile|sql>", ultraCosmetics);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            error(sender, "This command can only be used from the console.");
            return;
        }
        if (UltraCosmeticsData.get().usingFileStorage()) {
            error(sender, "SQL must be enabled and connected to migrate either direction.");
            return;
        }
        if (args.length < 2 || (!args[1].equalsIgnoreCase("flatfile") && !args[1].equalsIgnoreCase("sql"))) {
            badUsage(sender);
            return;
        }
        boolean flatfile = args[1].equalsIgnoreCase("flatfile");
        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            if (Bukkit.getOnlinePlayers().size() > 0) {
                error(sender, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                error(sender, "  It is HIGHLY recommended migration is only  ");
                error(sender, "performed when no players are online, but you ");
                error(sender, "           can decide for yourself!           ");
                error(sender, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            String from = flatfile ? "SQL database" : "flatfile";
            String to = flatfile ? "flatfile" : "SQL database";
            error(sender, "You are migrating from " + from + " to " + to);
            error(sender, "Warning: any conflicting data in " + to + " will be overwritten.");
            error(sender, "If you want to proceed, use /uc migrate " + args[1] + " confirm");
            return;
        }
        ultraCosmetics.getScheduler().runAsync((task) -> migrate(flatfile));
    }

    private void migrate(boolean flatfile) {
        ultraCosmetics.getSmartLogger().write("Loading UUIDs to migrate...");
        List<UUID> uuids;
        if (flatfile) {
            uuids = getUUIDsFromSql();
        } else {
            uuids = getUUIDsFromFlatfile();
        }
        if (uuids == null) return; // error already printed
        ultraCosmetics.getSmartLogger().write("Found " + uuids.size() + " UUIDs, starting migration");
        PlayerData data;
        for (UUID uuid : uuids) {
            data = new PlayerData(uuid);
            if (flatfile) {
                data.loadFromSQL();
                data.saveToFile();
            } else {
                data.loadFromFile();
                data.saveToSQL();
            }
            ultraCosmetics.getSmartLogger().write("Successfully migrated " + uuid.toString());
        }
        ultraCosmetics.getSmartLogger().write(uuids.size() + " UUIDs successfully migrated!");
        if (flatfile) {
            ultraCosmetics.getSmartLogger().write("Set `MySQL.Enabled` to `false` in the config to complete the switch to flatfile storage.");
        }
    }

    private List<UUID> getUUIDsFromFlatfile() {
        List<UUID> uuids = new ArrayList<>();
        File dataFolder = new File(ultraCosmetics.getDataFolder(), "data");
        for (File file : dataFolder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            String baseName = file.getName().substring(0, file.getName().length() - 4);
            try {
                uuids.add(UUID.fromString(baseName));
            } catch (IllegalArgumentException e) {
                ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Failed to parse UUID of flatfile '" + file.getName() + "', ignoring");
            }
        }
        return uuids;
    }

    private List<UUID> getUUIDsFromSql() {
        PlayerDataTable table = ultraCosmetics.getMySqlConnectionManager().getPlayerData();
        return table.select("uuid_text").unsafe().getResults(r -> {
            List<UUID> uuids = new ArrayList<>();
            while (r.next()) {
                uuids.add(UUID.fromString(r.getString("uuid_text")));
            }
            return uuids;
        }, true);
    }

    @Override
    protected void tabComplete(CommandSender sender, String[] args, List<String> options) {
        if (args.length == 2) {
            options.add("flatfile");
            options.add("sql");
        }

    }
}
