package be.isach.ultracosmetics.bedrock;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.DeviceOs;

import java.util.EnumSet;
import java.util.Set;

/**
 * Real {@link BedrockFormProvider}, backed by the Floodgate API. Only instantiate this
 * when the {@code floodgate} plugin is enabled — loading it otherwise throws
 * {@link NoClassDefFoundError}.
 */
public class FloodgateFormProvider implements BedrockFormProvider {

    /**
     * Touchscreen devices that get forms. Note that Android is {@link DeviceOs#GOOGLE},
     * not {@code ANDROID} — there is no {@code ANDROID} constant. {@code AMAZON} covers
     * Fire tablets, which are touchscreen too. Everything else in the enum
     * ({@code WIN32}, {@code UWP}, {@code XBOX}, {@code PS4}, {@code NX}, ...) is
     * desktop or console and keeps the inventory GUI.
     */
    private static final Set<DeviceOs> MOBILE_DEVICES =
            EnumSet.of(DeviceOs.GOOGLE, DeviceOs.IOS, DeviceOs.AMAZON);

    @Override
    public boolean isMobileBedrockPlayer(Player player) {
        if (player == null) {
            return false;
        }
        FloodgateApi api = FloodgateApi.getInstance();
        if (api == null || !api.isFloodgatePlayer(player.getUniqueId())) {
            return false;
        }
        // Can still be null in the window between connection and full login.
        FloodgatePlayer floodgatePlayer = api.getPlayer(player.getUniqueId());
        if (floodgatePlayer == null) {
            return false;
        }
        return MOBILE_DEVICES.contains(floodgatePlayer.getDeviceOs());
    }
}
