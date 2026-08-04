package be.isach.ultracosmetics.treasurechests;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.CustomConfiguration;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.util.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CommandReward {
    private final String name;
    private final ItemStack stack;
    private final int chance;
    private final String rawMessage;
    private boolean messageEnabled;
    private final List<String> commands;

    public CommandReward(String path) {
        CustomConfiguration config = UltraCosmeticsData.get().getPlugin().getConfig();
        MiniMessage mm = MessageManager.getMiniMessage();
        name = MessageManager.toLegacy(mm.deserialize(config.getString(path + ".Name")));
        stack = ItemFactory.getItemStackFromConfig(path + ".Material");
        chance = config.getInt(path + ".Chance");
        messageEnabled = config.getBoolean(path + ".Message.enabled");
        rawMessage = config.getString(path + ".Message.message");
        commands = config.getStringList(path + ".Commands");
    }

    /**
     * The weight this CommandReward has. Weights for
     * CommandRewards determine how likely it is to be picked
     * over a different command or a category of cosmetics.
     *
     * @return the weight this reward has
     */
    public int getChance() {
        return chance;
    }

    /**
     * @return true if the message from {@link #getMessage(Player)} will be sent
     */
    public boolean isMessageEnabled() {
        return messageEnabled;
    }

    /**
     * @param messageEnabled if true, the message from {@link #getMessage(Player)} will be sent
     * @see #isMessageEnabled()
     */
    public void setMessageEnabled(boolean messageEnabled) {
        this.messageEnabled = messageEnabled;
    }

    /**
     * Returns the message that should be sent when the
     * chest is opened. Will not be sent if {@link #isMessageEnabled()}
     * is false.
     *
     * @return the message to be sent, or null if none should be sent.
     */
    public Component getMessage(Player player) {
        if (rawMessage == null) {
            return null;
        }
        return MessageManager.getMiniMessage().deserialize(rawMessage,
                Placeholder.unparsed("name", player.getName())
        );
    }

    /**
     * Returns the commands that will be executed when
     * the chest is opened. Changing this list will
     * change the commands that are executed.
     *
     * @return the list of commands
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * @return the name of this CommandReward
     */
    public String getName() {
        return name;
    }

    /**
     * @return a copy of this CommandReward's display item
     */
    public ItemStack getItemStack() {
        return stack.clone();
    }
}
