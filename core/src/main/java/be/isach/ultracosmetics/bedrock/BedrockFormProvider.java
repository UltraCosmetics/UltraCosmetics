package be.isach.ultracosmetics.bedrock;

import org.bukkit.entity.Player;

/**
 * Abstracts away whether Floodgate is installed, so the rest of the plugin can ask
 * "should this player get a Bedrock form?" without touching Floodgate classes directly.
 *
 * <p>Follows the same pattern as {@link be.isach.ultracosmetics.worldguard.IFlagManager}:
 * a real implementation ({@link FloodgateFormProvider}) and a no-op fallback
 * ({@link NoopFormProvider}) so callers never need null checks or
 * {@code NoClassDefFoundError} handling.
 */
public interface BedrockFormProvider {

    /**
     * @param player the player to check
     * @return {@code true} if this player joined through Floodgate from a touchscreen
     *         Bedrock device (phone or tablet). Always {@code false} when Floodgate
     *         isn't installed, for Java players, and for Bedrock players on
     *         desktop or console.
     */
    boolean isMobileBedrockPlayer(Player player);
}
