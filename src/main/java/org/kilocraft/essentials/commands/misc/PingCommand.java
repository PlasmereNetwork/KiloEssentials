package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.commands.CommandUtils;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class PingCommand extends EssentialCommand {
    public PingCommand() {
        super("ping", CommandPermission.PING_SELF, new String[]{"latency"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("player", player())
                .requires(src -> hasPermission(src, CommandPermission.PING_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> execute(ctx, getPlayer(ctx, "player")));

        argumentBuilder.executes(ctx -> execute(ctx, ctx.getSource().getPlayer()));
        commandNode.addChild(selectorArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) {
        CommandSourceUser user = getCommandSource(ctx);
        if (CommandUtils.areTheSame(ctx.getSource(), target)) {
            user.sendLangMessage("command.ping.self", ComponentText.formatPing(target.pingMilliseconds));
            return 1;
        }

        user.sendLangMessage("command.ping.others", target.getName().asString(),
                ComponentText.formatPing(target.pingMilliseconds));

        return target.pingMilliseconds;
    }

}
