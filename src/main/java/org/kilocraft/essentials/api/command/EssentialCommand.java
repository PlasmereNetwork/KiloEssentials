package org.kilocraft.essentials.api.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.config.messages.Messages;
import org.kilocraft.essentials.util.NameLookup;
import org.kilocraft.essentials.util.text.Texter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public abstract class EssentialCommand implements IEssentialCommand {
    protected static final transient Logger logger = LogManager.getLogger();
    private static final DynamicCommandExceptionType PROFILE_RESOLVE_EXCEPTION = new DynamicCommandExceptionType((obj) -> Texter.newText("Unexpected error while resolving the requested profile\n" + obj));
    private final String label;
    public Config config = KiloConfig.main();
    public Messages messages = KiloConfig.messages();
    protected transient String[] alias;
    protected transient LiteralArgumentBuilder<ServerCommandSource> argumentBuilder;
    protected transient LiteralCommandNode<ServerCommandSource> commandNode;
    protected transient Predicate<ServerCommandSource> PERMISSION_CHECK_ROOT;
    protected transient CommandPermission permission;
    protected transient int MIN_OP_LEVEL;
    private transient String descriptionId = null;
    private transient String[] usageArguments = null;
    private transient ForkType forkType;

    public EssentialCommand(final String label) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public EssentialCommand(final String label, final String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public EssentialCommand(final String label, final Predicate<ServerCommandSource> predicate) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public EssentialCommand(final String label, final Predicate<ServerCommandSource> predicate, final String[] alias) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.alias = alias;
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
    }

    public EssentialCommand(final String label, final CommandPermission permission) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, permission);
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
    }

    public EssentialCommand(final String label, final CommandPermission permission, final int minOpLevel) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, permission, minOpLevel);
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
        this.MIN_OP_LEVEL = minOpLevel;
    }

    public EssentialCommand(final String label, final CommandPermission permission, final String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, permission);
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
    }

    public EssentialCommand(final String label, final CommandPermission permission, final int minOpLevel, final String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, permission, minOpLevel);
        this.argumentBuilder = this.literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.permission = permission;
        this.MIN_OP_LEVEL = minOpLevel;
    }

    public static String tl(final String key) {
        return ModConstants.translation(key);
    }

    public KiloEssentials getEssentials() {
        return KiloEssentials.getInstance();
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String[] getAlias() {
        return this.alias;
    }

    public LiteralCommandNode<ServerCommandSource> getCommandNode() {
        return this.commandNode;
    }

    public LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilder() {
        return this.argumentBuilder;
    }

    public Predicate<ServerCommandSource> getRootPermissionPredicate() {
        return this.PERMISSION_CHECK_ROOT;
    }

    public final void withUsage(final String identifier, final String... arguments) {
        this.usageArguments = arguments.clone();
        this.descriptionId = identifier;
    }

    public final void withForkType(@Nullable final ForkType type) {
        this.forkType = type;
    }

    public final String[] getUsageArguments() {
        return this.usageArguments;
    }

    public final String getDescriptionId() {
        return this.descriptionId;
    }

    public final ForkType getForkType() {
        return this.forkType == null ? ForkType.DEFAULT : this.forkType;
    }

    public final boolean hasUsage() {
        return this.usageArguments != null || this.descriptionId != null;
    }

    public boolean hasPermission(final ServerCommandSource src, final CommandPermission cmdPerm) {
        return KiloCommands.hasPermission(src, cmdPerm);
    }

    public boolean hasPermission(final ServerCommandSource src, final CommandPermission cmdPerm, final int minOpLevel) {
        return KiloCommands.hasPermission(src, cmdPerm, minOpLevel);
    }

    public boolean hasPermission(final ServerCommandSource src, final EssentialPermission essPerm) {
        return KiloEssentials.hasPermissionNode(src, essPerm);
    }

    public boolean hasPermission(final ServerCommandSource src, final EssentialPermission essPerm, final int minOpLevel) {
        return KiloEssentials.hasPermissionNode(src, essPerm, minOpLevel);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> literal(final String label) {
        return CommandManager.literal(label);
    }

    @Override
    public <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(final String label, final ArgumentType<T> argumentType) {
        return CommandManager.argument(label, argumentType);
    }

    @Override
    public OnlineUser getOnlineUser(final String name) {
        return this.getServer().getOnlineUser(name);
    }

    public OnlineUser getOnlineUser(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return this.getOnlineUser(ctx.getSource().getPlayer());
    }

    public CommandSourceUser getCommandSource(final CommandContext<ServerCommandSource> ctx) {
        return this.getServer().getCommandSourceUser(ctx.getSource());
    }

    @Override
    public OnlineUser getOnlineUser(UUID uuid) {
        return this.getServer().getOnlineUser(uuid);
    }

    public Server getServer() {
        return KiloEssentials.getServer();
    }

    public OnlineUser getOnlineUser(final ServerPlayerEntity player) {
        return this.getServer().getOnlineUser(player);
    }

    public String getUserArgumentInput(final CommandContext<ServerCommandSource> ctx, final String label) {
        return getString(ctx, label);
    }

    public OnlineUser getOnlineUser(final CommandContext<ServerCommandSource> ctx, final String label) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(StringArgumentType.getString(ctx, label));

        if (user == null) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }

        return user;
    }

    public CompletableFuture<Optional<User>> getUser(final GameProfile profile) {
        return this.getServer().getUserManager().getOffline(profile);
    }

    public CompletableFuture<Optional<User>> getUser(final String name) {
        return this.getServer().getUserManager().getOffline(name);
    }

    public boolean isOnline(final User user) {
        return this.getServer().getUserManager().isOnline(user);
    }

    public boolean isOnline(final UUID uuid) {
        return this.getServer().getUserManager().getOnline(uuid) != null;
    }

    public boolean isOnline(final String name) {
        return this.getServer().getUserManager().getOnline(name) != null;
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> getUserArgument(final String label) {
        return this.argument(label, string()).suggests(ArgumentSuggestions::users);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> getOnlineUserArgument(final String label) {
        return this.argument(label, string()).suggests(ArgumentSuggestions::users);
    }

    public CompletableFuture<GameProfile> resolveAndGetProfileAsync(final CommandContext<ServerCommandSource> ctx, final String label) throws CommandSyntaxException {
        return CompletableFuture.completedFuture(resolveAndGetProfile(ctx, label));
    }

    public GameProfile resolveAndGetProfile(final CommandContext<ServerCommandSource> ctx, final String label) throws CommandSyntaxException {
        try {
            final String input = ctx.getArgument(label, String.class);
            Matcher idMatcher = StringUtils.UUID_PATTERN.matcher(input);
            if (idMatcher.matches()) {
                UUID uuid = UUID.fromString(input);
                if (this.isOnline(uuid)) {
                    return this.getOnlineUser(uuid).asPlayer().getGameProfile();
                }

                try {
                    String name = NameLookup.getPlayerName(input);
                    return new GameProfile(uuid, name);
                } catch (IOException e) {
                    throw PROFILE_RESOLVE_EXCEPTION.create(e.getMessage());
                }
            }

            if (this.isOnline(input)) {
                return this.getOnlineUser(input).asPlayer().getGameProfile();
            }

            Matcher nameMatcher = StringUtils.USERNAME_PATTERN.matcher(input);
            if (nameMatcher.matches()) {
                try {
                    String id = NameLookup.getPlayerUUID(input);
                    if (id == null) {
                        throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
                    }
                    UUID uuid = UUID.fromString(id);
                    return new GameProfile(uuid, id);
                } catch (IOException e) {
                    throw PROFILE_RESOLVE_EXCEPTION.create(e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
    }

    public int sendUsage(CommandContext<ServerCommandSource> ctx, String key, Object... objects) {
        this.getCommandSource(ctx).sendLangMessage(key, objects);
        return AWAIT;
    }

    public String tl(final String key, final Object... objects) {
        return ModConstants.translation(key, objects);
    }

}
