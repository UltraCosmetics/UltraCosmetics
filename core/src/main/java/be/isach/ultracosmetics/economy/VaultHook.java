package be.isach.ultracosmetics.economy;

import be.isach.ultracosmetics.config.SettingsManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Locale;

public class VaultHook implements EconomyHook {
    private final Economy economy;
    private final boolean nonnegative;
    private final boolean ignoreResponse;

    public VaultHook() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            throw new IllegalStateException("Vault is not present.");
        }
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            throw new IllegalStateException(
                    "Found Vault but no economy, please check whether your economy plugin supports Vault.");
        }
        economy = economyProvider.getProvider();
        String validation =
                SettingsManager.getConfig().getString("Vault-Balance-Validation", "delegate").toLowerCase(Locale.ROOT);
        nonnegative = validation.endsWith("nonnegative");
        ignoreResponse = validation.equals("force-nonnegative");
    }

    @Override
    public void withdraw(Player player, int amount, Runnable onSuccess, Runnable onFailure) {
        if (nonnegative && economy.getBalance(player) < amount) {
            onFailure.run();
            return;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (ignoreResponse || response.type == ResponseType.SUCCESS) {
            onSuccess.run();
        } else {
            onFailure.run();
        }
    }

    @Override
    public void deposit(Player player, int amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public String getName() {
        return "Vault:" + economy.getName();
    }
}
