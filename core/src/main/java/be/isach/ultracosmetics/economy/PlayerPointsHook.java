package be.isach.ultracosmetics.economy;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerPointsHook implements EconomyHook {
    private final PlayerPointsAPI playerPointsApi;

    public PlayerPointsHook() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            throw new IllegalArgumentException("PlayerPoints is not running.");
        }
        playerPointsApi = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
    }

    @Override
    public void withdraw(Player player, int amount, Runnable onSuccess, Runnable onFailure) {
        if (playerPointsApi.take(player.getUniqueId(), amount)) {
            onSuccess.run();
        } else {
            onFailure.run();
        }
    }

    @Override
    public void deposit(Player player, int amount) {
        playerPointsApi.give(player.getUniqueId(), amount);
    }

    @Override
    public String getName() {
        return "PlayerPoints";
    }
}
