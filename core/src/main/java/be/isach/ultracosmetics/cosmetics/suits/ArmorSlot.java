package be.isach.ultracosmetics.cosmetics.suits;

import org.bukkit.ChatColor;
import org.bukkit.inventory.EquipmentSlot;

public enum ArmorSlot {
    HELMET(EquipmentSlot.HEAD),
    CHESTPLATE(EquipmentSlot.CHEST),
    LEGGINGS(EquipmentSlot.LEGS),
    BOOTS(EquipmentSlot.FEET);

    private final EquipmentSlot bukkitSlot;

    private ArmorSlot(EquipmentSlot bukkitSlot) {
        this.bukkitSlot = bukkitSlot;
    }

    public EquipmentSlot toBukkit() {
        return bukkitSlot;
    }

    public static ArmorSlot getByName(String s) {
        for (ArmorSlot a : ArmorSlot.values()) {
            if (a.toString().equalsIgnoreCase(ChatColor.stripColor(s))) {
                return a;
            }
        }
        return ArmorSlot.CHESTPLATE;
    }
}
