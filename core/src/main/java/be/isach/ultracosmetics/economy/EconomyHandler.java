package be.isach.ultracosmetics.economy;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.util.Discount;
import be.isach.ultracosmetics.util.SmartLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles the current economy being used.
 *
 * @author RadBuilder
 * @since 2.5
 */
@SuppressWarnings("Convert2MethodRef")
public class EconomyHandler {
    private static final Map<String, EconomyHookLoader> economies = new HashMap<>();

    static {
        // Do NOT use method references, this requires loading the class, which will fail if the plugin is missing
        economies.put("treasury", (uc, currency) -> new TreasuryHook(uc, currency));
        economies.put("vault", (uc, currency) -> new VaultHook());
        economies.put("playerpoints", (uc, currency) -> new PlayerPointsHook());
        economies.put("peconomy", (uc, currency) -> new PEconomyHook(uc, currency));
        // TODO: remove this once ExcellentEconomy has been around for a
        economies.put("coinsengine", (uc, currency) -> {
            try {
                return loadByReflection(uc, currency, "ExcellentEconomyHook");
            } catch (NoClassDefFoundError err) {
                return new CoinsEngineHook(uc, currency);
            }
        });
        economies.put("excellenteconomy", (uc, currency) -> loadByReflection(uc, currency, "ExcellentEconomyHook"));
    }

    private final UltraCosmetics ultraCosmetics;
    private EconomyHook economyHook;
    private String currency;
    private boolean usingEconomy = false;
    private boolean waitingForCustomEconomy = false;
    private final List<Discount> discounts = new ArrayList<>();

    public EconomyHandler(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        String economy = ultraCosmetics.getConfig().getString("Economy", "").toLowerCase(Locale.ROOT);
        if (economy.isEmpty()) {
            ultraCosmetics.getSmartLogger().write("Economy not specified in the config, disabling economy features.");
            return;
        }
        loadDiscounts();

        currency = ultraCosmetics.getConfig().getString("Economy-Currency", "");
        if (currency.isEmpty()) currency = null;

        ultraCosmetics.getSmartLogger().write("");
        EconomyHookLoader hookLoader = economies.get(economy);
        if (hookLoader != null) {
            loadHook(hookLoader);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin(economy) == null) {
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.ERROR, "Unknown economy: '" + economy + "'. Valid economies: " + String.join(", ", economies.keySet()));
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.WARNING, "Economy features will be disabled.");
        } else {
            ultraCosmetics.getSmartLogger().write("Economy plugin " + economy + " is unknown, waiting for it to register itself.");
            waitingForCustomEconomy = true;
        }
    }

    private void loadDiscounts() {
        ConfigurationSection section = SettingsManager.getConfig().getConfigurationSection("Discount-Groups");
        for (String key : section.getKeys(false)) {
            if (!section.isDouble(key)) continue;
            discounts.add(new Discount(key, section.getDouble(key)));
        }
        Collections.sort(discounts);
    }

    private void loadHook(EconomyHookLoader loader) {
        try {
            economyHook = loader.load(ultraCosmetics, currency);
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedClassVersionError |
                 ReflectiveOperationException e) {
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.ERROR, e.getClass().getName() + ": " + e.getMessage());
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.WARNING, "Economy features will be disabled.");
            return;
        } catch (Exception e) {
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.ERROR, "Failed to hook into " + loader.getClass().getName() + " for economy.");
            e.printStackTrace();
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.WARNING, "Economy features will be disabled.");
            return;
        }
        ultraCosmetics.getSmartLogger().write("Hooked into " + economyHook.getName() + " for economy.");
        usingEconomy = true;
        ultraCosmetics.getSmartLogger().write("");
    }

    public EconomyHook getHook() {
        return economyHook;
    }

    public void addHook(EconomyHookLoader loader) {
        if (waitingForCustomEconomy) {
            loadHook(loader);
            // Indicates success
            if (usingEconomy) {
                waitingForCustomEconomy = false;
            }
        } else {
            ultraCosmetics.getSmartLogger().write(SmartLogger.LogLevel.WARNING, "Economy already loaded, ignoring additional hook from " + loader.getClass().getName());
        }
    }

    public void withdrawWithDiscount(Player player, int amount, Runnable onSuccess, Runnable onFailure) {
        economyHook.withdraw(player, calculateDiscountPrice(player, amount), onSuccess, onFailure);
    }

    public int calculateDiscountPrice(Player player, int amount) {
        for (Discount discount : discounts) {
            if (discount.hasPermission(player)) {
                return (int) (amount * discount.getDiscount());
            }
        }
        return amount;
    }

    public boolean isUsingEconomy() {
        return usingEconomy;
    }

    private static EconomyHook loadByReflection(UltraCosmetics ultraCosmetics, String currency, String className) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName("be.isach.ultracosmetics.economy." + className);
        try {
            return (EconomyHook) clazz.getConstructor(UltraCosmetics.class, String.class).newInstance(ultraCosmetics, currency);
        } catch (InvocationTargetException e) {
            // Unwrap the exception if it's an IllegalArgumentException, those are expected
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
            throw e;
        }
    }
}
