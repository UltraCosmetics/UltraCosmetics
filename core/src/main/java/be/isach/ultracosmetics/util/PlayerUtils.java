package be.isach.ultracosmetics.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ListIterator;
import java.util.function.Predicate;

public class PlayerUtils {

    public static Vector getHorizontalDirection(Player player, double mult) {
        Vector vector = new Vector();
        double rotX = Math.toRadians(player.getLocation().getYaw());
        vector.setX(-Math.sin(rotX));
        vector.setZ(Math.cos(rotX));
        return vector.multiply(mult);
    }

    /**
     * Removes items from player inventory based on a Predicate.
     *
     * @param player    The player to remove the item from.
     * @param matchFunc A function that returns true if the item should be removed.
     */
    public static void removeItems(Player player, Predicate<ItemStack> matchFunc) {
        PlayerInventory inv = player.getInventory();
        ListIterator<ItemStack> iter = inv.iterator();
        while (iter.hasNext()) {
            ItemStack stack = iter.next();
            if (stack == null) {
                continue;
            }
            if (matchFunc.test(stack)) {
                // iter.remove() will not work here
                inv.clear(iter.previousIndex());
            }
        }
    }

}
