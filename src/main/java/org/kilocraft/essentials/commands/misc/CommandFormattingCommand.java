package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.text.Texter;

public class CommandFormattingCommand extends EssentialCommand {
    private static final MutableText text = Texter.InfoBlockStyle.of(tl("command.commandformatting.title"))
            .newLine().append(tl("command.commandformatting.info")).newLine()
            .build();

    public CommandFormattingCommand() {
        super("commandformatting", CommandPermission.SUDO_OTHERS);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> ctx) {
        getCommandSource(ctx).sendMessage(text);
        return SUCCESS;
    }
}
