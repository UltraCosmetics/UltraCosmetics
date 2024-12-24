package be.isach.ultracosmetics.permissions;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.type.CosmeticType;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class LuckPermsHook implements CosmeticPermissionSetter, RawPermissionSetter {
    private final UltraCosmetics ultraCosmetics;
    private final LuckPerms api;
    private final ImmutableContextSet context;
    private boolean log = true;

    public LuckPermsHook(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        api = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
        String[] contexts = SettingsManager.getConfig().getString("TreasureChests.Permission-Add-Command").split(" ");
        ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
        for (int i = 1; i < contexts.length; i++) {
            if (contexts[i].equals("nolog")) {
                log = false;
                continue;
            }
            String[] kv = contexts[i].split("=");
            if (kv.length != 2) {
                ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Invalid LuckPerms context: " + contexts[i]);
                continue;
            }
            contextBuilder.add(kv[0], kv[1]);
        }
        context = contextBuilder.build();
    }

    @Override
    public void setRawPermission(Player player, String permission) {
        if (log) {
            ultraCosmetics.getSmartLogger().write("Setting permission '" + permission + "' for user " + player.getName());
        }

        ultraCosmetics.getScheduler().runAsync((task) -> {
            User user = api.getPlayerAdapter(Player.class).getUser(player);
            Node node = Node.builder(permission).value(true).context(context).build();
            user.data().add(node);
            api.getUserManager().saveUser(user);
        });
    }

    @Override
    public void setPermissions(Player player, Set<CosmeticType<?>> types) {
        types.forEach(t -> setRawPermission(player, t.getPermission().getName()));
    }

    @Override
    public void unsetPermissions(Player player, Set<CosmeticType<?>> types) {
        throw new UnsupportedOperationException("Cannot unset permission using LuckPerms API");
    }

    @Override
    public boolean isUnsetSupported() {
        return false;
    }
}
