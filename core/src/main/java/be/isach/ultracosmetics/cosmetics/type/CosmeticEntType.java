package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;

public abstract class CosmeticEntType<T extends Cosmetic<?>> extends CosmeticType<T> {

    private final EntityType entityType;

    public CosmeticEntType(Category category, String configName, XMaterial material, XEntityType entityType,
                           Class<? extends T> clazz) {
        super(category, configName, material, clazz);
        this.entityType = entityType.get();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isMonster() {
        // If the entity is a monster and the world is set to peaceful, we can't spawn it
        return Monster.class.isAssignableFrom(entityType.getEntityClass())
                // no idea why Slime doesn't implement Monster but we have to check for it
                || Slime.class.isAssignableFrom(entityType.getEntityClass());
    }
}
