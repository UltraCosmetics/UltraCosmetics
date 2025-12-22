package be.isach.ultracosmetics.cosmetics.pets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;

/**
 * Represents an instance of a panda pet summoned by a player.
 *
 * @author Chris6ix
 * @since 13-01-2022
 */
public class PetPanda extends Pet {
    public PetPanda(UltraPlayer owner, PetType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    protected boolean customize(String customization) {
        return enumCustomize(Gene.class, customization, gene -> {
            Panda panda = (Panda) entity;
            panda.setMainGene(gene);
            panda.setHiddenGene(gene);
        });
    }
}
