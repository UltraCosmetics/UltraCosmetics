package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.Area;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.SmartLogger;
import be.isach.ultracosmetics.util.StructureRollback;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Sacha on 11/11/15.
 */
public class TreasureChestManager implements Listener {

    private static final Random random = new Random();
    private final UltraCosmetics ultraCosmetics;
    private final List<TreasureLocation> TREASURE_LOCATIONS = new ArrayList<>();

    public TreasureChestManager(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        if (!SettingsManager.getConfig().getBoolean("TreasureChests.Locations.Enabled")) {
            return;
        }
        Set<String> locationNames = SettingsManager.getConfig().getConfigurationSection("TreasureChests.Locations").getKeys(false);
        for (String locationName : locationNames) {
            if (!SettingsManager.getConfig().isConfigurationSection("TreasureChests.Locations." + locationName)) {
                continue;
            }
            ConfigurationSection location = SettingsManager.getConfig().getConfigurationSection("TreasureChests.Locations." + locationName);
            String worldName = location.getString("World", "none");
            World world = null;
            if (!worldName.equals("none")) {
                world = Bukkit.getWorld(worldName);
                if (world == null) {
                    ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.ERROR, "Invalid world set for location " + locationName + ", using player world");
                }
            }
            TreasureLocation tloc = new TreasureLocation(world, location.getInt("X", 0), location.getInt("Y", 63), location.getInt("Z", 0));
            TREASURE_LOCATIONS.add(tloc);
        }
        if (TREASURE_LOCATIONS.size() == 0) {
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.WARNING, "No treasure chest locations are defined, the setting will be ignored");
        }
    }

    private String getRandomDesign() {
        Set<String> set = ultraCosmetics.getConfig().getConfigurationSection("TreasureChests.Designs").getKeys(false);
        List<String> list = new ArrayList<>(set);
        return list.get(random.nextInt(set.size()));
    }

    /*
     * Returns false if a chest could not be opened, but a simple mode chest may succeed.
     * Returns true for success or if the key purchase menu was opened.
     */
    public boolean tryOpenChest(Player player) {
        if (TREASURE_LOCATIONS.isEmpty()) {
            tryOpenChest(player, null);
            return true;
        }
        List<TreasureLocation> locations = new ArrayList<>(TREASURE_LOCATIONS);
        for (UltraPlayer up : ultraCosmetics.getPlayerManager().getUltraPlayers()) {
            if (up.getCurrentTreasureChest() != null) {
                locations.remove(up.getCurrentTreasureChest().getTreasureLocation());
            }
        }
        if (locations.isEmpty()) {
            MessageManager.send(player, "Treasure-Chest-Occupied");
            return false;
        }
        TreasureLocation tloc = locations.get(random.nextInt(locations.size()));
        return tryOpenChest(player, tloc);
    }

    /**
     * @see #tryOpenChest(Player)
     */
    public boolean tryOpenChest(Player player, TreasureLocation tpTo) {
        UltraPlayer ultraPlayer = ultraCosmetics.getPlayerManager().getUltraPlayer(player);
        if (ultraPlayer.getCurrentTreasureChest() != null) {
            return false;
        }

        if (ultraPlayer.getKeys() < 1) {
            player.closeInventory();
            ultraCosmetics.getMenus().openKeyPurchaseMenu(ultraPlayer);
            // Opening
            return true;
        }

        if (player.getVehicle() != null) {
            MessageManager.send(player, "Chest-Location.Dismount-First");
            return false;
        }

        Location targetLoc = tpTo == null ? player.getLocation() : tpTo.toLocation(player);

        boolean large = SettingsManager.getConfig().getBoolean("TreasureChests.Large");
        int range = large ? 3 : 2;
        Area area = new Area(targetLoc, range, 1);

        if (!area.isEmptyExcept(targetLoc.getBlock().getLocation())) {
            MessageManager.send(player, "Chest-Location.Not-Enough-Space");
            return false;
        }

        Area placeArea = new Area(targetLoc.clone().subtract(0, 1, 0), large ? 3 : 2, 0);
        // Also checks for treasure chests that may occupy the area in the future.
        if (StructureRollback.isBlockRollingBackInArea(placeArea)) {
            MessageManager.send(player, "Chest-Location.Not-Enough-Space");
            return false;
        }

        for (Entity ent : targetLoc.getWorld().getNearbyEntities(targetLoc, range, range, range)) {
            if (shouldPush(ultraPlayer, ent)) {
                player.closeInventory();
                MessageManager.send(player, "Chest-Location.Too-Close-To-Entity");
                return false;
            }
        }

        Block block = targetLoc.getBlock();
        if (!BlockUtils.isAir(block.getRelative(BlockFace.UP).getType())
                || BlockUtils.isAir(block.getRelative(BlockFace.DOWN).getType())) {
            MessageManager.send(player, "Gadgets.Rocket.Not-On-Ground");
            return false;
        }

        Location preLoc = null;
        if (tpTo != null) {
            preLoc = player.getLocation();
            tpTo.tpTo(player);
        }

        if (!ultraCosmetics.getWorldGuardManager().areChestsAllowedHere(player)) {
            player.closeInventory();
            MessageManager.send(player, "Chest-Location.Region-Disabled");
            if (preLoc != null) {
                ultraCosmetics.getScheduler().teleportAsync(player, preLoc);
            }
            return false;
        }

        ultraPlayer.removeKey();
        String designPath = getRandomDesign();
        player.closeInventory();
        new TreasureChest(player.getUniqueId(), new TreasureChestDesign(designPath), preLoc, tpTo);
        return true;
    }

    public static boolean shouldPush(UltraPlayer chestOwner, Entity entity) {
        if (chestOwner.getBukkitPlayer() == entity || !(entity instanceof LivingEntity)) return false;
        if (entity instanceof ArmorStand && !((ArmorStand) entity).isVisible()) return false;
        if (entity.hasMetadata("NPC")) return false;
        if (chestOwner.getCurrentPet() != null && entity == chestOwner.getCurrentPet().getEntity()) return false;
        return true;
    }
}
