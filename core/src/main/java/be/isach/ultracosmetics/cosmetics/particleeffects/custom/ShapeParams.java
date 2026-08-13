package be.isach.ultracosmetics.cosmetics.particleeffects.custom;

import org.bukkit.configuration.ConfigurationSection;

public class ShapeParams {

    private final ConfigurationSection section;

    public ShapeParams(ConfigurationSection section) {
        this.section = section;
    }

    public double getDouble(String key, double def) {
        return section.isDouble(key) || section.isInt(key) ? section.getDouble(key) : def;
    }

    public int getInt(String key, int def) {
        return section.isInt(key) ? section.getInt(key) : def;
    }

    public ConfigurationSection raw() {
        return section;
    }
}
