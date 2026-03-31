package be.isach.ultracosmetics.economy;

import be.isach.ultracosmetics.UltraCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;

public class ExcellentEconomyHook implements EconomyHook {
    private final ExcellentEconomyAPI api;
    private final ExcellentCurrency currency;

    public ExcellentEconomyHook(UltraCosmetics plugin, String currencyName) {
        RegisteredServiceProvider<ExcellentEconomyAPI> provider = Bukkit.getServer().getServicesManager().getRegistration(ExcellentEconomyAPI.class);
        if (provider == null) {
            throw new IllegalArgumentException("Couldn't find ExcellentEconomy");
        }
        api = provider.getProvider();
        if (currencyName == null) {
            // Pick some currency
            currency = api.getCurrencies().iterator().next();
        } else {
            currency = api.getCurrency(currencyName);
        }
        if (currency == null) {
            throw new IllegalArgumentException("Couldn't find specified ExcellentEconomy currency '" + currencyName + "'");
        }
    }

    @Override
    public void withdraw(Player player, int amount, Runnable onSuccess, Runnable onFailure) {
        api.getBalanceAsync(player.getUniqueId(), currency).whenComplete((balance, ex) -> {
            if (ex != null) {
                onFailure.run();
                return;
            }
            if (balance < amount) {
                onFailure.run();
                return;
            }
            api.withdrawAsync(player.getUniqueId(), currency, amount).whenComplete((res, e) -> {
                if (e == null) {
                    onSuccess.run();
                } else {
                    onFailure.run();
                }
            });
        });
    }

    @Override
    public void deposit(Player player, int amount) {
        api.deposit(player, currency, amount);
    }

    @Override
    public String getName() {
        return "ExcellentEconomy:" + currency.getName();
    }
}
