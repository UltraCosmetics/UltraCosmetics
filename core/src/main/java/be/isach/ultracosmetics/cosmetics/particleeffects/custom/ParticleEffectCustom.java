package be.isach.ultracosmetics.cosmetics.particleeffects.custom;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.particleeffects.ParticleEffect;
import be.isach.ultracosmetics.cosmetics.type.CustomParticleEffectType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.particles.ParticleDisplay;

import java.util.List;
import java.util.stream.Collectors;

public class ParticleEffectCustom extends ParticleEffect {

    private final Shape shape;
    private final ShapeParams shapeParams;
    private final List<ParticleDisplay> displays;
    private int tick;

    public ParticleEffectCustom(UltraPlayer owner, CustomParticleEffectType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
        this.shape = type.getShape();
        this.shapeParams = type.getShapeParams();
        this.displays = type.getSpecs().stream().map(ParticleSpec::buildDisplay).collect(Collectors.toList());
    }

    @Override
    public void onUpdate() {
        shape.render(getPlayer(), displays, shapeParams, tick++);
    }
}
