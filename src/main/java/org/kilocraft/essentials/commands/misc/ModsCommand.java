package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.text.Texter;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ModsCommand extends EssentialCommand {
    public ModsCommand() {
        super("mods", new String[]{"fabric"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> modArgument = argument("modid", word())
                .suggests(this::suggestMods)
                .executes(this::sendInfo);

        commandNode.addChild(modArgument.build());
        argumentBuilder.executes(this::sendList);
    }

    private int sendList(CommandContext<ServerCommandSource> ctx) {
        Texter.ListStyle text = Texter.ListStyle.of("Mods", Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY);

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();

            text.append(
                    Texter.Events.onHover(tl("general.click_info")),
                    Texter.Events.onClickRun("/mods " + meta.getId()),
                    meta.getName()
            );
        }
        getCommandSource(ctx).sendMessage(text.build());
        return SUCCESS;
    }

    private int sendInfo(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String inputId = getString(ctx, "modid");

        if (!FabricLoader.getInstance().getModContainer(inputId).isPresent()) {
            throw MOD_NOT_PRESENT.create();
        }

        ModMetadata meta = FabricLoader.getInstance().getModContainer(inputId).get().getMetadata();

        Texter.InfoBlockStyle text = Texter.InfoBlockStyle.of(meta.getName());
        text.append("Version", meta.getVersion().getFriendlyString());
        text.append("Authors", authorsToArrayText(meta));
        text.append("Description", meta.getDescription());

        getCommandSource(ctx).sendMessage(text.build());
        return SUCCESS;
    }

    private MutableText authorsToArrayText(ModMetadata meta) {
        Texter.ArrayStyle text = new Texter.ArrayStyle();
        for (Person author : meta.getAuthors()) {
            MutableText mutable = Texter.newText(author.getName());
            mutable.styled((style) -> {
                style.withHoverEvent(Texter.Events.onHover(tl("general.click_info")));
                style.withClickEvent(Texter.Events.onClickRun("mods", meta.getId(), author.getName()));
                return style;
            });
            text.append(mutable);
        }

        return text.build();
    }


    private CompletableFuture<Suggestions> suggestMods(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(FabricLoader.getInstance().getAllMods().stream()
                .map(mod -> mod.getMetadata().getId()), builder);
    }

    private final SimpleCommandExceptionType MOD_NOT_PRESENT = new SimpleCommandExceptionType(new LiteralText("Can't find a mod with that name/id!"));
}
