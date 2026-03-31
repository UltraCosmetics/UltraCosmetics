package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.gadgets.*;
import com.cryptomorin.xseries.XMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Gadget types.
 *
 * @author iSach
 * @since 12-01-2015
 */
public class GadgetType extends CosmeticType<Gadget> {

    private final double cooldown;
    private final int runTime;

    private GadgetType(XMaterial material, double defaultCooldown, int runTime, String configName, Class<? extends Gadget> clazz) {
        super(Category.GADGETS, configName, material, clazz);

        if (!SettingsManager.getConfig().isDouble("Gadgets." + configName + ".Cooldown")) {
            this.cooldown = defaultCooldown;
            SettingsManager.getConfig().set("Gadgets." + configName + ".Cooldown", defaultCooldown);
        } else {
            this.cooldown = SettingsManager.getConfig().getDouble("Gadgets." + configName + ".Cooldown");
        }

        this.runTime = runTime;
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".name", configName);
        }
    }

    public boolean requiresAmmo() {
        return SettingsManager.getConfig().getBoolean("Gadgets." + getConfigName() + ".Ammo.Enabled");
    }

    /**
     * Gets the price for each ammo purchase.
     *
     * @return the price for each ammo purchase.
     */
    public int getAmmoPrice() {
        return SettingsManager.getConfig().getInt("Gadgets." + getConfigName() + ".Ammo.Price");
    }

    /**
     * Gets the ammo it should give after a purchase.
     *
     * @return the ammo it should give after a purchase.
     */
    public int getResultAmmoAmount() {
        return SettingsManager.getConfig().getInt("Gadgets." + getConfigName() + ".Ammo.Result-Amount");
    }

    public double getCountdown() {
        // cooldown should not be lower than runtime unless you enjoy bugs
        return Math.max(cooldown, runTime);
    }

    public int getRunTime() {
        return runTime;
    }

    @Override
    public void setupConfig(CustomConfiguration config, String path) {
        super.setupConfig(config, path);
        if (UltraCosmeticsData.get().isAmmoEnabled()) {
            config.addDefault(path + ".Ammo.Enabled", true, "You want this gadget to need ammo?");
            config.addDefault(path + ".Ammo.Price", 500, "What price for the ammo?");
            config.addDefault(path + ".Ammo.Result-Amount", 20, "And how much ammo is given when bought?");
        }
    }

    public static void register() {
        new GadgetType(XMaterial.IRON_HORSE_ARMOR, 8, 3, "BatBlaster", GadgetBatBlaster.class);
        new GadgetType(XMaterial.COOKED_CHICKEN, 6, 3, "Chickenator", GadgetChickenator.class);
        new GadgetType(XMaterial.BEACON, 45, 20, "DiscoBall", GadgetDiscoBall.class);
        new GadgetType(XMaterial.ENDER_PEARL, 2, 0, "EtherealPearl", GadgetEtherealPearl.class);
        new GadgetType(XMaterial.TRIPWIRE_HOOK, 5, 0, "FleshHook", GadgetFleshHook.class);
        new GadgetType(XMaterial.MELON, 2, 0, "MelonThrower", GadgetMelonThrower.class);
        new GadgetType(XMaterial.COMPARATOR, 2, 0, "PortalGun", GadgetPortalGun.class) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                config.addDefault(path + ".Affects-Others", "true", "Whether the portals should affect players other than the owner.");
            }
        };
        new GadgetType(XMaterial.DIAMOND_HORSE_ARMOR, 0.5, 0, "PaintballGun", GadgetPaintballGun.class) {
            @Override
            public void setupConfig(CustomConfiguration config, String path) {
                super.setupConfig(config, path);
                // default "" so we don't have to deal with null
                if (config.getString(path + ".Block-Type", "").equals("STAINED_CLAY")) {
                    config.set(path + ".Block-Type", "_TERRACOTTA", "With what block will it paint?", "Uses all blocks that end with the supplied string. For values, see:", "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");
                }
                config.addDefault(path + ".Block-Type", "_TERRACOTTA", "With what block will it paint?", "Uses all blocks that end with the supplied string. For values, see:", "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");
                config.addDefault(path + ".Particle.Enabled", false, "Should it display particles?");
                config.addDefault(path + ".Particle.Effect", "FIREWORKS_SPARK", "what particles? (List: http://pastebin.com/CVKkufck)");
                config.addDefault(path + ".Particle.Count", 50, "How many particles should be displayed?");
                config.addDefault(path + ".Radius", 2, "The radius of painting.");
                List<String> blackListedBlocks = new ArrayList<>();
                blackListedBlocks.add("REDSTONE_BLOCK");
                config.addDefault(path + ".BlackList", blackListedBlocks, "A list of the BLOCKS that", "can't be painted.");
            }
        };
        new GadgetType(XMaterial.IRON_AXE, 8, 0, "ThorHammer", GadgetThorHammer.class);
        new GadgetType(XMaterial.ENDER_EYE, 30, 12, "AntiGravity", GadgetAntiGravity.class);
        new GadgetType(XMaterial.FIREWORK_STAR, 15, 0, "SmashDown", GadgetSmashDown.class);
        new GadgetType(XMaterial.FIREWORK_ROCKET, 60, 4, "Rocket", GadgetRocket.class);
        new GadgetType(XMaterial.WATER_BUCKET, 12, 2, "Tsunami", GadgetTsunami.class);
        new GadgetType(XMaterial.TNT, 10, 0, "TNT", GadgetTNT.class);
        new GadgetType(XMaterial.BLAZE_ROD, 4, 0, "FunGun", GadgetFunGun.class);
        new GadgetType(XMaterial.LEAD, 60, 7, "Parachute", GadgetParachute.class);
        new GadgetType(XMaterial.SKELETON_SKULL, 45, 8, "GhostParty", GadgetGhostParty.class);
        new GadgetType(XMaterial.FIREWORK_ROCKET, 0.2, 0, "Firework", GadgetFirework.class);
        new GadgetType(XMaterial.FERN, 20, 10, "ChristmasTree", GadgetChristmasTree.class);
        new GadgetType(XMaterial.ICE, 8, 3, "FreezeCannon", GadgetFreezeCannon.class);
        new GadgetType(XMaterial.SNOWBALL, 0.5, 0, "Snowball", GadgetSnowball.class);
        new GadgetType(XMaterial.GOLDEN_CARROT, 2, 0, "PartyPopper", GadgetPartyPopper.class);
        new GadgetType(XMaterial.LIGHT_BLUE_WOOL, 25, 7, "ColorBomb", GadgetColorBomb.class);
        new GadgetType(XMaterial.BLUE_WOOL, 75, 12, "Trampoline", GadgetTrampoline.class);
        new GadgetType(XMaterial.BLACK_TERRACOTTA, 35, 8, "BlackHole", GadgetBlackHole.class);

        if (UltraCosmeticsData.get().isMobChipAvailable()) {
            new GadgetType(XMaterial.SHEARS, 25, 11, "ExplosiveSheep", GadgetExplosiveSheep.class);
        }

        if (UltraCosmeticsData.get().getVersionManager().isUsingNMS()) {
            new GadgetType(XMaterial.PACKED_ICE, 12, 2, "BlizzardBlaster", GadgetBlizzardBlaster.class);
            new GadgetType(XMaterial.DIAMOND_HOE, 3, 0, "QuakeGun", GadgetQuakeGun.class);
        }
    }
}
