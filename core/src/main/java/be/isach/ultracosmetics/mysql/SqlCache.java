package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.player.profile.CosmeticsProfile;
import be.isach.ultracosmetics.player.profile.ProfileKey;
import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Package: be.isach.ultracosmetics.mysql
 * Created by: sacha
 * Date: 15/08/15
 * Project: UltraCosmetics
 */
public class SqlCache extends CosmeticsProfile {
    private final MySqlConnectionManager sql;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private WrappedTask updateTask = null;

    public SqlCache(UltraPlayer ultraPlayer, UltraCosmetics ultraCosmetics) {
        super(ultraPlayer, ultraCosmetics);
        this.sql = ultraCosmetics.getMySqlConnectionManager();
    }

    @Override
    public void load() {
        data.loadFromSQL();
    }

    // Saved on write
    @Override
    public void save() {
    }

    @Override
    public void setEnabledCosmetic(Category cat, CosmeticType<?> type) {
        // Primary use: if we're setting it to null and it's already null, skip
        if (data.getEnabledCosmetics().get(cat) == type) return;
        super.setEnabledCosmetic(cat, type);
        if (sql.getEquippedTable() == null) return;
        if (type == null) {
            queueUpdate(() -> sql.getEquippedTable().unsetEquipped(uuid, cat));
        } else {
            queueUpdate(() -> sql.getEquippedTable().setEquipped(uuid, type));
        }
    }

    @Override
    public void clearAllEquipped() {
        super.clearAllEquipped();
        if (sql.getEquippedTable() == null) return;
        queueUpdate(() -> sql.getEquippedTable().clearAllEquipped(uuid));
    }

    @Override
    public void setAmmo(GadgetType type, int amount) {
        super.setAmmo(type, amount);
        if (sql.getAmmoTable() == null) return;
        queueUpdate(() -> sql.getAmmoTable().setAmmo(uuid, type, amount));
    }

    @Override
    public void setPetName(PetType type, String name) {
        super.setPetName(type, name);
        if (sql.getPetNames() == null) return;
        queueUpdate(() -> sql.getPetNames().setPetName(uuid, type, name));
    }

    @Override
    public void setKeys(int amount) {
        super.setKeys(amount);
        queueUpdate(() -> sql.getPlayerData().setKeys(uuid, amount));
    }

    @Override
    public void setGadgetsEnabled(boolean gadgetsEnabled) {
        // If the value did not change, skip the update.
        if (gadgetsEnabled == hasGadgetsEnabled()) return;
        super.setGadgetsEnabled(gadgetsEnabled);
        queueUpdate(() -> sql.getPlayerData().setSetting(uuid, ProfileKey.GADGETS_ENABLED, gadgetsEnabled));
    }

    @Override
    public void setSeeSelfMorph(boolean seeSelfMorph) {
        super.setSeeSelfMorph(seeSelfMorph);
        queueUpdate(() -> sql.getPlayerData().setSetting(uuid, ProfileKey.MORPH_VIEW, seeSelfMorph));
    }

    @Override
    public void setTreasureNotifications(boolean treasureNotifications) {
        super.setTreasureNotifications(treasureNotifications);
        queueUpdate(() -> sql.getPlayerData().setSetting(uuid, ProfileKey.TREASURE_NOTIFICATION, treasureNotifications));
    }

    @Override
    public void setFilterByOwned(boolean filterByOwned) {
        super.setFilterByOwned(filterByOwned);
        queueUpdate(() -> sql.getPlayerData().setSetting(uuid, ProfileKey.FILTER_OWNED, filterByOwned));
    }

    @Override
    public void setUnlocked(Set<CosmeticType<?>> types) {
        super.setUnlocked(types);
        if (sql.getUnlockedTable() == null) return;
        queueUpdate(() -> sql.getUnlockedTable().setUnlocked(uuid, types));
    }

    @Override
    public void setLocked(Set<CosmeticType<?>> types) {
        super.setLocked(types);
        if (sql.getUnlockedTable() == null) return;
        queueUpdate(() -> sql.getUnlockedTable().unsetUnlocked(uuid, types));
    }

    /**
     * This function runs SQl queries asynchronously
     *
     * @param update The function to run
     */
    private void queueUpdate(Runnable update) {
        queue.add(update);
        if (updateTask == null || updateTask.isCancelled()) {
            updateTask = ultraCosmetics.getScheduler().runLaterAsync(() -> {
                while (!queue.isEmpty()) {
                    queue.poll().run();
                }
                updateTask.cancel();
            }, 1); // 1 tick delay so that the queue can be filled before running
        }
    }
}
