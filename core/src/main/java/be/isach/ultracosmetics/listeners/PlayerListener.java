package be.isach.ultracosmetics.listeners;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.suits.ArmorSlot;
import be.isach.ultracosmetics.menu.MenuItemHandler;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.player.UltraPlayerManager;
import be.isach.ultracosmetics.run.FallDamageManager;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Player listeners.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class PlayerListener implements Listener {

    private final UltraCosmetics ultraCosmetics;
    private final UltraPlayerManager pm;
    private final boolean menuItemEnabled = SettingsManager.getConfig().getBoolean("Menu-Item.Enabled");
    private final int menuItemSlot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
    private final long joinItemDelay = SettingsManager.getConfig().getLong("Item-Delay.Join", 1);
    private final long respawnItemDelay = SettingsManager.getConfig().getLong("Item-Delay.World-Change-Or-Respawn", 0);
    private final boolean updateOnWorldChange = SettingsManager.getConfig().getBoolean("Always-Update-Cosmetics-On-World-Change", false);
    private final boolean preventCommandsDuringChests = SettingsManager.getConfig().getBoolean("TreasureChests.Prevent-Commands-While-Opening", false);

    public PlayerListener(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        this.pm = ultraCosmetics.getPlayerManager();
    }

    private boolean isNPC(Player player) {
        return player.hasMetadata("NPC") || player.hasMetadata("fake-player");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreJoin(final PlayerJoinEvent event) {
        if (isNPC(event.getPlayer())) return;
        // Ready UltraPlayer as early as possible so it can be ready for other plugins that might also run code on join
        pm.createUltraPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        if (isNPC(event.getPlayer())) return;
        UltraPlayer ultraPlayer = pm.getUltraPlayer(event.getPlayer());
        if (SettingsManager.isAllowedWorld(event.getPlayer().getWorld())) {
            runWhenValid(event.getPlayer(), joinItemDelay, ultraPlayer::load);
        }

        if (ultraCosmetics.getUpdateChecker() != null && ultraCosmetics.getUpdateChecker().isOutdated()) {
            if (event.getPlayer().hasPermission("ultracosmetics.updatenotify")) {
                Component prefix = MessageManager.getMessage("Prefix");
                ultraPlayer.sendMessage(Component.empty().append(prefix).append(Component.text("An update is available: "
                        + ultraCosmetics.getUpdateChecker().getSpigotVersion(), NamedTextColor.RED, TextDecoration.BOLD)));
                Component use = Component.text("Use ", NamedTextColor.RED, TextDecoration.BOLD);
                Component command = Component.text("/uc update", NamedTextColor.YELLOW);
                Component toInstall = Component.text(" to install the update.", NamedTextColor.RED, TextDecoration.BOLD);
                ultraPlayer.sendMessage(Component.empty().append(prefix).append(use).append(command).append(toInstall));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        if (isNPC(event.getPlayer())) return;
        if (!SettingsManager.isAllowedWorld(event.getPlayer().getWorld())) return;
        UltraPlayer up = pm.getUltraPlayer(event.getPlayer());
        if (menuItemEnabled && event.getPlayer().hasPermission("ultracosmetics.receivechest")) {
            ultraCosmetics.getScheduler().runAtEntityLater(event.getPlayer(), up::giveMenuItem, respawnItemDelay);
        }
        // If the player joined an allowed world from a non-allowed world
        // or we need to update their cosmetics for another reason, re-equip their cosmetics.
        if (!SettingsManager.isAllowedWorld(event.getFrom()) || updateOnWorldChange) {
            ultraCosmetics.getScheduler().runAtEntityLater(event.getPlayer(), () -> up.getProfile().equip(), respawnItemDelay);
        }
    }

    private void clearCosmeticsForWorldChange(Player player) {
        if (isNPC(player)) return;
        boolean goingToBadWorld = !SettingsManager.isAllowedWorld(player.getWorld());
        if (!goingToBadWorld && !updateOnWorldChange) {
            return;
        }
        UltraPlayer ultraPlayer = pm.getUltraPlayer(player);
        // Disable cosmetics when joining a bad world.
        ultraPlayer.removeMenuItem();
        ultraPlayer.withPreserveEquipped(() -> {
            // Clear cosmetics either way, but only display the message if player is going to a bad world.
            if (ultraPlayer.clear() && goingToBadWorld) {
                MessageManager.send(ultraPlayer.getBukkitPlayer(), "World-Disabled");
            }
        });
    }

    // run this as early as possible for compatibility with MV-inventories
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChangeEarly(final PlayerChangedWorldEvent event) {
        clearCosmeticsForWorldChange(event.getPlayer());
    }

    // MyWorlds uses the PlayerTeleportEvent to handle world changes instead for some reason.
    // Not sure if both this and onWorldChangeEarly are needed, but just in case...
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() != null && event.getFrom().getWorld() != event.getTo().getWorld()) {
            clearCosmeticsForWorldChange(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if (isNPC(event.getPlayer())) return;
        // When PlayerRespawnEvent is being called, the player may or may not be at
        // the final respawn location, so wait one tick before re-equipping.
        runWhenValid(event.getPlayer(), Math.max(1, respawnItemDelay), () -> {
            if (!SettingsManager.isAllowedWorld(event.getPlayer().getWorld())) return;
            UltraPlayer ultraPlayer = pm.getUltraPlayer(event.getPlayer());
            if (menuItemEnabled) {
                ultraPlayer.giveMenuItem();
            }
            ultraPlayer.getProfile().equip();
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Dispose even for NPCs, if we did accidentally allocate an UltraPlayer for an NPC, we don't want to be leaking
        // memory
        pm.getUltraPlayer(event.getPlayer()).dispose();
        UUID uuid = event.getPlayer().getUniqueId();
        // workaround plugins calling events after player quit
        ultraCosmetics.getScheduler().runLater(() -> pm.remove(uuid), 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        // Ignore NPC deaths as per iSach#467
        if (isNPC(event.getEntity())) return;
        if (MenuItemHandler.isMenuItem(event.getEntity().getInventory().getItem(menuItemSlot))) {
            event.getDrops().remove(event.getEntity().getInventory().getItem(menuItemSlot));
            event.getEntity().getInventory().setItem(menuItemSlot, null);
        }
        UltraPlayer ultraPlayer = pm.getUltraPlayer(event.getEntity());
        if (ultraPlayer.getCurrentGadget() != null) {
            event.getDrops().remove(ultraPlayer.getCurrentGadget().getItemStack());
        }
        if (ultraPlayer.getCurrentHat() != null) event.getDrops().remove(ultraPlayer.getCurrentHat().getItemStack());
        Arrays.asList(ArmorSlot.values()).forEach(armorSlot -> {
            if (ultraPlayer.getCurrentSuit(armorSlot) != null) {
                event.getDrops().remove(ultraPlayer.getCurrentSuit(armorSlot).getItemStack());
            }
        });
        if (ultraPlayer.getCurrentEmote() != null) {
            event.getDrops().remove(ultraPlayer.getCurrentEmote().getItemStack());
        }

        ultraPlayer.withPreserveEquipped(() -> {
            for (Category cat : Category.values()) {
                if (cat.isClearOnDeath()) {
                    ultraPlayer.removeCosmetic(cat);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && FallDamageManager.shouldBeProtected(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework && event.getDamager().hasMetadata("uc_firework")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickUpItem(EntityPickupItemEvent event) {
        if (MenuItemHandler.isMenuItem(event.getItem().getItemStack())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("ultracosmetics.bypass.disabledcommands")) return;
        UltraPlayer player = pm.getUltraPlayer(event.getPlayer());
        if (preventCommandsDuringChests && player.getCurrentTreasureChest() != null) {
            event.setCancelled(true);
            MessageManager.send(event.getPlayer(), "Commands-Disabled-During-Chest");
            return;
        }
        String strippedCommand = event.getMessage().split(" ")[0].replace("/", "").toLowerCase(Locale.ROOT);
        if (!SettingsManager.getConfig().getList("Disabled-Commands").contains(strippedCommand)) return;
        if (player.hasCosmeticsEquipped()) {
            event.setCancelled(true);
            MessageManager.send(event.getPlayer(), "Disabled-Command-Message");
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        UltraPlayer ultraPlayer = pm.getUltraPlayer(event.getPlayer());
        if (ultraPlayer.getCurrentGadget() != null && ultraPlayer.getCurrentGadget().getItemStack().equals(event.getItem())) {
            event.setCancelled(true);
            return;
        }
        for (ArmorSlot armorSlot : ArmorSlot.values()) {
            if (ultraPlayer.getCurrentSuit(armorSlot) != null) {
                if (event.getItem().equals(ultraPlayer.getCurrentSuit(armorSlot).getItemStack())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void runWhenValid(Player player, long minDelay, Runnable runnable) {
        if (player.isValid()) {
            ultraCosmetics.getScheduler().runAtEntityLater(player, runnable, minDelay);
            runnable.run();
            return;
        }
        // Allow a mutable value to be referenced inside a lambda
        final long[] counter = {0};
        final WrappedTask[] task = {null};
        task[0] = ultraCosmetics.getScheduler().runTimer(() -> {
            if (player.isValid()) {
                ultraCosmetics.getScheduler().runAtEntity(player, t -> runnable.run());
                task[0].cancel();
                return;
            }
            if (counter[0]++ > 10) {
                // They probably disconnected, give up
                task[0].cancel();
            }
        }, minDelay, 1);
    }
}
