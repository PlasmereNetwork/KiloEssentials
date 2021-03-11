package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.util.text.Texter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemNameCommand {
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_NAME);

    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("name")
                .requires(PERMISSION_CHECK).build();
        RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", greedyString())
                .suggests(ItemNameCommand::itemNameSuggestions)
                .executes(ItemNameCommand::execute);

        rootCommand.addChild(nameArgument.build());
        builder.then(rootCommand);
        dispatcher.register(literal("rename").requires(PERMISSION_CHECK).redirect(rootCommand));
    }

    private static CompletableFuture<Suggestions> itemNameSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ItemStack item = context.getSource().getPlayer().getMainHandStack();
        List<String> list = new ArrayList<String>() {{
            add("reset");
        }};
        if (!item.isEmpty()) {
            list.add(TextFormat.reverseTranslate(item.getName().getString(), '&'));
        }
        return CommandSource.suggestMatching(list, builder);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String inputString = getString(ctx, "name");
        ItemStack item = player.getMainHandStack();
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(ctx.getSource());

        if (inputString.length() >= 90) {
            user.sendLangMessage( "command.item.too_long");
            return 0;
        }

        if (item.isEmpty()) {
            user.sendLangMessage( "general.no_item");
            return 0;
        }

        if (player.experienceLevel < 1 && !player.isCreative()) {
			user.sendLangMessage( "command.item.no_exp");
        	return 0;
		}

		player.addExperienceLevels(-1);

        if (inputString.equalsIgnoreCase("reset")) {
            item.removeCustomName();
            user.sendLangMessage( "command.item.reset", "name", Texter.Legacy.toFormattedString(item.getName()));

            return 1;
        }

        String nameToSet = KiloCommands.hasPermission(ctx.getSource(), CommandPermission.ITEM_FORMATTING) ? inputString : ComponentText.clearFormatting(inputString);
        user.sendLangMessage( "command.item.set", "name", nameToSet);
        item.setCustomName(ComponentText.toText(nameToSet));

        return 1;
    }

}
