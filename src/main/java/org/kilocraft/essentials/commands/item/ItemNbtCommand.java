package org.kilocraft.essentials.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemNbtCommand {
    private static Predicate<ServerCommandSource> PERMISSION_CHECK = src -> KiloCommands.hasPermission(src, CommandPermission.ITEM_COMMANDS, 4);

    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("command")
                .requires(PERMISSION_CHECK)
                .build();

        LiteralArgumentBuilder<ServerCommandSource> resetArgument = literal("reset")
                .executes(ItemNbtCommand::executeReset);

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
        RequiredArgumentBuilder<ServerCommandSource, Integer> removeLineArgument = argument("line", integer(1, 10))
                .suggests(TabCompletions::noSuggestions)
                .executes(ItemNbtCommand::executeRemove);

        LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");

        RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 10))
                .suggests(TabCompletions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> textArgument = argument("command", greedyString())
                .suggests(ItemNbtCommand::commandSuggestions)
                .executes(ItemNbtCommand::execute);

        removeArgument.then(removeLineArgument);
        lineArgument.then(textArgument.build());
        setArgument.then(lineArgument.build());
        rootCommand.addChild(removeArgument.build());
        rootCommand.addChild(resetArgument.build());
        rootCommand.addChild(setArgument.build());
        builder.then(rootCommand);
    }

    private static CompletableFuture<Suggestions> commandSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        ItemStack item = context.getSource().getPlayer().getMainHandStack();

        if (item.isEmpty() || !item.hasTag() || item.getTag() == null ||
                !item.getTag().contains("display") || !item.getTag().contains("NBTCommands"))
            return TabCompletions.noSuggestions(context, builder);

        int inputLine = 0;

        char[] chars = context.getInput().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            try	{
                inputLine = Integer.parseInt(String.valueOf(chars[i])) - 1;
                if (chars[i++] == 0) inputLine = chars[i] + '0';
            } catch (NumberFormatException ignored) { }
        }

        ListTag lore = item.getTag().getList("NBTCommands", 8);
        String[] strings = {lore.getString(inputLine)};
        return CommandSource.suggestMatching(strings, builder);
    }

    private static int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int inputLine = getInteger(ctx, "line") - 1;
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack item = player.getMainHandStack();

        if (item.isEmpty()) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.invalid_item");
            return -1;
        }

        if (!item.hasTag() || item.getTag() == null || !item.getTag().contains("display") || !item.getTag().getCompound("display").contains("Lore")) {
            KiloChat.sendLangMessageTo(player, "command.item.nothing_to_reset");
            return -1;
        }

        ListTag lore = item.getTag().getCompound("display").getList("Lore", 8);

        if (inputLine >= lore.size()) {
            KiloChat.sendLangMessageTo(player, "command.item.nothing_to_reset");
            return -1;
        }

        lore.remove(inputLine);

        KiloChat.sendLangMessageTo(player, "command.item.lore.remove", inputLine + 1);
        return 1;
    }

    private static int executeReset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ItemStack item = ctx.getSource().getPlayer().getMainHandStack();

        if (item.isEmpty()) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.invalid_item");
            return -1;
        }

        if (item.getTag() == null) {
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.nothing_to_reset");
            return -1;
        }

        Objects.requireNonNull(item.getTag()).getCompound("display").remove("Lore");
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.item.lore.reset");
        return 1;
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String inputString = getString(ctx, "command").replaceFirst("/", "");
        ItemStack item = player.getMainHandStack();

        if (inputString.length() >= 90) {
            KiloChat.sendLangMessageTo(player, "command.item.too_long");
            return -1;
        }

        if (item.isEmpty()) {
            KiloChat.sendLangMessageTo(player, "command.item.invalid_item");
            return -1;
        }

        CompoundTag itemTag = item.getTag();

        if (!item.hasTag()) {
            itemTag = new CompoundTag();
        }

        if (!itemTag.contains("NBTCommands")) {
            itemTag.put("NBTCommands", new CompoundTag());
        }

        ListTag command = itemTag.getList("NBTCommands", 8);
        int inputLine = getInteger(ctx, "line") - 1;

        if (command == null) {
            command = new ListTag();
        }

        command.set(inputLine, StringTag.of(inputString));
        itemTag.put("NBTCommands", command);
        item.setTag(itemTag);

        KiloChat.sendLangMessageTo(player, "command.item.set", "command", inputLine + 1, "&e:\n &7" + inputLine);
        return 1;
    }

}
