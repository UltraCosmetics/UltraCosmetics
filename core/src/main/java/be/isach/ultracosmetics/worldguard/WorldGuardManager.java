package be.isach.ultracosmetics.worldguard;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.gadgets.Gadget;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.Problem;
import be.isach.ultracosmetics.util.SmartLogger;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import be.isach.ultracosmetics.util.TextUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class WorldGuardManager {

    private final UltraCosmetics ultraCosmetics;
    private IFlagManager flagManager;

    public WorldGuardManager(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
    }

    public void register() {
        try {
            flagManager = new FlagManager();
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Couldn't find required classes for WorldGuard integration.");
            ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Please make sure you are using the latest version of WorldGuard");
            ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "for your version of Minecraft. Debug info:");
            e.printStackTrace();
            ultraCosmetics.getSmartLogger().write("WorldGuard support is disabled.");
            ultraCosmetics.addProblem(Problem.WORLDGUARD_HOOK_FAILURE);
            return;
        }
        flagManager.register();
    }

    public void registerPhase2() {
        if (flagManager == null) return;
        SmartLogger log = ultraCosmetics.getSmartLogger();
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            log.write(LogLevel.ERROR, "WorldGuard is not enabled yet! Is WorldGuard up to date? Is another plugin interfering with the load order?");
            log.write(LogLevel.ERROR, "WorldGuard support will be disabled.");
            ultraCosmetics.addProblem(Problem.WORLDGUARD_HOOK_FAILURE);
            return;
        }
        flagManager.registerPhase2();
        log.write();
        log.write("WorldGuard custom flags enabled");
    }

    public boolean areCosmeticsAllowedHere(Player player, Category category) {
        return allowedCosmeticsState(player, category) == CosmeticRegionState.ALLOWED;
    }

    public CosmeticRegionState allowedCosmeticsState(Player player, Category category) {
        if (flagManager == null) return CosmeticRegionState.ALLOWED;

        if (!flagManager.flagCheck(UCFlag.COSMETICS, player)) {
            return CosmeticRegionState.BLOCKED_ALL;
        }
        if (category != null && !categoryFlagCheck(player, category)) {
            return CosmeticRegionState.BLOCKED_CATEGORY;
        }
        return CosmeticRegionState.ALLOWED;
    }

    public boolean canAffectPlayersHere(Player player) {
        if (flagManager == null) return true;
        return flagManager.flagCheck(UCFlag.COSMETICS, player) && flagManager.flagCheck(UCFlag.AFFECT_PLAYERS, player);
    }

    public boolean areChestsAllowedHere(Player player) {
        if (flagManager == null) return true;
        return flagManager.flagCheck(UCFlag.TREASURE, player);
    }

    public boolean isInShowroom(Player player) {
        if (flagManager == null) return false;
        return flagManager.flagCheck(UCFlag.SHOWROOM, player);
    }

    public void doCosmeticCheck(Player player, UltraCosmetics uc) {
        if (flagManager == null) return;
        if (!flagManager.flagCheck(UCFlag.COSMETICS, player) && uc.getPlayerManager().getUltraPlayer(player).clear()) {
            MessageManager.send(player, "Region-Disabled");
            return;
        }
        Set<Category> blockedCategories = flagManager.categoryFlagCheck(player);
        if (blockedCategories == null) return;
        for (Category category : blockedCategories) {
            if (blockedCategories.contains(category) && uc.getPlayerManager().getUltraPlayer(player).removeCosmetic(category)) {
                TagResolver.Single placeholder = Placeholder.component("category",
                        TextUtil.stripColor(MessageManager.getMessage("Menu." + category.getConfigPath() + ".Title")));
                MessageManager.send(player, "Region-Disabled-Category", placeholder);
            }
        }
    }

    protected boolean categoryFlagCheck(Player player, Category category) {
        Set<Category> categories = flagManager.categoryFlagCheck(player);
        return categories == null || !categories.contains(category);
    }

    public void noCosmeticsRegionEntered(UltraPlayer ultraPlayer) {
        if (ultraPlayer.clear()) {
            MessageManager.send(ultraPlayer.getBukkitPlayer(), "Region-Disabled");
        }
    }

    public void restrictedCosmeticsChange(UltraPlayer ultraPlayer, Set<Category> restrictions) {
        Player bukkitPlayer = ultraPlayer.getBukkitPlayer();
        for (Category cat : restrictions) {
            if (ultraPlayer.removeCosmetic(cat)) {
                MessageManager.send(bukkitPlayer, "Region-Disabled-Category",
                        Placeholder.component("category", MessageManager.getMessage("Menu." + cat.getConfigPath() + ".Title"))
                );
            }
        }
    }

    public void showroomFlagChange(UltraPlayer ultraPlayer, boolean newValue) {
        if (!newValue) {
            for (Category cat : Category.values()) {
                Cosmetic<?> cosmetic = ultraPlayer.getCosmetic(cat);
                if (cosmetic == null) continue;
                if (!ultraCosmetics.getPermissionManager().hasPermission(ultraPlayer, cosmetic.getType())) {
                    ultraPlayer.removeCosmetic(cat);
                }
            }
        }
        if (ultraPlayer.hasCosmetic(Category.GADGETS)) {
            ultraCosmetics.getScheduler().runAtEntity(ultraPlayer.getBukkitPlayer(), (task) -> {
                Gadget gadget = (Gadget) ultraPlayer.getCurrentGadget();
                // One tick has elapsed since the previous check, so we should check again.
                if (gadget == null) return;
                gadget.equipItem();
            });
        }
    }

    public boolean isHooked() {
        return flagManager != null;
    }
}
