package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;

import java.util.Locale;

public class TreasureChestDesign {

    private final XMaterial center;
    private final XMaterial blocks2;
    private final XMaterial blocks3;
    private final XMaterial belowChests;
    private final XMaterial barriers;
    private ChestType chestType;
    private XParticle effect;

    public TreasureChestDesign(String path) {
        center = getXMaterial(path + ".center-block");
        blocks2 = getXMaterial(path + ".around-center");
        blocks3 = getXMaterial(path + ".third-blocks");
        belowChests = getXMaterial(path + ".below-chests");
        barriers = getXMaterial(path + ".barriers");
        String chestType = UltraCosmeticsData.get().getPlugin().getConfig()
                .getString("TreasureChests.Designs." + path + ".chest-type");
        String effect = UltraCosmeticsData.get().getPlugin().getConfig()
                .getString("TreasureChests.Designs." + path + ".effect");
        try {
            this.chestType = ChestType.valueOf(chestType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exc) {
            this.chestType = ChestType.NORMAL;
        }
        if (effect != null) {
            this.effect = XParticle.of(effect).orElse(XParticle.FLAME);
        }
    }

    private XMaterial getXMaterial(String s) {
        return ItemFactory.getNullableXMaterialFromConfig("TreasureChests.Designs." + s);
    }

    public ChestType getChestType() {
        return chestType;
    }

    public XMaterial getCenter() {
        return center;
    }

    public XMaterial getBlocks2() {
        return blocks2;
    }

    public XMaterial getBlocks3() {
        return blocks3;
    }

    public XMaterial getBarriers() {
        return barriers;
    }

    public XMaterial getBelowChests() {
        return belowChests;
    }

    public XParticle getEffect() {
        return effect;
    }
}
