package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.type.GadgetType;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.treasurechests.loot.*;
import be.isach.ultracosmetics.util.EntitySpawner;
import be.isach.ultracosmetics.util.WeightedSet;
import com.cryptomorin.xseries.XSound;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureRandomizer {
    private final int moneyChance = SettingsManager.getConfig().getInt("TreasureChests.Loots.Money.Chance");
    private final int ammoChance = SettingsManager.getConfig().getInt("TreasureChests.Loots.Gadgets-Ammo.Chance");
    private final WeightedSet<Loot> lootTypes = new WeightedSet<>();
    private Location loc;
    private final Player player;
    private final boolean forceMessageToOwner;
    private final MoneyLoot money = new MoneyLoot();
    private final NothingLoot nothing = new NothingLoot();
    private final AmmoLoot ammo;
    private final boolean canAddAmmo;
    private final XSound.SoundPlayer sound =
            XSound.BLOCK_CHEST_OPEN.record().withVolume(1.4f).withPitch(1.5f).soundPlayer();

    public TreasureRandomizer(final Player player, Location location, boolean forceMessageToOwner) {
        this.loc = location.add(0.5, 0, 0.5);
        this.player = player;
        this.forceMessageToOwner = forceMessageToOwner;
        // add ammo.
        canAddAmmo = Category.GADGETS.isEnabled() && UltraCosmeticsData.get().isAmmoEnabled()
                && SettingsManager.getConfig().getBoolean("TreasureChests.Loots.Gadgets-Ammo.Enabled");
        ammo = new AmmoLoot(player);
        if (!ammo.isEmpty() && canAddAmmo) {
            lootTypes.add(ammo, ammoChance);
        }
        initializeCommandLoot();

        for (Category cat : Category.values()) {
            setupChance(cat);
        }

        if (UltraCosmeticsData.get().useMoneyTreasureLoot()) {
            lootTypes.add(money, moneyChance);
        }
    }

    public TreasureRandomizer(final Player player, Location location) {
        this(player, location, false);
    }

    private void initializeCommandLoot() {
        CustomConfiguration config = UltraCosmeticsData.get().getPlugin().getConfig();
        ConfigurationSection commandLootConfig = config.getConfigurationSection("TreasureChests.Loots.Commands");
        int globalCommandWeight = commandLootConfig.getInt("Overall-Chance", 0);
        CommandLootContainer container = null;
        if (globalCommandWeight > 0) {
            container = new CommandLootContainer();
        }
        for (String key : commandLootConfig.getKeys(false)) {
            if (!commandLootConfig.isConfigurationSection(key) || !commandLootConfig.getBoolean(key + ".Enabled")) {
                continue;
            }
            String cancelPermission = commandLootConfig.getString(key + ".Cancel-If-Permission");
            if (cancelPermission.equals("no") || !player.hasPermission(cancelPermission)) {
                CommandReward reward = new CommandReward("TreasureChests.Loots.Commands." + key);
                if (container == null) {
                    lootTypes.add(new CommandLoot(reward), reward.getChance());
                } else {
                    container.addCommandLoot(new CommandLoot(reward), reward.getChance());
                }
            }
        }
        if (container != null && container.getSize() > 0) {
            lootTypes.add(container, globalCommandWeight);
        }
    }

    private void setupChance(Category category) {
        String configPath = "TreasureChests.Loots." + category.getConfigPath();
        if (!SettingsManager.getConfig().getBoolean(configPath + ".Enabled")) {
            return;
        }
        if (!category.isEnabled()) {
            return;
        }
        int chance = SettingsManager.getConfig().getInt(configPath + ".Chance");
        if (chance <= 0) {
            return;
        }
        CosmeticLoot loot = new CosmeticLoot(category, player);
        if (!loot.isEmpty()) {
            lootTypes.add(loot, chance);
        }
    }

    private static Color randomColor() {
        Random r = ThreadLocalRandom.current();
        return Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
    }

    public LootReward giveRandomThing(TreasureChest chest, boolean skipFirework) {
        UltraPlayer ultraPlayer = UltraCosmeticsData.get().getPlugin().getPlayerManager().getUltraPlayer(player);
        sound.atLocation(loc).play();
        if (lootTypes.size() == 0) {
            return giveFallback(ultraPlayer, chest);
        }
        Loot loot = lootTypes.getRandom();
        LootReward reward = loot.giveToPlayer(ultraPlayer, chest);
        broadcast(reward.getMessage(), reward.isBroadcast());
        if (!skipFirework && reward.isFirework()) {
            spawnFirework();
        }
        if (loot.isEmpty()) {
            lootTypes.remove(loot);
        }
        if (loot instanceof CosmeticLoot && ((CosmeticLoot) loot).getCategory() == Category.GADGETS) {
            ammo.add((GadgetType) reward.getCosmetic());
            if (canAddAmmo && !lootTypes.contains(ammo)) {
                lootTypes.add(ammo, ammoChance);
            }
        }

        return reward;
    }

    private LootReward giveFallback(UltraPlayer player, TreasureChest chest) {
        if (UltraCosmeticsData.get().useMoneyTreasureLoot()) {
            return money.giveToPlayer(player, chest);
        }
        return nothing.giveToPlayer(player, chest);
    }

    public void spawnFirework() {
        EntitySpawner.spawnFireworks(loc.clone().add(0.5, 0, 0.5), randomColor(), randomColor(),
                FireworkEffect.Type.BALL);
    }

    private void broadcast(Component message, boolean toOthers) {
        if (message == null) {
            return;
        }
        UltraCosmetics ultraCosmetics = UltraCosmeticsData.get().getPlugin();
        if (ultraCosmetics.getDiscordHook() != null) {
            ultraCosmetics.getDiscordHook().sendLootMessage(player, MessageManager.toLegacy(message));
        }
        BukkitAudiences audiences = MessageManager.getAudiences();
        if (!toOthers) {
            if (forceMessageToOwner) {
                audiences.player(player).sendMessage(message);
            }
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == this.player || (SettingsManager.isAllowedWorld(player.getWorld())
                    && ultraCosmetics.getPlayerManager().getUltraPlayer(player).isTreasureNotifying())) {
                audiences.player(player).sendMessage(message);
            }
        }
    }

    public void setLocation(Location newLoc) {
        loc = newLoc;
    }
}
