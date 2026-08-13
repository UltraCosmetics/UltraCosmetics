package be.isach.ultracosmetics.bedrock;

import org.bukkit.entity.Player;

/**
 * Fallback used when Floodgate isn't installed (or failed to load). Reports every player
 * as "not a mobile Bedrock player", so the plugin always falls back to the inventory GUI.
 */
public class NoopFormProvider implements BedrockFormProvider {

    @Override
    public boolean isMobileBedrockPlayer(Player player) {
        return false;
    }
}
