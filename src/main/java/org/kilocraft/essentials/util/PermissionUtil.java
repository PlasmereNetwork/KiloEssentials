package org.kilocraft.essentials.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.track.UserTrackEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PermissionUtil {
    private static final Logger logger = (Logger) KiloEssentials.getLogger();
    private static final List<String> pendingPermissions = new ArrayList<>();
    public static String COMMAND_PERMISSION_PREFIX = "kiloessentials.command.";
    public static String PERMISSION_PREFIX = "kiloessentials.";
    private boolean present;
    private Manager manager;

    public PermissionUtil() {
        this.manager = Manager.fromString(KiloConfig.main().permissionManager());

        if (manager == Manager.VANILLA || manager == Manager.NONE) {
            logger.info("Using vanilla permission system");
            this.present = false;
            return;
        }

        logger.info("Checking {} for Availability", manager.getName());

        this.present = this.checkPresent();

        if (!this.present) {
            logger.warn("**** {} is not present! Switching to vanilla operator system", manager.getName());
            logger.warn("     You need to install LuckPerms for Fabric to manage the permissions");
            this.manager = Manager.NONE;
            return;
        }

        if (this.manager == Manager.LUCKPERMS) {
            LuckPermsProvider.get().getEventBus().subscribe(UserTrackEvent.class, event -> {
                OnlineUser user = KiloEssentials.getServer().getOnlineUser(event.getUser().getUniqueId());
                if (user != null) {
                    KiloEssentials.getServer().getPlayerManager().sendCommandTree(user.asPlayer());
                }
            });
        }

        logger.info("Using {} as the Permission Manager", manager.getName());

        logger.info("Registered " + (CommandPermission.values().length + EssentialPermission.values().length) + " permission nodes.");
    }

    public boolean hasPermission(ServerCommandSource src, String permission, int opLevel) {
        if (this.present) {
            if (manager == Manager.LUCKPERMS) {
                KiloServer.getLogger().info("Checking permission " + permission + "(result: " + fromLuckPerms(src, permission, opLevel) + ")");
                return fromLuckPerms(src, permission, opLevel);
            }
        }

        return src.hasPermissionLevel(opLevel);
    }

    public static void registerNode(final String node) {
        pendingPermissions.add(node);
    }

    private boolean fromLuckPerms(ServerCommandSource src, String perm, int op) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        try {
            ServerPlayerEntity player = src.getPlayer();
            User user = luckPerms.getUserManager().getUser(player.getUuid());

            if (user != null) {
                QueryOptions options = luckPerms.getContextManager().getQueryOptions(player);
                return user.getCachedData().getPermissionData(options).checkPermission(perm).asBoolean();
            }

        } catch (CommandSyntaxException ignored) {
        }

        return src.hasPermissionLevel(op);
    }

    private boolean checkPresent() {
        if (manager == Manager.NONE) {
            return false;
        }

        try {
            if (manager == Manager.LUCKPERMS) {
                try {
                    LuckPermsProvider.get();
                    return true;
                } catch (Throwable ignored) {
                }
            }

            return FabricLoader.getInstance().getModContainer(manager.getName().toLowerCase(Locale.ROOT)).isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean managerPresent() {
        return this.present;
    }

    public Manager getManager() {
        return this.manager;
    }

    public enum Manager {
        NONE("none"),
        VANILLA("Vanilla"),
        LUCKPERMS("LuckPerms");

        private final String name;

        Manager(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @NotNull
        public static Manager fromString(@NotNull final String str) {
            for (Manager value : Manager.values()) {
                if (value.name.equalsIgnoreCase(str)) {
                    return value;
                }
            }

            return Manager.NONE;
        }
    }

    @Override
    public String toString() {
        return this.manager.name;
    }

}
