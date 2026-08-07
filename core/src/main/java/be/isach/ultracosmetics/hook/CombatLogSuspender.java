package be.isach.ultracosmetics.hook;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.player.UltraPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Polls Lightly's combat state each tick window and suspends the player's cosmetics
 * per config while they are combat-tagged, restoring them when the tag clears. See
 * {@code Combat-Log-Integration.*} in config.yml for behavior tuning.
 */
public class CombatLogSuspender {
    private static final String ROOT = "Combat-Log-Integration";

    private final UltraCosmetics uc;
    private final CombatLogHook hook;
    private final Map<UUID, Set<Category>> suspended = new HashMap<>();

    public CombatLogSuspender(UltraCosmetics uc, CombatLogHook hook) {
        this.uc = uc;
        this.hook = hook;
    }

    public void start() {
        int period = Math.max(5, SettingsManager.getConfig().getInt(ROOT + ".Poll-Interval-Ticks"));
        uc.getScheduler().runTimer(this::tick, period, period);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UltraPlayer up = uc.getPlayerManager().getUltraPlayer(player);
            if (up == null) continue;
            boolean inCombat = hook.isInCombat(player);
            Set<Category> alreadySuspended = suspended.get(player.getUniqueId());
            if (inCombat && alreadySuspended == null) {
                suspend(up);
            } else if (!inCombat && alreadySuspended != null) {
                restore(up, alreadySuspended);
                suspended.remove(player.getUniqueId());
            }
        }
    }

    private void suspend(UltraPlayer up) {
        Set<Category> toSuspend = EnumSet.noneOf(Category.class);
        for (Category cat : Category.values()) {
            if (!up.hasCosmetic(cat)) continue;
            Cosmetic<?> cosmetic = up.getCosmetic(cat);
            if (shouldSuspend(cat, cosmetic.getType().getConfigName())) {
                toSuspend.add(cat);
            }
        }
        if (toSuspend.isEmpty()) return;
        up.withPreserveEquipped(() -> {
            for (Category cat : toSuspend) {
                up.removeCosmetic(cat);
            }
        });
        suspended.put(up.getUUID(), toSuspend);
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

    private boolean shouldSuspend(Category cat, String cosmeticConfigName) {
        String overridePath = ROOT + ".Overrides." + cat.getConfigPath() + "." + cosmeticConfigName;
        if (SettingsManager.getConfig().isBoolean(overridePath)) {
            return SettingsManager.getConfig().getBoolean(overridePath);
        }
        return SettingsManager.getConfig().getBoolean(ROOT + ".Suspend-Categories." + cat.getConfigPath());
    }

    public void onPlayerQuit(UUID uuid) {
        suspended.remove(uuid);
    }
}
