package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.ServerImpl;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUserManager;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class UpdateGroupsCommand extends EssentialCommand {
    public UpdateGroupsCommand() {
        super("updategroups", CommandPermission.BROADCAST);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser sender = this.getCommandSource(ctx);
        try {
            boolean done = false;
            for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
                boolean thing = ((ServerUserManager) KiloServer.getServer().getUserManager()).updateGroup(playerEntity);

                if (thing) {
                    done = true;
                }
            }

            if (done) {
                sender.sendMessage(new LiteralText("Updated groups!").formatted(Formatting.YELLOW));
            } else {
                sender.sendMessage(new LiteralText("No groups to update!").formatted(Formatting.RED));
            }

            return SUCCESS;
        } catch (Exception e){
            KiloServer.getLogger().error("Could execute update groups command...", e);
            sender.sendMessage(new LiteralText("Could not update groups!").formatted(Formatting.RED));
        }

        return FAILED;
    }
}