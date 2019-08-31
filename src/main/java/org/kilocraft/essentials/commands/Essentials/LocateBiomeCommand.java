package org.kilocraft.essentials.commands.Essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.provider.LocateBiomeProvider;

import java.util.Objects;

public class LocateBiomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("locate")
                .then(CommandManager.literal("- Biome"));

        Registry.BIOME.stream().forEach(biome -> {
            literalArgumentBuilder.then(CommandManager.literal(Objects.requireNonNull(Registry.BIOME.getId(biome)).toString()).executes(context -> {
                LocateBiomeProvider.executeByCommand(context.getSource(), biome);
                return 1;
            }));
        });

        dispatcher.register(literalArgumentBuilder);
    }
}