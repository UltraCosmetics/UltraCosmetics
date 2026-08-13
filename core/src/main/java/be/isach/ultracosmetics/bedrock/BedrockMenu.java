package be.isach.ultracosmetics.bedrock;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.CommandManager;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.cosmetics.type.PetType;
import be.isach.ultracosmetics.menu.CosmeticActions;
import be.isach.ultracosmetics.menu.VirtualCategory;
import be.isach.ultracosmetics.menu.buttons.VirtualCategorySelectorButton;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The cosmetics menu rendered as native Bedrock forms instead of a chest GUI, for Bedrock
 * players on touchscreen devices. Mirrors the virtual-category layout of
 * {@link be.isach.ultracosmetics.menu.menus.MenuUnified}.
 *
 * <p>Unlike the inventory menus this holds <b>no per-player state</b>: every action
 * rebuilds and resends a fresh form, so there is nothing to clean up on quit.
 *
 * <p><b>Threading:</b> Cumulus invokes result handlers on a netty thread. Every handler
 * here therefore does nothing but hand off to
 * {@link be.isach.ultracosmetics.UltraCosmetics#getScheduler()} via
 * {@link #onPlayerThread}, and all player state changes happen inside that. Touching
 * Bukkit state directly from a handler would race on Paper and throw on Folia.
 */
public class BedrockMenu {

    private final UltraCosmetics ultraCosmetics;

    public BedrockMenu(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
    }

    /**
     * Sends the top-level form: one button per configured virtual category, plus the
     * treasure chest entries when they're enabled.
     */
    public void open(UltraPlayer player) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (!hasMenuPermission(bukkitPlayer)) {
            CommandManager.sendNoPermissionMessage(bukkitPlayer);
            return;
        }

        List<VirtualCategory> categories = ultraCosmetics.getMenus().getUnifiedMenu().getVirtualCategories();
        List<Runnable> actions = new ArrayList<>();
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(MessageManager.getLegacyMessage("Menu.Unified.Title"))
                .content(MessageManager.getLegacyMessage("Menu.Unified.Description"));

        for (VirtualCategory category : categories) {
            builder.button(VirtualCategorySelectorButton.renderName(category.getDisplayName()));
            actions.add(() -> openVirtual(player, category));
        }

        if (UltraCosmeticsData.get().areTreasureChestsEnabled()) {
            builder.button(MessageManager.getLegacyMessage("Treasure-Chests"));
            actions.add(() -> ultraCosmetics.getTreasureChestManager().tryOpenChest(bukkitPlayer));

            builder.button(MessageManager.getLegacyMessage("Treasure-Keys") + "\n"
                    + MessageManager.getLegacyMessage("Your-Keys",
                    Placeholder.unparsed("keys", String.valueOf(player.getKeys()))));
            // Self-gating: returns silently when economy, price or permission don't allow it.
            actions.add(() -> ultraCosmetics.getMenus().openKeyPurchaseMenu(player));
        }

        dispatch(player, builder, actions);
    }

    /**
     * Sends the form for a single virtual category: its cosmetics, the owned-only filter,
     * a clear button, the configured extras and a way back.
     */
    public void openVirtual(UltraPlayer player, VirtualCategory category) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (!hasMenuPermission(bukkitPlayer)) {
            CommandManager.sendNoPermissionMessage(bukkitPlayer);
            return;
        }
        if (category == null) {
            open(player);
            return;
        }

        List<Runnable> actions = new ArrayList<>();
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(MessageManager.getLegacyMessage("Menu.Unified.Title"))
                .content(VirtualCategorySelectorButton.renderName(category.getDisplayName()));

        for (CosmeticType<?> type : category.getCosmetics()) {
            if (CosmeticActions.shouldHide(ultraCosmetics, player, type)) {
                continue;
            }
            builder.button(cosmeticLabel(player, type));
            actions.add(() -> onCosmeticClick(player, category, type));
        }

        builder.button(MessageManager.getLegacyMessage(
                player.isFilteringByOwned() ? "Disable-Filter-By-Owned" : "Enable-Filter-By-Owned"));
        actions.add(() -> {
            CosmeticActions.toggleFilter(player);
            openVirtual(player, category);
        });

        builder.button(MessageManager.getLegacyMessage("Clear.Cosmetics"));
        actions.add(() -> {
            CosmeticActions.clearCategories(player, category.getCoveredCategories());
            openVirtual(player, category);
        });

        addExtras(player, category, builder, actions);

        builder.button(MessageManager.getLegacyMessage("Menu.Main.Button.Name"));
        actions.add(() -> open(player));

        dispatch(player, builder, actions);
    }

    /**
     * Adds the {@code Extras:} buttons for this virtual category, honoring the same config
     * gates as the inventory menu. {@code CLEAR} is skipped because a clear button is
     * always present already.
     */
    private void addExtras(UltraPlayer player, VirtualCategory category,
                           SimpleForm.Builder builder, List<Runnable> actions) {
        for (Map.Entry<Integer, VirtualCategory.ExtraButton> entry : category.getExtras().entrySet()) {
            switch (entry.getValue()) {
                case RENAME_PET:
                    if (!SettingsManager.getConfig().getBoolean("Pets-Rename.Enabled")) continue;
                    if (SettingsManager.getConfig().getBoolean("Pets-Rename.Permission-Required")
                            && !player.getBukkitPlayer().hasPermission("ultracosmetics.pets.rename")) continue;
                    builder.button(MessageManager.getLegacyMessage("Menu.Rename-Pet.Title"));
                    actions.add(() -> openRenamePet(player, category));
                    break;
                case TOGGLE_GADGETS:
                    if (!SettingsManager.getConfig()
                            .getBoolean("Categories.Gadgets.Allow-Disable-Gadgets", true)) continue;
                    builder.button(MessageManager.getLegacyMessage(
                            player.hasGadgetsEnabled() ? "Disable-Gadgets" : "Enable-Gadgets"));
                    actions.add(() -> {
                        player.setGadgetsEnabled(!player.hasGadgetsEnabled());
                        openVirtual(player, category);
                    });
                    break;
                case TOGGLE_MORPH_SELF_VIEW:
                    builder.button(MessageManager.getLegacyMessage(player.canSeeSelfMorph()
                            ? "Disable-Third-Person-View" : "Enable-Third-Person-View"));
                    actions.add(() -> {
                        player.setSeeSelfMorph(!player.canSeeSelfMorph());
                        openVirtual(player, category);
                    });
                    break;
                case CLEAR:
                    // Already added unconditionally above; don't duplicate it.
                    break;
            }
        }
    }

    /**
     * Button text for a cosmetic: the same "Spawn/Despawn Wolf" wording the inventory menu
     * uses, plus a second line for the ammo count or the missing-permission note.
     */
    private String cosmeticLabel(UltraPlayer player, CosmeticType<?> type) {
        Component tooltip = CosmeticActions.isEquipped(player, type)
                ? type.getCategory().getDeactivateTooltip()
                : type.getCategory().getActivateTooltip();
        StringBuilder label = new StringBuilder(MessageManager.toLegacy(
                Component.empty().append(tooltip).appendSpace().append(type.getName())));

        if (!player.canEquip(type)) {
            label.append('\n').append(MessageManager.getLegacyMessage("Permission-Lore.Permission-No"));
        } else if (showsAmmo(type)) {
            label.append('\n').append(MessageManager.getLegacyMessage("Ammo",
                    Placeholder.unparsed("ammo", String.valueOf(player.getAmmo((GadgetType) type)))));
        }
        return label.toString();
    }

    private boolean showsAmmo(CosmeticType<?> type) {
        return type instanceof GadgetType
                && UltraCosmeticsData.get().isAmmoEnabled()
                && ((GadgetType) type).requiresAmmo();
    }

    private void onCosmeticClick(UltraPlayer player, VirtualCategory category, CosmeticType<?> type) {
        if (!player.canEquip(type)) {
            // Cosmetics are permission-only here: no purchase flow in forms.
            player.sendMessage(MessageManager.getMessage("No-Permission"));
            openVirtual(player, category);
            return;
        }
        // Equipping an ammo-gated gadget with no ammo left would silently do nothing, so
        // report the ammo count instead of equipping. Buying ammo stays in the Java menu.
        if (showsAmmo(type) && !CosmeticActions.isEquipped(player, type)
                && player.getAmmo((GadgetType) type) < 1) {
            player.sendMessage(MessageManager.getMessage("Ammo", Placeholder.unparsed("ammo", "0")));
            openVirtual(player, category);
            return;
        }
        CosmeticActions.toggleCosmetic(ultraCosmetics, player, type);
        openVirtual(player, category);
    }

    // ---------------------------------------------------------------- rename pet

    private void openRenamePet(UltraPlayer player, VirtualCategory category) {
        if (player.getCurrentPet() == null) {
            player.sendMessage(MessageManager.getMessage("Active-Pet-Needed"));
            openVirtual(player, category);
            return;
        }
        String current = player.getProfile().getPetName(player.getCurrentPet().getType());
        if (current == null) {
            current = MessageManager.getLegacyMessage("Menu.Rename-Pet.Placeholder");
        }
        sendRenameInput(player, category, current, null);
    }

    /**
     * Text input for the new pet name. Any error is prepended to the input's own label
     * rather than added as a separate label component, so the response always has exactly
     * one value to read.
     */
    private void sendRenameInput(UltraPlayer player, VirtualCategory category, String current, String error) {
        String placeholder = MessageManager.getLegacyMessage("Menu.Rename-Pet.Placeholder");
        String inputLabel = MessageManager.getLegacyMessage("Menu.Rename-Pet.Title");
        if (error != null) {
            inputLabel = error + "\n" + inputLabel;
        }

        Form form = CustomForm.builder()
                .title(MessageManager.getLegacyMessage("Menu.Rename-Pet.Title"))
                .input(inputLabel, placeholder, current)
                .validResultHandler(response -> {
                    String name = response.asInput();
                    onPlayerThread(player, () -> {
                        if (name == null || name.isEmpty()) {
                            openVirtual(player, category);
                            return;
                        }
                        if (CosmeticActions.isPetNameTooLong(name)) {
                            sendRenameInput(player, category, name, MessageManager.getLegacyMessage("Too-Long"));
                            return;
                        }
                        if (ultraCosmetics.getEconomyHandler().isUsingEconomy()
                                && SettingsManager.getConfig().getBoolean("Pets-Rename.Requires-Money.Enabled")) {
                            sendRenameConfirm(player, category, name);
                        } else {
                            applyPetName(player, name);
                            openVirtual(player, category);
                        }
                    });
                })
                .closedOrInvalidResultHandler(() -> {
                })
                .build();
        send(player, form);
    }

    /**
     * Confirmation step charging for the rename, replacing the Java purchase inventory so
     * the whole flow stays native on mobile.
     */
    private void sendRenameConfirm(UltraPlayer player, VirtualCategory category, String name) {
        int price = SettingsManager.getConfig().getInt("Pets-Rename.Requires-Money.Price");
        Player bukkitPlayer = player.getBukkitPlayer();
        int discountPrice = ultraCosmetics.getEconomyHandler().calculateDiscountPrice(bukkitPlayer, price);
        String content = MessageManager.getLegacyMessage("Menu.Purchase-Rename.Button.Showcase",
                Placeholder.unparsed("price", TextUtil.formatNumber(discountPrice)),
                Placeholder.component("name", MessageManager.getMiniMessage().deserialize(name)));

        Form form = SimpleForm.builder()
                .title(MessageManager.getLegacyMessage("Menu.Purchase-Rename.Title"))
                .content(content)
                .button(MessageManager.getLegacyMessage("Purchase"))
                .button(MessageManager.getLegacyMessage("Cancel"))
                .validResultHandler(response -> {
                    int clicked = response.clickedButtonId();
                    onPlayerThread(player, () -> {
                        if (clicked != 0) {
                            openVirtual(player, category);
                            return;
                        }
                        // withdrawWithDiscount applies the discount itself, so pass the base price.
                        // Economy hooks may answer asynchronously, hence the inner hop back.
                        ultraCosmetics.getEconomyHandler().withdrawWithDiscount(bukkitPlayer, price,
                                () -> onPlayerThread(player, () -> {
                                    applyPetName(player, name);
                                    openVirtual(player, category);
                                }),
                                () -> onPlayerThread(player, () -> {
                                    MessageManager.send(bukkitPlayer, "Not-Enough-Money");
                                    openVirtual(player, category);
                                }));
                    });
                })
                .closedOrInvalidResultHandler(() -> {
                })
                .build();
        send(player, form);
    }

    /**
     * Applies the name, re-checking the pet since it may have been despawned while the
     * form was open.
     */
    private void applyPetName(UltraPlayer player, String name) {
        if (player.getCurrentPet() == null) {
            player.sendMessage(MessageManager.getMessage("Active-Pet-Needed"));
            return;
        }
        PetType petType = player.getCurrentPet().getType();
        player.setPetName(petType, name);
    }

    // ---------------------------------------------------------------- plumbing

    private boolean hasMenuPermission(Player bukkitPlayer) {
        return bukkitPlayer.hasPermission(ultraCosmetics.getMenus().getUnifiedMenu().getPermission());
    }

    /**
     * Wires the collected actions to button indices and sends the form. Indices are taken
     * from the list rather than hardcoded, because several buttons are conditional.
     */
    private void dispatch(UltraPlayer player, SimpleForm.Builder builder, List<Runnable> actions) {
        Form form = builder
                .validResultHandler(response -> {
                    int clicked = response.clickedButtonId();
                    if (clicked < 0 || clicked >= actions.size()) {
                        return;
                    }
                    onPlayerThread(player, actions.get(clicked));
                })
                .closedOrInvalidResultHandler(() -> {
                    // Player closed the form. Deliberately does nothing: resending here
                    // would trap them in a form they can't dismiss.
                })
                .build();
        send(player, form);
    }

    private void send(UltraPlayer player, Form form) {
        FloodgateApi.getInstance().sendForm(player.getUUID(), form);
    }

    /**
     * Runs the given work on the thread that owns the player, since form responses arrive
     * off the main thread.
     */
    private void onPlayerThread(UltraPlayer player, Runnable action) {
        ultraCosmetics.getScheduler().runAtEntity(player.getBukkitPlayer(), task -> action.run());
    }
}
