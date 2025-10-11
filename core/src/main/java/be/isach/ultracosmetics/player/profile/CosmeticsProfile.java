package be.isach.ultracosmetics.player.profile;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class CosmeticsProfile {
    protected final UltraPlayer ultraPlayer;
    protected final UUID uuid;
    protected final UltraCosmetics ultraCosmetics;
    protected final PlayerData data;
    protected final AtomicBoolean loaded = new AtomicBoolean();
    protected Consumer<CosmeticsProfile> onLoad = p -> {
    };

    public CosmeticsProfile(UltraPlayer ultraPlayer, UltraCosmetics ultraCosmetics) {
        this.ultraPlayer = ultraPlayer;
        this.uuid = ultraPlayer.getUUID();
        this.ultraCosmetics = ultraCosmetics;
        this.data = new PlayerData(uuid);
        ultraCosmetics.getScheduler().runAsync((outer) -> {
            load();
            synchronized (loaded) {
                loaded.set(true);
                ultraCosmetics.getScheduler().runNextTick((inner) -> onLoad.accept(this));
            }
        });
    }

    public void onLoad(Consumer<CosmeticsProfile> onLoad) {
        synchronized (loaded) {
            if (loaded.get()) {
                onLoad.accept(this);
                return;
            }
            this.onLoad = onLoad;
        }
    }

    protected abstract void load();

    public abstract void save();

    public void equip() {
        if (!ultraPlayer.isOnline()) return;
        if (!SettingsManager.isAllowedWorld(ultraPlayer.getBukkitPlayer().getWorld())) return;
        ultraPlayer.withPreserveEquipped(() -> {
            for (Entry<Category, CosmeticType<?>> type : data.getEnabledCosmetics().entrySet()) {
                if (type.getValue() != null && type.getKey().isEnabled() && type.getValue().isEnabled()) {
                    type.getValue().equip(ultraPlayer, ultraCosmetics);
                }
            }
        });
    }

    public void setEnabledCosmetic(Category cat, Cosmetic<?> cosmetic) {
        setEnabledCosmetic(cat, cosmetic == null ? null : cosmetic.getType());
    }

    public void setEnabledCosmetic(Category cat, CosmeticType<?> type) {
        data.getEnabledCosmetics().put(cat, type);
    }

    public void clearAllEquipped() {
        data.getEnabledCosmetics().clear();
    }

    public boolean hasAnyEquipped() {
        return !data.getEnabledCosmetics().isEmpty();
    }

    public int getAmmo(GadgetType gadget) {
        return data.getAmmo().getOrDefault(gadget, 0);
    }

    public void setAmmo(GadgetType type, int amount) {
        data.getAmmo().put(type, amount);
    }

    public void addAmmo(GadgetType type, int amount) {
        setAmmo(type, getAmmo(type) + amount);
    }

    public String getPetName(PetType pet) {
        return data.getPetNames().get(pet);
    }

    public void setPetName(PetType pet, String name) {
        data.getPetNames().put(pet, name);
    }

    public int getKeys() {
        return data.getKeys();
    }

    public void setKeys(int amount) {
        data.setKeys(amount);
    }

    public void addKeys(int amount) {
        setKeys(getKeys() + amount);
    }

    public void setGadgetsEnabled(boolean enabled) {
        data.setGadgetsEnabled(enabled);
    }

    public boolean hasGadgetsEnabled() {
        return data.isGadgetsEnabled();
    }

    public void setSeeSelfMorph(boolean enabled) {
        data.setMorphSelfView(enabled);
    }

    public boolean canSeeSelfMorph() {
        return data.isMorphSelfView();
    }

    public boolean isTreasureNotifications() {
        return data.isTreasureNotifications();
    }

    public void setTreasureNotifications(boolean treasureNotifications) {
        data.setTreasureNotifications(treasureNotifications);
    }

    public boolean isFilterByOwned() {
        return data.isFilterByOwned();
    }

    public void setFilterByOwned(boolean filterByOwned) {
        data.setFilterByOwned(filterByOwned);
    }

    public boolean hasUnlocked(CosmeticType<?> type) {
        return data.getUnlockedCosmetics().contains(type);
    }

    public void setUnlocked(Set<CosmeticType<?>> type) {
        data.getUnlockedCosmetics().addAll(type);
    }

    public void setLocked(Set<CosmeticType<?>> type) {
        data.getUnlockedCosmetics().removeAll(type);
    }

    public Set<CosmeticType<?>> getAllUnlocked() {
        return Collections.unmodifiableSet(data.getUnlockedCosmetics());
    }
}
