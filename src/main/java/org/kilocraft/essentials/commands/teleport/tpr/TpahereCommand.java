package org.kilocraft.essentials.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Texts;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.Texter;
import org.kilocraft.essentials.util.UserUtils;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public class TpahereCommand extends EssentialCommand {
    public TpahereCommand() {
        super("tpahere", CommandPermission.TELEPORTREQUEST, new String[]{"tprhere"});
        this.withUsage("command.tpa.usage", "target");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument = this.getOnlineUserArgument("target")
                .executes(this::request);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int request(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "target");

        if (src.equals(target)) {
            throw KiloCommands.getException(ExceptionMessageNode.SOURCE_IS_TARGET).create();
        }

        if (target.ignored(src.getUuid()) || target.getSetting(Settings.DON_NOT_DISTURB)) {
            throw KiloCommands.getException(ExceptionMessageNode.IGNORED, target.getFormattedDisplayName()).create();
        }

        if (UserUtils.TpaRequests.hasRequest(src, target)) {
            return src.sendLangError("command.tpa.already_requested", target.getFormattedDisplayName());
        }

        UserUtils.TpaRequests.add(src, target, true);

        src.sendMessage(
                Texter.toText(ModConstants.translation("command.tpa.sent", target.getFormattedDisplayName()))
                        .append(" ")
                        .append(Texts.bracketed(Texter.getButton(" &c" + '\u00d7' + "&r ", "/tpcancel " + target.getUsername(), Texter.toText("&cCancel"))))
        );

        target.sendMessage(
                Texter.toText(ModConstants.translation("command.tpa.receive.here", src.getFormattedDisplayName()))
                        .append(" ")
                        .append(Texts.bracketed(Texter.getButton(" &a" + '\u2714' + "&r ", "/tpaccept " + src.getUsername(), Texter.toText("&aClick to accept"))))
                        .append(" ")
                        .append(Texts.bracketed(Texter.getButton(" &c" + '\u00d7' + "&r ", "/tpdeny " + src.getUsername(), Texter.toText("&cClick to deny"))))
        );

        if (target.getSetting(Settings.SOUNDS)) {
            ServerChat.pingPlayer(target.asPlayer(), ServerChat.PingType.PRIVATE);
        }
        return SINGLE_SUCCESS;
    }

}