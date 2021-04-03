package org.kilocraft.essentials.extensions.warps.serverwidewarps;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.ScheduledExecutionThread;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;

import java.util.Locale;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WarpCommand {
    private static final SimpleCommandExceptionType WARP_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the warp specified!"));
    private static final SimpleCommandExceptionType NO_WARPS = new SimpleCommandExceptionType(new LiteralText("There are no Warps set!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("warp")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.WARP));
        RequiredArgumentBuilder<ServerCommandSource, String> warpArg = argument("warp", word());
        LiteralArgumentBuilder<ServerCommandSource> listLiteral = literal("warps");

        warpArg.executes(c -> executeTeleport(c.getSource(), getString(c, "warp")));
        listLiteral.executes(c -> executeList(c.getSource()));

        warpArg.suggests(ServerWarpManager::suggestions);

        builder.then(warpArg);
        registerAdmin(builder, dispatcher);
        dispatcher.register(listLiteral);
        dispatcher.register(builder);

        registerAliases();
    }

    public static void registerAliases() {
        for (ServerWarp warp : ServerWarpManager.getWarps()) {
            if (warp.addCommand()) {
                SimpleCommandManager.register(
                        new SimpleCommand(
                                "server_warp:" + warp.getName().toLowerCase(Locale.ROOT),
                                warp.getName().toLowerCase(Locale.ROOT),
                                (source, args, server) -> executeTeleport(source, warp.getName())
                        ).withoutArgs()
                );
            }
        }
    }

    private static void registerAdmin(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> aliasAdd = literal("setwarp");
        LiteralArgumentBuilder<ServerCommandSource> aliasRemove = literal("delwarp");
        RequiredArgumentBuilder<ServerCommandSource, String> removeArg = argument("warp", word());
        RequiredArgumentBuilder<ServerCommandSource, String> addArg = argument("name", word());

        aliasAdd.requires(s -> KiloCommands.hasPermission(s, CommandPermission.SETWARP));
        aliasRemove.requires(s -> KiloCommands.hasPermission(s, CommandPermission.DELWARP));

        removeArg.executes(c -> executeRemove(c.getSource(), getString(c, "warp")));
        addArg.then(argument("registerCommand", bool())
                .executes(c -> executeAdd(c.getSource(), getString(c, "name"), getBool(c, "registerCommand"))));

        removeArg.suggests(ServerWarpManager::suggestions);

        aliasAdd.then(addArg);
        aliasRemove.then(removeArg);

        dispatcher.register(aliasAdd);
        dispatcher.register(aliasRemove);
    }

    private static int executeTeleport(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (!ServerWarpManager.getWarpsByName().contains(name)) {
            throw WARP_NOT_FOUND_EXCEPTION.create();
        }
        source.getPlayer();
        ServerWarp warp = ServerWarpManager.getWarp(name);
        OnlineUser user = KiloServer.getServer().getOnlineUser(source.getPlayer());
        //TODO: Set a home for people who warp and don't have a home yet
/*        if (UserHomeHandler.isEnabled() && user.getHomesHandler().getHomes().isEmpty()) {
            Home home = new Home();
            user.getHomesHandler().addHome();
        }*/
        ScheduledExecutionThread.teleport(user, null, () -> {
            if (user.isOnline()) {
                user.sendLangMessage("command.warp.teleport", warp.getName());
                user.saveLocation();
                try {
                    ServerWarpManager.teleport(user.getCommandSource(), warp);
                } catch (CommandSyntaxException ignored) {
                    //We already have a check, which checks if the executor is a player
                }
            }
        });
        return 1;
    }

    private static int executeList(ServerCommandSource source) throws CommandSyntaxException {
        int warpsSize = ServerWarpManager.getWarps().size();

        if (warpsSize == 0)
            throw NO_WARPS.create();

        MutableText text = new LiteralText("Warps").formatted(Formatting.GOLD)
                .append(new LiteralText(" [ ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(String.valueOf(warpsSize)).formatted(Formatting.LIGHT_PURPLE))
                .append(new LiteralText(" ]: ").formatted(Formatting.DARK_GRAY));

        int i = 0;
        boolean nextColor = false;
        for (ServerWarp warp : ServerWarpManager.getWarps()) {
            LiteralText thisWarp = new LiteralText("");
            i++;

            Formatting thisFormat = nextColor ? Formatting.WHITE : Formatting.GRAY;

            thisWarp.append(new LiteralText(warp.getName()).styled((style) -> style.withFormatting(thisFormat).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new LiteralText("[i] ").formatted(Formatting.YELLOW)
                            .append(new LiteralText("Click to teleport!").formatted(Formatting.GREEN)))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/warp " + warp.getName()))));

            if (warpsSize != i)
                thisWarp.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

            nextColor = !nextColor;

            text.append(thisWarp);
        }
        KiloServer.getServer().getCommandSourceUser(source).sendMessage(text);
        return 1;
    }

    private static int executeAdd(ServerCommandSource source, String name, boolean addCommand) throws CommandSyntaxException {
        ServerWarpManager.addWarp(new ServerWarp(name, Vec3dLocation.of(source.getPlayer()).shortDecimals(), addCommand));
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(source);
        user.sendLangMessage("command.warp.set", name);
        registerAliases();
        KiloCommands.updateGlobalCommandTree();
        return 1;
    }

    private static int executeRemove(ServerCommandSource source, String warp) throws CommandSyntaxException {
        ServerWarp w = ServerWarpManager.getWarp(warp);
        CommandSourceUser user = KiloServer.getServer().getCommandSourceUser(source);
        if (w != null) {
            ServerWarpManager.removeWarp(w);
            user.sendLangMessage("command.warp.remove", warp);
        }
        else
            throw WARP_NOT_FOUND_EXCEPTION.create();

        return 1;
    }

}
