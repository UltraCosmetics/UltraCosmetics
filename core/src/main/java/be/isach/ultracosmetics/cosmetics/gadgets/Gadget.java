package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.Cosmetic;
import be.isach.ultracosmetics.cosmetics.PlayerAffectingCosmetic;
import be.isach.ultracosmetics.cosmetics.Updatable;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.PlayerUtils;
import be.isach.ultracosmetics.util.TextUtil;
import be.isach.ultracosmetics.util.UnmovableItemProvider;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Represents an instance of a Gadget summoned by a player.
 *
 * @author iSach
 * @since 08-03-2015
 */
public abstract class Gadget extends Cosmetic<GadgetType> implements UnmovableItemProvider {

    private static final DecimalFormatSymbols OTHER_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        OTHER_SYMBOLS.setDecimalSeparator('.');
        OTHER_SYMBOLS.setGroupingSeparator('.');
        OTHER_SYMBOLS.setPatternSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("0.0", OTHER_SYMBOLS);
    }

    /**
     * Gadget ItemStack.
     */
    protected ItemStack itemStack;

    /**
     * If true, will display cooldown left when fail on use because cooldown active.
     */
    protected boolean displayCooldownMessage = true;

    /**
     * Last Clicked Block by the player.
     */
    protected Block lastClickedBlock;

    /**
     * If Gadget interaction should tick asynchronously.
     */
    private final boolean asynchronous;

    // Cache the actual material value so we don't have to keep calling parseMaterial

    protected final int slot = SettingsManager.getConfig().getInt("Gadget-Slot");

    private final boolean removeWithDrop = SettingsManager.getConfig().getBoolean("Remove-Gadget-With-Drop");

    private final boolean showCooldownInBar = UltraCosmeticsData.get().displaysCooldownInBar();

    private final boolean requiresAmmo = UltraCosmeticsData.get().isAmmoEnabled() && getType().requiresAmmo();

    private final XSound.SoundPlayer readySound;

    private boolean handledThisTick = false;

    public Gadget(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics) {
        this(owner, type, ultraCosmetics, false);
    }

    public Gadget(UltraPlayer owner, GadgetType type, UltraCosmetics ultraCosmetics, boolean asynchronous) {
        super(owner, type, ultraCosmetics);
        this.asynchronous = asynchronous;
        readySound = XSound.BLOCK_NOTE_BLOCK_HAT.record().withVolume(1.4f).withPitch(1.5f).soundPlayer().forPlayers(getPlayer());
    }

    @Override
    public boolean tryEquip() {
        getOwner().removeCosmetic(Category.GADGETS);
        if (getPlayer().getInventory().getItem(slot) != null) {
            MessageManager.send(getPlayer(), "Must-Remove.Gadgets",
                    Placeholder.unparsed("slot", String.valueOf(slot + 1))
            );
            return false;
        }

        equipItem();
        return true;
    }

    @Override
    public void onEquip() {
        // All gadgets need a task running if we need to show the cooldown bar.
        // The base Cosmetic class schedules it anyway if the cosmetic is Updatable,
        // so we just need to handle the other case.
        // The base Cosmetic class attempts to cancel the task for Updatable and
        // non-Updatable cosmetics alike, so we don't need to worry about that.
        if (!(this instanceof Updatable)) {
            scheduleTask();
        }
        getUltraCosmetics().getUnmovableItemListener().addProvider(this);
    }

    @Override
    public void run() {
        // For some reason, the client sends two `use` packets for snowballs and
        // ender pearls, so we have to figure out how to ignore one of them.
        handledThisTick = false;
        if (getOwner() == null || getPlayer() == null) return;

        UltraPlayer owner = getOwner();
        if (owner.getCurrentGadget() == null || owner.getCurrentGadget().getType() != getType()) {
            clear();
            return;
        }
        if (this instanceof Updatable) {
            ((Updatable) this).onUpdate();
        }
        if (showCooldownInBar) {
            @SuppressWarnings("deprecation")
            ItemStack hand = getPlayer().getItemInHand();
            if (itemMatches(hand) && !owner.canUse(getType())) {
                owner.sendCooldownBar(getType(), getType().getCountdown(), getType().getRunTime());
            }
        }

        double left = owner.getCooldown(getType());
        if (left > 0) {
            String leftRounded = DECIMAL_FORMAT.format(left);
            double decimalRoundedValue = Double.parseDouble(leftRounded);
            if (decimalRoundedValue == 0) {
                String message = MessageManager.getLegacyMessage("Gadgets.Gadget-Ready-ActionBar",
                        Placeholder.component("gadgetname", TextUtil.stripColor(getTypeName()))
                );
                ActionBar.sendActionBar(getPlayer(), message);
                readySound.play();
            }
        }
    }

    @Override
    public void clear() {
        removeItem();
        getUltraCosmetics().getUnmovableItemListener().removeProvider(this);
        super.clear();
    }

    /**
     * Removes the item from the player.
     * Does not assume item is in the correct slot,
     * or that there is only one item.
     */
    public void removeItem() {
        PlayerUtils.removeItems(getPlayer(), this::itemMatches);
    }

    /**
     * Gets the gadget current Item Stack.
     * If using ammo system, this may not be up-to-date.
     * Call {@link #updateItemStack()} to update itemstack immediately.
     *
     * @return current itemstack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    private Component getItemDisplayName() {
        if (!requiresAmmo || getUltraCosmetics().getWorldGuardManager().isInShowroom(getPlayer())) {
            return getTypeName();
        }
        Component ammo = Component.text(getOwner().getAmmo(getType()) + " ", NamedTextColor.WHITE, TextDecoration.BOLD);
        return Component.empty().append(ammo).append(getTypeName());
    }

    public void updateItemStack() {
        itemStack = ItemFactory.create(getType().getMaterial(), getItemDisplayName(), MessageManager.getLegacyMessage("Gadgets.Lore"));
        ItemFactory.applyCosmeticMarker(itemStack);
    }

    public void equipItem() {
        updateItemStack();
        getPlayer().getInventory().setItem(slot, this.itemStack);
    }

    protected boolean checkRequirements(PlayerInteractEvent event) {
        return true;
    }

    public boolean itemMatches(ItemStack stack) {
        if (stack == null || stack.getType() != getItemStack().getType() || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
            return false;
        }

        // If a gadget name is the default color for the item, we won't actually see it
        // in getDisplayName(), so we have to correct for that in the original display name.
        String displayName = MessageManager.toLegacy(getItemDisplayName());
        if (displayName.startsWith("§f")) {
            displayName = displayName.substring(2);
        }
        // Case sensitivity causes issues with hex color codes for some reason, even with MiniMessage
        return stack.getItemMeta().getDisplayName().equalsIgnoreCase(displayName);
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        if (handledThisTick) return;
        handledThisTick = true;
        UltraPlayer ultraPlayer = getUltraCosmetics().getPlayerManager().getUltraPlayer(player);

        if (ultraPlayer.getCurrentTreasureChest() != null) return;

        if (PlayerAffectingCosmetic.isHidden(event.getPlayer(), getPlayer()) && SettingsManager.getConfig().getBoolean("Prevent-Cosmetics-In-Vanish")) {
            getOwner().clear();
            MessageManager.send(getPlayer(), "Not-Allowed-In-Vanish");
            return;
        }

        if (!ultraPlayer.hasGadgetsEnabled()) {
            MessageManager.send(getPlayer(), "Gadgets-Enabled-Needed");
            return;
        }

        boolean inShowroom = getUltraCosmetics().getWorldGuardManager().isInShowroom(player);
        if (requiresAmmo && ultraPlayer.getAmmo(getType()) < 1 && !inShowroom) {
            if (UltraCosmeticsData.get().isAmmoPurchaseEnabled() && getUltraCosmetics().getEconomyHandler().isUsingEconomy()) {
                getUltraCosmetics().getMenus().openAmmoPurchaseMenu(getType(), getOwner(), () -> {
                });
            } else {
                MessageManager.send(getPlayer(), "No-Ammo");
            }
            return;
        }

        if (!checkRequirements(event)) return;

        double coolDown = ultraPlayer.getCooldown(getType());
        if (coolDown > 0) {
            String timeLeft = new DecimalFormat("#.#").format(coolDown);
            if (getType().getCountdown() > 1) {
                MessageManager.send(getPlayer(), "Gadgets.Countdown-Message",
                        Placeholder.component("gadgetname", TextUtil.stripColor(getTypeName())),
                        Placeholder.unparsed("time", String.valueOf(timeLeft))
                );
            }
            return;
        }
        ultraPlayer.setCooldown(getType(), getType().getCountdown(), getType().getRunTime());
        if (requiresAmmo && !inShowroom) {
            ultraPlayer.removeAmmo(getType());
            equipItem();
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            lastClickedBlock = event.getClickedBlock();
        }
        boolean isLeft = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        Runnable callClick = isLeft ? this::onLeftClick : this::onRightClick;
        if (asynchronous) {
            Bukkit.getScheduler().runTaskAsynchronously(getUltraCosmetics(), callClick);
        } else {
            callClick.run();
        }
    }

    /**
     * Called when a right-click is performed, and potentially when a left-click
     * is performed, depending on the implementation of onLeftClick()
     */
    protected abstract void onRightClick();

    /**
     * Called when a left click is done with gadget.
     */
    protected void onLeftClick() {
        onRightClick();
    }

    @Override
    public void handleDrop(PlayerDropItemEvent event) {
        if (removeWithDrop) {
            clear();
            event.getItemDrop().remove();
        } else {
            event.setCancelled(true);
        }
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void moveItem(int slot, Player player) {
        clear();
    }
}
