package be.isach.ultracosmetics.economy;

import be.isach.ultracosmetics.UltraCosmetics;

@FunctionalInterface
public interface EconomyHookLoader {
    public EconomyHook load(UltraCosmetics ultraCosmetics, String currency) throws ReflectiveOperationException;
}
