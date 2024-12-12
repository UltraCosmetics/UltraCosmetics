package be.isach.ultracosmetics.economy;

import be.isach.ultracosmetics.UltraCosmetics;
import me.lokka30.treasury.api.common.Cause;
import me.lokka30.treasury.api.common.NamespacedKey;
import me.lokka30.treasury.api.common.service.Service;
import me.lokka30.treasury.api.common.service.ServiceRegistry;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.currency.Currency;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Optional;

public class TreasuryHook implements EconomyHook {
    private final UltraCosmetics ultraCosmetics;
    private final EconomyProvider economy;
    private final Cause<NamespacedKey> cause = Cause.plugin(NamespacedKey.of("UltraCosmetics", "TreasuryHook"));
    private final Currency currency;

    public TreasuryHook(UltraCosmetics ultraCosmetics, String currencyName) {
        this.ultraCosmetics = ultraCosmetics;
        Optional<Service<EconomyProvider>> optProvider = ServiceRegistry.INSTANCE.serviceFor(EconomyProvider.class);
        if (!optProvider.isPresent()) {
            throw new IllegalStateException("Could not find provider for Treasury economy.");
        }
        economy = optProvider.get().get();
        if (currencyName == null) {
            currency = economy.getPrimaryCurrency();
        } else {
            Optional<Currency> optCurrency = economy.findCurrency(currencyName);
            if (!optCurrency.isPresent()) {
                throw new IllegalArgumentException("Couldn't find specified Treasury currency '" + currencyName + "'");
            }
            currency = optCurrency.get();
        }
    }

    private Runnable mainThread(Runnable runnable) {
        return () -> ultraCosmetics.getScheduler().runNextTick((task) -> runnable.run());
    }

    @Override
    public void withdraw(Player player, int intAmount, Runnable onSuccess, Runnable onFailure) {
        BigDecimal amount = new BigDecimal(intAmount);
        economy.accountAccessor().player().withUniqueId(player.getUniqueId()).get()
                .thenCompose(account -> account.withdrawBalance(amount, cause, currency))
                .thenRun(mainThread(onSuccess)).exceptionally(ex -> {
                    mainThread(onFailure).run();
                    return null;
                });

    }

    @Override
    public void deposit(Player player, int intAmount) {
        BigDecimal amount = new BigDecimal(intAmount);
        economy.accountAccessor().player().withUniqueId(player.getUniqueId()).get()
                .thenCompose(account -> account.depositBalance(amount, cause, currency));
    }

    @Override
    public String getName() {
        return "Treasury:" + currency.getIdentifier();
    }

}
