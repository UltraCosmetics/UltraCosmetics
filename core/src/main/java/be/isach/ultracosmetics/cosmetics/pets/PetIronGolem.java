package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public class PetIronGolem extends Pet {
    private static final Attribute SCALE = XAttribute.SCALE.get();

    public PetIronGolem(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics, XMaterial.RED_DYE);
    }

    @Override
    public void setupEntity() {
        if (SettingsManager.getConfig().getBoolean("Pets-Are-Babies") && SCALE != null) {
            AttributeModifier mod =
                    ItemFactory.createAttributeModifier("scale", -0.5, AttributeModifier.Operation.ADD_NUMBER, null);
            entity.getAttribute(SCALE).addModifier(mod);
        }
    }
}
