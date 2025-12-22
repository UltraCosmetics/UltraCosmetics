package be.isach.ultracosmetics.player;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.emotes.Emote;
import be.isach.ultracosmetics.cosmetics.gadgets.Gadget;
import be.isach.ultracosmetics.cosmetics.hats.Hat;
import be.isach.ultracosmetics.cosmetics.morphs.Morph;
import be.isach.ultracosmetics.cosmetics.mounts.Mount;
import be.isach.ultracosmetics.cosmetics.particleeffects.ParticleEffect;
import be.isach.ultracosmetics.cosmetics.pets.Pet;
import be.isach.ultracosmetics.cosmetics.suits.ArmorSlot;
import be.isach.ultracosmetics.cosmetics.suits.Suit;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.menu.MenuItemHandler;
import be.isach.ultracosmetics.mysql.SqlCache;
import be.isach.ultracosmetics.player.profile.CosmeticsProfile;
import be.isach.ultracosmetics.player.profile.FileCosmeticsProfile;
import be.isach.ultracosmetics.run.FallDamageManager;
import be.isach.ultracosmetics.treasurechests.TreasureChest;
import be.isach.ultracosmetics.util.PlayerUtils;
import com.cryptomorin.xseries.messages.ActionBar;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player on the server.
 *
 * @author iSach
 * @since 08-03-2015
 */
public class UltraPlayer {

    private final UUID uuid;
    private final UltraCosmetics ultraCosmetics;

    /**
     * Equipped suit cosmetics are stored in `suitMap`,
     * all others are in `equipped`.
     */
    private final Map<Category, Cosmetic<?>> equipped = new HashMap<>();
    private TreasureChest currentTreasureChest;

    /**
     * Stores data that can persist, such as enabled cosmetics, keys, pet names, ammo, etc.
     */
    private final CosmeticsProfile cosmeticsProfile;

    private final boolean allowDisableGadgets = SettingsManager.getConfig().getBoolean("Categories.Gadgets.Allow-Disable-Gadgets", true);
    private final boolean menuItemEnabled = SettingsManager.getConfig().getBoolean("Menu-Item.Enabled");

    /**
     * Specifies if the player can currently be hit by any other gadget.
     * Exemple: Get hit by a flesh hook.
     * <p>
     * TODO: this is only used in one place, is that a problem?
     */
    private boolean canBeHitByOtherGadgets = true;

    /**
     * Cooldown map storing all the current cooldowns for cosmetics.
     * The values are the currentTimeMillis when the player should
     * regain access to the cosmetics.
     */
    private final Map<CosmeticType<?>, Long> cooldowns = new HashMap<>();

    /**
     * Indicates if the player is leaving the server or switching worlds.
     * Useful to differentiate for example when a player deactivates
     * a cosmetic on purpose or because they are leaving.
     */
    private boolean preserveEquipped = false;

    /**
     * Stores the client brand string.
     * Used for determining if player is a Geyser client.
     */
    private String clientBrand = null;

    /**
     * Allows to store custom data for each player easily.
     * Created on join, and deleted on quit.
     *
     * @param uuid           The player UUID.
     * @param ultraCosmetics UltraCosmetics
     */
    public UltraPlayer(UUID uuid, UltraCosmetics ultraCosmetics) {
        this.uuid = uuid;
        this.ultraCosmetics = ultraCosmetics;

        if (UltraCosmeticsData.get().usingFileStorage()) {
            cosmeticsProfile = new FileCosmeticsProfile(this, ultraCosmetics);
        } else {
            // loads data from database async
            cosmeticsProfile = new SqlCache(this, ultraCosmetics);
        }
    }

    /**
     * Do actions that should happen once the player is ready, i.e. receive menu item and re-equip cosmetics.
     */
    public void load() {
        if (menuItemEnabled && getBukkitPlayer().hasPermission("ultracosmetics.receivechest")) {
            giveMenuItem();
        }
        if (UltraCosmeticsData.get().areCosmeticsProfilesEnabled()) {
            getProfile().onLoad(CosmeticsProfile::equip);
        }
    }

    /**
     * Checks if a player can use a given gadget type.
     *
     * @param type The gadget type.
     * @return 0 if player can use, otherwise the time left (in seconds).
     */
    public double getCooldown(CosmeticType<?> type) {
        Long count = cooldowns.get(type);

        if (count == null || System.currentTimeMillis() > count) return 0;

        double valueMillis = count - System.currentTimeMillis();
        return valueMillis / 1000d;
    }

    /**
     * Checks whether the cooldown for this cosmetic has elapsed
     *
     * @param type The cosmetic type to check
     * @return true if the cooldown has elapsed
     */
    public boolean canUse(CosmeticType<?> type) {
        return getCooldown(type) == 0;
    }

    /**
     * Checks if the player can equip a cosmetic, whether due to permissions or a showroom.
     *
     * @param type the type of cosmetic to check for
     * @return true if the cosmetic can be equipped
     */
    public boolean canEquip(CosmeticType<?> type) {
        return ultraCosmetics.getWorldGuardManager().isInShowroom(getBukkitPlayer())
                || ultraCosmetics.getPermissionManager().hasPermission(this, type);
    }

    /**
     * Sets the cooldown of a cosmetic.
     *
     * @param type     The cosmetic.
     * @param cooldown The standard cooldown of the cosmetic in seconds
     * @param runTime  The runtime of the cosmetic (i.e. minimum possible cooldown) in seconds.
     */
    public void setCooldown(CosmeticType<?> type, double cooldown, double runTime) {
        double time = isBypassingCooldown() ? runTime : cooldown;
        if (time == 0) return;
        cooldowns.put(type, (long) (time * 1000 + System.currentTimeMillis()));
    }

    /**
     * Checks whether a cosmetic is on cooldown, and if not, sets the cooldown.
     *
     * @param type     The cosmetic
     * @param cooldown The standard cooldown of the cosmetic in seconds
     * @param runTime  The runtime of the cosmetic (i.e. minimum possible cooldown) in seconds.
     * @return true if the cosmetic can be used (making this a drop-in replacement for canUse)
     */
    public boolean getAndSetCooldown(CosmeticType<?> type, double cooldown, double runTime) {
        if (canUse(type)) {
            setCooldown(type, cooldown, runTime);
            return true;
        }
        return false;
    }

    /**
     * Sends the current cooldown in action bar.
     */

    public void sendCooldownBar(CosmeticType<?> type, double cooldown, double runTime) {
        StringBuilder stringBuilder = new StringBuilder(ChatColor.GREEN.toString());

        double currentCooldown = getCooldown(type);
        double maxCooldown = isBypassingCooldown() ? runTime : cooldown;

        int res = (int) (currentCooldown / maxCooldown * 50);
        for (int i = 0; i < 50; i++) {
            if (i == 50 - res) {
                stringBuilder.append(ChatColor.RED);
            }
            stringBuilder.append("|");
        }

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        otherSymbols.setPatternSeparator('.');
        final DecimalFormat decimalFormat = new DecimalFormat("0.0", otherSymbols);
        String timeLeft = decimalFormat.format(currentCooldown) + "s";
        String name = BukkitComponentSerializer.legacy().serialize(type.getName());
        ActionBar.sendActionBar(getBukkitPlayer(), name + ChatColor.WHITE + " " + stringBuilder + ChatColor.WHITE + " " + timeLeft);
    }

    /**
     * Get the player owning the UltraPlayer.
     *
     * @return The player owning the UltraPlayer.
     */
    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public Cosmetic<?> getCosmetic(Category category) {
        return equipped.get(category);
    }

    // I don't like this but I can't think of a better way without a bunch of casting elsewhere
    public ParticleEffect getCurrentParticleEffect() {
        return (ParticleEffect) getCosmetic(Category.EFFECTS);
    }

    public Emote getCurrentEmote() {
        return (Emote) getCosmetic(Category.EMOTES);
    }

    public Gadget getCurrentGadget() {
        return (Gadget) getCosmetic(Category.GADGETS);
    }

    public Hat getCurrentHat() {
        return (Hat) getCosmetic(Category.HATS);
    }

    public Morph getCurrentMorph() {
        return (Morph) getCosmetic(Category.MORPHS);
    }

    public Mount getCurrentMount() {
        return (Mount) getCosmetic(Category.MOUNTS);
    }

    public Pet getCurrentPet() {
        return (Pet) getCosmetic(Category.PETS);
    }

    public Suit getCurrentSuit(ArmorSlot slot) {
        return (Suit) getCosmetic(Category.suitsFromSlot(slot));
    }

    public boolean hasCosmetic(Category category) {
        return equipped.containsKey(category);
    }

    /**
     * Unequips the cosmetic of the specified category.
     *
     * @return {@code true} if a cosmetic was actually unequipped
     */
    public boolean removeCosmetic(Category category) {
        if (!equipped.containsKey(category)) return false;

        unsetCosmetic(category).clear();

        return true;
    }

    /**
     * Removes a cosmetic from a player without calling clear()
     * Internal use only
     *
     * @param category The category of cosmetic to unequip
     * @return the cosmetic that was removed, or null if no cosmetic was removed.
     */
    public Cosmetic<?> unsetCosmetic(Category category) {
        if (!preserveEquipped) {
            cosmeticsProfile.setEnabledCosmetic(category, (CosmeticType<?>) null);
        }
        return equipped.remove(category);
    }

    /**
     * Sets a cosmetic as equipped, unequipping any cosmetic
     * of the same category that is already equipped.
     * <p>
     * Note that this does not actually call equip() on the cosmetic,
     * equipping should be done before this method is called.
     * <p>
     * Category of the cosmetic is automatically determined.
     * For equipping a Suit part, please use setCurrentSuitPart(ArmorSlot, Suit)
     *
     * @param cosmetic The cosmetic to set as equipped.
     */
    public void setCosmeticEquipped(Cosmetic<?> cosmetic) {
        removeCosmetic(cosmetic.getCategory());
        equipped.put(cosmetic.getCategory(), cosmetic);
        if (!preserveEquipped) {
            cosmeticsProfile.setEnabledCosmetic(cosmetic.getCategory(), cosmetic);
        }
    }

    public void addKeys(int amount) {
        cosmeticsProfile.addKeys(amount);
    }

    /**
     * Gives a key to the player.
     */
    public void addKey() {
        addKeys(1);
    }

    /**
     * Removes a key from the player.
     */
    public void removeKey() {
        addKeys(-1);
    }

    /**
     * @return The amount of keys that the player owns.
     */
    public int getKeys() {
        return cosmeticsProfile.getKeys();
    }

    public void saveCosmeticsProfile() {
        cosmeticsProfile.save();
    }

    /**
     * Returns true if the player has any cosmetics equipped
     */
    public boolean hasCosmeticsEquipped() {
        return equipped.size() > 0;
    }

    /**
     * Clears all gadgets.
     */
    public boolean clear() {
        boolean toReturn = hasCosmeticsEquipped();
        if (!preserveEquipped) {
            cosmeticsProfile.clearAllEquipped();
        }
        if (Category.MORPHS.isEnabled() && Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")
                // Ensure disguises in non-enabled worlds (not from UC) aren't cleared on accident.
                // If player is "quitting", remove the disguise anyway. Player is marked as quitting
                // when changing worlds, making sure morphs get correctly unset.
                && (preserveEquipped || SettingsManager.isAllowedWorld(getBukkitPlayer().getWorld()))) {
            removeCosmetic(Category.MORPHS);
        }
        for (Category cat : Category.values()) {
            // handled above
            if (cat == Category.MORPHS) {
                continue;
            }
            removeCosmetic(cat);
        }
        removeTreasureChest();
        return toReturn;
    }

    /**
     * Saves player data and removes all cosmetics from them.
     * Do NOT continue using object after this is called.
     */
    public void dispose() {
        preserveEquipped = true;
        if (currentTreasureChest != null) {
            currentTreasureChest.forceOpen(0);
        }
        saveCosmeticsProfile();
        clear();
        removeMenuItem();
    }

    /**
     * Sets the name of a pet.
     *
     * @param petType The pet name.
     * @param name    The new name.
     */
    public void setPetName(PetType petType, String name) {
        if (name != null && name.isEmpty()) {
            name = null;
        }
        cosmeticsProfile.setPetName(petType, name);
        if (hasCosmetic(Category.PETS)) {
            ((Pet) getCosmetic(Category.PETS)).updateName();
        }
    }

    /**
     * Gets the name of a pet.
     *
     * @param petType The pet type.
     * @return The pet name.
     */
    public Component getPetName(PetType petType) {
        String rawName = cosmeticsProfile.getPetName(petType);
        if (rawName == null) return null;
        return MessageManager.getMiniMessage().deserialize(cosmeticsProfile.getPetName(petType));
    }

    /**
     * Gives ammo to player.
     *
     * @param type   The gadget type.
     * @param amount The ammo amount to give.
     */
    public void addAmmo(GadgetType type, int amount) {
        if (!UltraCosmeticsData.get().isAmmoEnabled()) return;
        cosmeticsProfile.addAmmo(type, amount);
        Gadget gadget = getCurrentGadget();
        if (gadget != null) {
            gadget.equipItem();
        }
    }

    public void applyVelocity(Vector vector) {
        getBukkitPlayer().setVelocity(vector);
        ultraCosmetics.getScheduler().runLaterAsync(() -> FallDamageManager.addNoFall(getBukkitPlayer()), 2);
    }

    /**
     * Sets if player has gadgets enabled.
     *
     * @param enabled if player has gadgets enabled.
     */
    public void setGadgetsEnabled(boolean enabled) {
        cosmeticsProfile.setGadgetsEnabled(enabled);

        if (enabled) {
            MessageManager.send(getBukkitPlayer(), "Enabled-Gadgets");
        } else {
            MessageManager.send(getBukkitPlayer(), "Disabled-Gadgets");
        }
    }

    /**
     * @return if the player has gadgets enabled or not.
     */
    public boolean hasGadgetsEnabled() {
        return !allowDisableGadgets || cosmeticsProfile.hasGadgetsEnabled();
    }

    /**
     * Sets if a player can see his own morph or not.
     *
     * @param enabled if player should be able to see his own morph.
     */
    public void setSeeSelfMorph(boolean enabled) {
        cosmeticsProfile.setSeeSelfMorph(enabled);
        if (enabled) {
            MessageManager.send(getBukkitPlayer(), "Enabled-SelfMorphView");
        } else {
            MessageManager.send(getBukkitPlayer(), "Disabled-SelfMorphView");
        }
        if (hasCosmetic(Category.MORPHS)) {
            getCurrentMorph().setSeeSelf(enabled);
        }
    }

    /**
     * @return if player should be able to see his own morph or not.
     */
    public boolean canSeeSelfMorph() {
        return cosmeticsProfile.canSeeSelfMorph();
    }

    /**
     * Gets the ammo of a gadget.
     *
     * @param type The gadget type.
     * @return The ammo of the given gadget.
     */
    public int getAmmo(GadgetType type) {
        if (!UltraCosmeticsData.get().isAmmoEnabled()) return 0;
        return cosmeticsProfile.getAmmo(type);
    }

    /**
     * Clears current Treasure Chest.
     */
    public void removeTreasureChest() {
        if (currentTreasureChest == null) return;
        this.currentTreasureChest.clear();
        this.currentTreasureChest = null;
    }

    /**
     * Removes One Ammo of a gadget.
     *
     * @param type The gadget.
     */
    public void removeAmmo(GadgetType type) {
        addAmmo(type, -1);
    }

    /**
     * Gives the Menu Item.
     */
    public void giveMenuItem() {
        if (!menuItemEnabled || getBukkitPlayer() == null) {
            return;
        }
        removeMenuItem();
        int slot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
        ItemStack slotItem = getBukkitPlayer().getInventory().getItem(slot);
        if (slotItem != null && !MenuItemHandler.isMenuItem(slotItem)) {
            getBukkitPlayer().getWorld().dropItemNaturally(getBukkitPlayer().getLocation(), slotItem);
        }
        getBukkitPlayer().getInventory().setItem(slot, MenuItemHandler.getMenuItem());
    }

    /**
     * Removes the menu Item.
     */
    public void removeMenuItem() {
        if (!menuItemEnabled) return;
        Player player = getBukkitPlayer();
        if (player != null) {
            PlayerUtils.removeItems(player, MenuItemHandler::isMenuItem);
        }
    }

    public void sendMessage(String message) {
        if (message.isEmpty()) return;
        getBukkitPlayer().sendMessage(message);
    }

    public void sendMessage(Component message) {
        if (message == null || message.equals(Component.empty())) return;
        MessageManager.getAudiences().player(getBukkitPlayer()).sendMessage(message);
    }

    /**
     * Gets the UUID.
     *
     * @return The UUID.
     */
    public UUID getUUID() {
        return uuid;
    }

    public TreasureChest getCurrentTreasureChest() {
        return currentTreasureChest;
    }

    public void setCurrentTreasureChest(TreasureChest currentTreasureChest) {
        this.currentTreasureChest = currentTreasureChest;
    }

    public void setCanBeHitByOtherGadgets(boolean canBeHitByOtherGadgets) {
        this.canBeHitByOtherGadgets = canBeHitByOtherGadgets;
    }

    public boolean canBeHitByOtherGadgets() {
        return canBeHitByOtherGadgets;
    }

    public boolean isOnline() {
        return Bukkit.getServer().getPlayer(uuid) != null;
    }

    /**
     * @return true if equipped cosmetics should not be removed from external data sources.
     * @see #withPreserveEquipped(Runnable)
     */
    public boolean isPreserveEquipped() {
        return preserveEquipped;
    }

    /**
     * Runs code without marking any cosmetics as no longer equipped in
     * external data sources. This allows for cosmetics to be temporarily
     * cleared, such as on death, and then restored with a call to {@link CosmeticsProfile#equip()}
     *
     * @param runnable The runnable to run while preserving equipped cosmetics.
     */
    public void withPreserveEquipped(Runnable runnable) {
        this.preserveEquipped = true;
        try {
            runnable.run();
        } finally {
            this.preserveEquipped = false;
        }
    }

    public boolean isBypassingCooldown() {
        return getBukkitPlayer().hasPermission("ultracosmetics.bypass.cooldown");
    }

    public boolean isFilteringByOwned() {
        return cosmeticsProfile.isFilterByOwned();
    }

    public void setFilteringByOwned(boolean filterByOwned) {
        cosmeticsProfile.setFilterByOwned(filterByOwned);
    }

    public boolean isTreasureNotifying() {
        return cosmeticsProfile.isTreasureNotifications();
    }

    public void setTreasureNotifying(boolean treasureNotifications) {
        cosmeticsProfile.setTreasureNotifications(treasureNotifications);
    }

    /**
     * Clients with brand "Geyser" are not guaranteed to actually be Geyser clients,
     * but all Geyser clients will have brand "Geyser". If some Java client wants
     * to see the Geyser menu view enough to change its brand, UC has no problems with that.
     * <p>
     * Not currently used anywhere.
     *
     * @return {@code true} if the client says it is a Geyser client.
     */
    public boolean isGeyserClient() {
        // Inverted equals so we don't have to worry about nulls
        return "Geyser".equals(clientBrand);
    }

    public void setClientBrand(String brand) {
        this.clientBrand = brand;
    }

    public CosmeticsProfile getProfile() {
        return cosmeticsProfile;
    }
}
