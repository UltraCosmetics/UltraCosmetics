package be.isach.ultracosmetics.v1_21_R7.customentities;

import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author RadBuilder
 */
public class CustomEntityFirework extends FireworkRocketEntity {
    private Player[] players = null;
    private boolean gone = false;

    public CustomEntityFirework(Level world, Player... p) {
        super(EntityType.FIREWORK_ROCKET, world);
        players = p;
    }

    @Override
    public void tick() {
        if (gone) {
            return;
        }

        if (!this.level().isClientSide()) {
            gone = true;

            if (players != null) {
                if (players.length > 0) {
                    for (Player player : players) {
                        (((CraftPlayer) player).getHandle()).connection.send(new ClientboundEntityEventPacket(this, (byte) 17));
                    }
                } else {
                    level().broadcastEntityEvent(this, (byte) 17);
                }
            }
            ((Entity) this).discard();
        }
    }
}
