package org.kilocraft.essentials.config;

import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.math.DataTracker;
import org.kilocraft.essentials.util.monitor.SystemMonitor;
import org.kilocraft.essentials.util.text.Texter;

public class ConfigVariableFactory {
    private static final Server server = KiloEssentials.getServer();

    public static String replaceOnlineUserVariables(String str, @NotNull final OnlineUser user) {
        Validate.notNull(user, "User most not be null!");
        String string = replaceUserVariables(str, user);
        return new ConfigObjectReplacerUtil("user", string)
                .append("rankedName", user.getRankedDisplayNameAsString())
                .append("ranked_displayName", Texter.Legacy.toFormattedString(user.getRankedDisplayName()))
                .append("ping", user.asPlayer().pingMilliseconds)
                .append("formatted_ping", ComponentText.formatPing(user.asPlayer().pingMilliseconds))
                .toString();
    }

    public static String replaceTargetUserVariables(String str, @NotNull final User user) {
        Validate.notNull(user, "User most not be null!");
        return new ConfigObjectReplacerUtil("target", str)
                .append("rankedName", user.getRankedDisplayNameAsString())
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .toString();
    }

    public static String replaceUserVariables(String str, @NotNull final User user) {
        Validate.notNull(user, "User most not be null!");
        return new ConfigObjectReplacerUtil("user", str)
                .append("rankedName", user.getRankedDisplayNameAsString())
                .append("displayName", user.getFormattedDisplayName())
                .append("name", user.getUsername())
                .append("tag", user.getNameTag())
                .toString();
    }

    public static String replacePlayerVariables(String str, @NotNull final ServerPlayerEntity player) {
        Validate.notNull(player, "Player most not be null!");
        return new ConfigObjectReplacerUtil("player", str)
                .append("ping", player.pingMilliseconds)
                .append("formatted_ping", ComponentText.formatPing(player.pingMilliseconds))
                .toString();
    }

    public static String replaceServerVariables(String str) {
        Validate.notNull(str, "String must not be null!");
        final double memUsagePercent = SystemMonitor.getRamUsedPercentage();
        return new ConfigObjectReplacerUtil("server", str)
                .append("tps", DataTracker.tps.getAverage(100))
                .append("formatted_tps", ComponentText.formatTps(DataTracker.tps.getAverage(100)))
                .append("tps5", DataTracker.tps.getAverage(6000))
                .append("formatted_tps5", ComponentText.formatTps(DataTracker.tps.getAverage(6000)))
                .append("tps15", DataTracker.tps.getAverage(18000))
                .append("formatted_tps15", ComponentText.formatTps(DataTracker.tps.getAverage(18000)))
                .append("player_count", server.getPlayerManager().getCurrentPlayerCount())
                .append("max_players", server.getPlayerManager().getMaxPlayerCount())
                .append("name", KiloConfig.main().server().name)
                .append("memory_max", String.valueOf(SystemMonitor.getRamMaxMB()))
                .append("memory_usage_percentage", memUsagePercent)
                .append("formatted_memory_usage_percentage", ComponentText.formatPercentage(memUsagePercent))
                .toString();
    }

}

