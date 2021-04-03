package org.kilocraft.essentials.extensions.warps.serverwidewarps;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ServerWarpManager implements ConfigurableFeature, NBTStorage {
    private static ArrayList<String> byName = new ArrayList<>();
    private static List<ServerWarp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        NBTStorageUtil.addCallback(this);
        WarpCommand.register(KiloCommands.getDispatcher());
        return true;
    }

    public static void load() {
        WarpCommand.registerAliases();
    }

    public static List<ServerWarp> getWarps() { // TODO Move all access to Feature Types in future.
        return warps;
    }

    public static ArrayList<String> getWarpsByName() {
        return byName;
    }

    public static void addWarp(ServerWarp warp) {
        warps.add(warp);
        byName.add(warp.getName());
    }

    public static void removeWarp(ServerWarp warp) {
        warps.remove(warp);
        byName.remove(warp.getName());

        if (warp.addCommand()) {
            SimpleCommandManager.unregister("server_warp:" + warp.getName().toLowerCase(Locale.ROOT));
            KiloCommands.updateGlobalCommandTree();
        }
    }

    public static void removeWarp(String name) {
        ServerWarp warp = getWarp(name);

        if (warp != null) {
            removeWarp(warp);
        }
    }

    @Nullable
    public static ServerWarp getWarp(String warp) {
        for (ServerWarp w : warps) {
            if (w.getName().toLowerCase(Locale.ROOT).equals(warp.toLowerCase(Locale.ROOT))) {
                return w;
            }
        }

        return null;
    }

    public static int teleport(ServerCommandSource source, ServerWarp warp) throws CommandSyntaxException {
        ServerWorld world = RegistryUtils.toServerWorld(warp.getLocation().getDimensionType());
        source.getPlayer().teleport(world, warp.getLocation().getX(), warp.getLocation().getY(), warp.getLocation().getZ(),
                warp.getLocation().getRotation().getYaw(), warp.getLocation().getRotation().getPitch());

        return 1;
    }

    public static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(warps.stream().map(ServerWarp::getName), builder);
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("warps.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public NbtCompound serialize() {
        NbtCompound tag = new NbtCompound();
        for (ServerWarp warp : warps) {
            tag.put(warp.getName(), warp.toTag());
        }

        return tag;
    }

    @Override
    public void deserialize(@NotNull NbtCompound NbtCompound) {
        warps.clear();
        byName.clear();
        NbtCompound.getKeys().forEach((key) -> {
            warps.add(new ServerWarp(key, NbtCompound.getCompound(key)));
            byName.add(key);
        });
    }
}
