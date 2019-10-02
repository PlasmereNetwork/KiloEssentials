package org.kilocraft.essentials.craft.commands.servermanagement;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextColor;

public class StopCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("ke_stop")
                .then(CommandManager.argument("args", StringArgumentType.greedyString())
                    .executes(c -> execute(c, StringArgumentType.getString(c, "args"))))
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.server.stop", 2))
                .executes(c -> execute(c, ""));

        dispatcher.register(builder);
    }

    private static int execute(CommandContext<ServerCommandSource> context, String s) {
        boolean isConfirmed = false;
        if (s.startsWith("-confirmed")) isConfirmed = true;

        if (isConfirmed) {
            TextColor.sendToUniversalSource(context.getSource(), "&cStopping the server...", false);
            String reason = s.replaceFirst("-confirmed", "");
            if (reason.isEmpty()) reason = "&cServer closed by an operator!";
            KiloServer.getServer().stop(false, TextColor.translateAlternateColorCodes('&', reason));

        } else {
            TextColor.sendToUniversalSource(context.getSource(), "&cPlease confirm your action by doing:\n &8\"&7/stop -confirmed <optional: reason>&8\"", false);
        }

        return 1;
    }

}
