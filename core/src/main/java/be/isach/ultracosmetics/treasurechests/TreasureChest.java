package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.treasurechests.loot.LootReward;
import be.isach.ultracosmetics.util.Area;
import be.isach.ultracosmetics.util.BlockUtils;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import be.isach.ultracosmetics.util.StructureRollback;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

public class TreasureChest implements Listener {

    private final StructureRollback rollback;
    private final List<Block> unopenedChests = new ArrayList<>();
    private final StructureRollback chestRollback = new StructureRollback();
    private final UUID owner;
    private final TreasureRandomizer randomGenerator;
    private final Location center;
    private final Block centerBlock;
    private final XParticle particleEffect;
    private int chestsLeft = SettingsManager.getConfig().getInt("TreasureChests.Count", 4);
    private final Player player;
    private final Set<Item> items = new HashSet<>();
    private final Set<ArmorStand> holograms = new HashSet<>();
    private boolean stopping;
    private boolean cooldown = false;
    private final TreasureChestDesign design;
    private final Location preLoc;
    private final TreasureLocation treasureLoc;
    private final boolean large;
    private final PlaceBlocksRunnable blocksRunnable;
    private final boolean allowDamage;

    public TreasureChest(UUID owner, final TreasureChestDesign design, Location preLoc, TreasureLocation destLoc) {
        this.design = design;
        this.particleEffect = design.getEffect();
        this.owner = owner;
        this.preLoc = preLoc;
        this.treasureLoc = destLoc;
        large = SettingsManager.getConfig().getBoolean("TreasureChests.Large");
        allowDamage = SettingsManager.getConfig().getBoolean("TreasureChests.Allow-Damage");
        if (chestsLeft > 4) {
            if (large && chestsLeft > 12) {
                chestsLeft = 12;
            } else if (!large) {
                chestsLeft = 4;
            }
        }
        if (chestsLeft < 1) chestsLeft = 1;

        UltraCosmetics uc = UltraCosmeticsData.get().getPlugin();
        UltraPlayerManager pm = uc.getPlayerManager();

        Bukkit.getPluginManager().registerEvents(this, uc);

        this.player = getPlayer();

        centerBlock = player.getLocation().getBlock();
        center = centerBlock.getLocation();
        rollback = new StructureRollback(new Area(center.clone().subtract(0, 1, 0), large ? 3 : 2, 3));
        if (!BlockUtils.isAir(centerBlock.getType())) {
            rollback.setToRestore(centerBlock, Material.AIR);
        }

        if (pm.getUltraPlayer(getPlayer()).getCurrentMorph() != null) {
            pm.getUltraPlayer(getPlayer()).setSeeSelfMorph(false);
        }

        this.randomGenerator = new TreasureRandomizer(getPlayer(), getPlayer().getLocation());

        blocksRunnable = new PlaceBlocksRunnable(this);
        blocksRunnable.schedule();

        UltraCosmeticsData.get().getPlugin().getScheduler().runAtEntityLater(player, () -> {
            if (pm.getUltraPlayer(player) != null && pm.getUltraPlayer(player).getCurrentTreasureChest() == TreasureChest.this) {
                forceOpen(45);
            }
        }, 1200L);

        pm.getUltraPlayer(getPlayer()).setCurrentTreasureChest(this);

        new PlayerBounceRunnable(this).schedule();
    }

    public Player getPlayer() {
        if (owner != null) {
            return Bukkit.getPlayer(owner);
        }
        return null;
    }

    public void clear() {
        rollback.rollback();
        if (stopping) {
            cleanup();
        } else {
            UltraCosmeticsData.get().getPlugin().getScheduler().runAtEntityLater(getPlayer(), this::cleanup, 30L);
        }
    }

    private void cleanup() {
        for (ArmorStand hologram : holograms) {
            hologram.remove();
        }
        for (Item item : items) {
            item.remove();
        }
        cancelRunnables();
        items.clear();
        unopenedChests.clear();
        holograms.clear();
        rollback.cleanup();
        chestRollback.cleanup();
        if (getPlayer() != null) {
            UltraCosmeticsData.get().getPlugin().getPlayerManager().getUltraPlayer(getPlayer()).setCurrentTreasureChest(null);
            if (preLoc != null) {
                UltraCosmeticsData.get().getPlugin().getScheduler().teleportAsync(getPlayer(), preLoc);
            }
        }
        HandlerList.unregisterAll(this);
    }

    private void cancelRunnables() {
        // cancels all child runnables as well
        blocksRunnable.propagateCancel();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer() == getPlayer() && !event.getTo().getBlock().equals(centerBlock)) {
            event.setCancelled(true);
            UltraCosmeticsData.get().getPlugin().getScheduler().teleportAsync(getPlayer(), event.getFrom());
        }
    }

    public void forceOpen(int delay) {
        if (delay == 0) {
            stopping = true;
            for (int i = 0; i < chestsLeft; i++) {
                // Skip firework to avoid running EntitySpawner tasks on shutdown
                LootReward reward = randomGenerator.giveRandomThing(this, true);
                String[] names = reward.getName();
                MessageManager.send(getPlayer(), "You-Won-Treasure-Chests",
                        Placeholder.unparsed("name", names[names.length - 1])
                );
            }
            return;
        }

        for (final Block b : unopenedChests) {
            setLidPosition(b, true);
            randomGenerator.setLocation(b.getLocation().clone().add(0.0D, 1.0D, 0.0D));
            LootReward reward = randomGenerator.giveRandomThing(this, false);

            items.add(spawnItem(reward.getStack(), b.getLocation()));
            UltraCosmeticsData.get().getPlugin().getScheduler().runAtLocationLater(b.getLocation(), () -> makeHolograms(b.getLocation(), reward), 15L);

            chestsLeft -= 1;
        }
        unopenedChests.clear();

        UltraCosmeticsData.get().getPlugin().getScheduler().runAtEntityLater(getPlayer(), this::clear, delay);
    }

    private void makeHolograms(Location location, LootReward reward) {
        String[] names = reward.getName();
        Location loc = location.clone().add(0.5, 0.3, 0.5);
        for (int i = names.length - 1; i >= 0; i--) {
            spawnHologram(loc, names[i]);
            loc.add(0, 0.25, 0);
        }
    }

    private ArmorStand spawnHologram(Location location, String s) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setSmall(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setCustomName(s);
        armorStand.setCustomNameVisible(true);
        armorStand.setPersistent(false);
        armorStand.setMetadata("C_AD_ArmorStand", new FixedMetadataValue(UltraCosmeticsData.get().getPlugin(), "C_AD_ArmorStand"));
        holograms.add(armorStand);
        return armorStand;
    }

    public boolean isSpecialEntity(Entity entity) {
        return items.contains(entity) || holograms.contains(entity);
    }

    public static void setLidPosition(Block block, boolean open) {
        // Lidded API didn't exist until 1.16,
        // and EnderChest wasn't lidded until 1.19!
        try {
            Lidded state = (Lidded) block.getState();
            if (open) {
                state.open();
            } else {
                state.close();
            }
            ((BlockState) state).update();
        } catch (NoClassDefFoundError | ClassCastException ignored) {
        }
    }

    @EventHandler
    public void onInter(final PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (rollback.containsBlock(block)) {
            event.setCancelled(true);
            return;
        }
        if (!chestRollback.containsBlock(block)) return;
        event.setCancelled(true);
        if (!unopenedChests.contains(block)) return;
        if (event.getPlayer() != getPlayer() || cooldown) return;

        setLidPosition(block, true);
        Location loc = block.getLocation();
        randomGenerator.setLocation(loc.clone().add(0.0D, 1.0D, 0.0D));
        LootReward reward = randomGenerator.giveRandomThing(this, false);

        cooldown = true;
        UltraCosmeticsData.get().getPlugin().getScheduler().runLaterAsync(() -> cooldown = false, 3L);

        ItemStack is = reward.getStack();

        items.add(spawnItem(is, loc));
        UltraCosmeticsData.get().getPlugin().getScheduler().runAtLocationLater(loc, () -> makeHolograms(loc, reward), 15L);

        chestsLeft -= 1;
        unopenedChests.remove(block);
        if (chestsLeft == 0) {
            UltraCosmeticsData.get().getPlugin().getScheduler().runAtLocationLater(loc, this::clear, 50L);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!allowDamage && event.getEntity() == getPlayer()) {
            event.setCancelled(true);
        }
    }

    private Item spawnItem(ItemStack stack, Location loc) {
        return ItemFactory.spawnUnpickableItem(stack, loc.clone().add(0.5, 1.2, 0.5), new Vector(0, 0.25, 0));
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getPlayer() == getPlayer() && event.getReason().equals("Flying is not enabled on this server")) {
            UltraCosmeticsData.get().getPlugin().getSmartLogger().write(LogLevel.INFO, "Cancelled flight kick while opening treasure chest");
            event.setCancelled(true);
            UltraCosmeticsData.get().getPlugin().getScheduler().teleportAsync(getPlayer(), center);
        }
    }

    public TreasureLocation getTreasureLocation() {
        return treasureLoc;
    }

    public XParticle getParticleEffect() {
        return particleEffect;
    }

    public Location getCenter() {
        return center.clone();
    }

    public TreasureChestDesign getDesign() {
        return design;
    }

    public void addChest(Block b, Material newType) {
        unopenedChests.add(b);
        chestRollback.setToRestore(b, newType);
    }

    public void addRestoreBlock(Block b, XMaterial newType) {
        rollback.setToRestore(b, newType);
    }

    public int getChestsLeft() {
        return chestsLeft;
    }

    /**
     * Cancel eggs from merging
     */
    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (items.contains(event.getEntity()) || items.contains(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    public boolean isLarge() {
        return large;
    }
}
