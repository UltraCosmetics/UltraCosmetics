package be.isach.ultracosmetics.hook;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.player.UltraPlayer;
import me.lightly.survival.api.event.combat.PlayerEnterCombatEvent;
import me.lightly.survival.api.event.combat.PlayerExitCombatEvent;
import me.lightly.survival.api.event.staff.PlayerUnvanishEvent;
import me.lightly.survival.api.event.staff.PlayerVanishEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Bridges Lightly's combat-log and staff-vanish events into UltraCosmetics' cosmetic
 * suspend/restore flow. Suspended cosmetics are physically cleared but their profile
 * record is preserved (via {@link UltraPlayer#withPreserveEquipped}), so restoring is
 * a straight {@code type.equip(...)} of what the profile still remembers.
 *
 * Combat: config-driven category set + per-cosmetic overrides (see
 * {@code Combat-Log-Integration.*} in config.yml).
 * Vanish: hard-suspends everything a staff player has equipped (no per-category config).
 */
public class LightlyIntegrationListener implements Listener {
    private static final String COMBAT_ROOT = "Combat-Log-Integration";
    private static final String VANISH_ROOT = "Vanish-Integration";

    private final UltraCosmetics uc;
    private final Map<UUID, Set<Category>> combatSuspended = new HashMap<>();
    private final Map<UUID, Set<Category>> vanishSuspended = new HashMap<>();

    public LightlyIntegrationListener(UltraCosmetics uc) {
        this.uc = uc;
    }

    @EventHandler
    public void onEnterCombat(PlayerEnterCombatEvent event) {
        if (!SettingsManager.getConfig().getBoolean(COMBAT_ROOT + ".Enabled")) return;
        UltraPlayer up = uc.getPlayerManager().getUltraPlayer(event.getPlayer());
        if (up == null) return;
        Set<Category> suspended = suspend(up, this::shouldSuspendInCombat);
        if (!suspended.isEmpty()) combatSuspended.put(up.getUUID(), suspended);
    }

    @EventHandler
    public void onExitCombat(PlayerExitCombatEvent event) {
        Set<Category> cats = combatSuspended.remove(event.getPlayer().getUniqueId());
        if (cats == null) return;
        // DEATH clears cosmetics via UC's own death handling; LOGOUT means the player
        // isn't around to see them anyway (and dispose() will run). Only restore on
        // TIMEOUT and WORLD_CHANGE, where the player stays online in a valid world.
        PlayerExitCombatEvent.ExitReason reason = event.getReason();
        if (reason == PlayerExitCombatEvent.ExitReason.DEATH
                || reason == PlayerExitCombatEvent.ExitReason.LOGOUT) {
            return;
        }
        UltraPlayer up = uc.getPlayerManager().getUltraPlayer(event.getPlayer());
        if (up == null) return;
        restore(up, cats);
    }

    @EventHandler
    public void onVanish(PlayerVanishEvent event) {
        if (!SettingsManager.getConfig().getBoolean(VANISH_ROOT + ".Enabled")) return;
        UltraPlayer up = uc.getPlayerManager().getUltraPlayer(event.getPlayer());
        if (up == null) return;
        Runnable action = () -> {
            Set<Category> suspended = suspend(up, (cat, cosmetic) -> true);
            if (!suspended.isEmpty()) vanishSuspended.put(up.getUUID(), suspended);
        };
        // On reconnect, UC's profile may not be loaded yet — defer until it is.
        // For a normal /vanish, profile is already loaded; run immediately.
        if (event.isReconnect()) {
            up.getProfile().onLoad(p -> action.run());
        } else {
            action.run();
        }
    }

    @EventHandler
    public void onUnvanish(PlayerUnvanishEvent event) {
        Set<Category> cats = vanishSuspended.remove(event.getPlayer().getUniqueId());
        if (cats == null) return;
        UltraPlayer up = uc.getPlayerManager().getUltraPlayer(event.getPlayer());
        if (up == null) return;
        restore(up, cats);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        combatSuspended.remove(uuid);
        vanishSuspended.remove(uuid);
    }

    private Set<Category> suspend(UltraPlayer up, BiPredicate<Category, Cosmetic<?>> filter) {
        Set<Category> toSuspend = EnumSet.noneOf(Category.class);
        for (Category cat : Category.values()) {
            if (!up.hasCosmetic(cat)) continue;
            Cosmetic<?> cosmetic = up.getCosmetic(cat);
            if (filter.test(cat, cosmetic)) toSuspend.add(cat);
        }
        if (!toSuspend.isEmpty()) {
            up.withPreserveEquipped(() -> {
                for (Category cat : toSuspend) up.removeCosmetic(cat);
            });
        }
        return toSuspend;
    }

    private void restore(UltraPlayer up, Set<Category> cats) {
        if (!up.isOnline()) return;
        up.withPreserveEquipped(() -> {
            for (Category cat : cats) {
                if (!cat.isEnabled() || up.hasCosmetic(cat)) continue;
                CosmeticType<?> type = up.getProfile().getEnabledCosmetic(cat);
                if (type != null && type.isEnabled()) {
                    type.equip(up, uc);
                }
            }
        });
    }

    private boolean shouldSuspendInCombat(Category cat, Cosmetic<?> cosmetic) {
        String overridePath = COMBAT_ROOT + ".Overrides." + cat.getConfigPath()
                + "." + cosmetic.getType().getConfigName();
        if (SettingsManager.getConfig().isBoolean(overridePath)) {
            return SettingsManager.getConfig().getBoolean(overridePath);
        }
        return SettingsManager.getConfig().getBoolean(COMBAT_ROOT + ".Suspend-Categories." + cat.getConfigPath());
    }
}
