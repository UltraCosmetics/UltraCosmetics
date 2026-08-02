package be.isach.ultracosmetics.version;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public interface IEntityUtil {

    default void resetWitherSize(Wither wither) {
        try {
            wither.setInvulnerabilityTicks(600);
        } catch (NoSuchMethodError ignored) {
        }
    }

    void sendBlizzard(final Player player, Location loc, Predicate<Entity> canAffectFunc, Vector v);

    void clearBlizzard(final Player player);

    void sendDestroyPacket(Player player, Entity entity);

    void setStepHeight(Entity entity);

    void sendTeleportPacket(Player player, Entity entity);
}
