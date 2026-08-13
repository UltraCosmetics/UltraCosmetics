package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.cosmetics.particleeffects.custom.ParticleEffectCustom;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.ParticleSpec;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.Shape;
import be.isach.ultracosmetics.cosmetics.particleeffects.custom.ShapeParams;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;

import java.util.List;

public class CustomParticleEffectType extends ParticleEffectType {

    private final Shape shape;
    private final List<ParticleSpec> specs;
    private final ShapeParams shapeParams;

    public CustomParticleEffectType(String configName, XParticle previewParticle, XMaterial material,
                                    Shape shape, List<ParticleSpec> specs, ShapeParams shapeParams) {
        super(configName, 1, previewParticle, material, ParticleEffectCustom.class, true);
        this.shape = shape;
        this.specs = specs;
        this.shapeParams = shapeParams;
    }

    public Shape getShape() {
        return shape;
    }

    public List<ParticleSpec> getSpecs() {
        return specs;
    }

    public ShapeParams getShapeParams() {
        return shapeParams;
    }
}
