package be.isach.ultracosmetics.economy;

import org.bukkit.entity.Player;

public interface EconomyHook {
    /**
     * Withdraws the specified amount of money from the specified player.
     *
     * @param player The player to withdraw money from.
     * @param amount The amount to withdraw from the player.
     */
    void withdraw(Player player, int amount, Runnable onSuccess, Runnable onFailure);

    /**
     * Gives the specified amount of money to the specified player.
     *
     * @param player The player to give money to.
     * @param amount The amount to give to the player.
     */
    void deposit(Player player, int amount);

    /**
     * Gets the name of the economy being used.
     *
     * @return The name of the economy being used.
     */
    String getName();
}
