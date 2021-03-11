package org.kilocraft.essentials.api.user;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.user.punishment.PunishmentEntry;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.util.MutedPlayerList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserManager {

    /**
     * Returns a list of all the Users
     * @return a List of all the Users that joined the server at least once
     */
    CompletableFuture<List<User>> getAll();

    /**
     * Returns a future which contains a user who is offline. If the user is online, the future will return immediately.
     * @param username The UUID of the user.
     * @return The loaded offline user, otherwise a dummy user if the player has never joined.
     */
    CompletableFuture<Optional<User>> getOffline(String username);

    /**
     * Returns a future which contains a user who is offline. If the user is online, the future will return immediately.
     * @param uuid The UUID of the user.
     * @param username The name of the user
     * @return The loaded offline user, otherwise a dummy user if the player has never joined.
     */
    CompletableFuture<Optional<User>> getOffline(UUID uuid, String username);

    /**
     * Returns a future which contains a user who is offline. If the user is online, the future will return immediately.
     * @param uuid The UUID of the user.
     * @return The loaded offline user, otherwise a dummy user if the player has never joined.
     */
    CompletableFuture<Optional<User>> getOffline(UUID uuid);

    /**
     * Returns a future which contains a user who is offline. If the user is online, the future will return immediately.
     * @param profile The user's GameProfile
     * @return The loaded offline user, otherwise a dummy user if the player has never joined.
     * @throws IllegalArgumentException If the gameprofile is incomplete and no UUID is present.
     */
    CompletableFuture<Optional<User>> getOffline(GameProfile profile);

    /**
     * Gets a map of the online server users
     * @return An Map of online users, with their UUID
     */
    Map<UUID, OnlineServerUser> getOnlineUsers();

    /**
     * Gets a list of the online users
     * @return An List of online users
     */
    List<OnlineUser> getOnlineUsersAsList();

    /**
     * Gets a user who is online based on their GameProfile.
     * @param profile The user's GameProfile.
     * @return An online user, or null if the user isn't online.
     */
    @Nullable
    OnlineUser getOnline(GameProfile profile);

    /**
     * Gets a user who is online based on their UUID.
     * @param uuid The user's UUID.
     * @return An online user, or null if the user isn't online.
     */
    @Nullable
    OnlineUser getOnline(UUID uuid);

    /**
     * Gets a user who is online based on their username.
     * @param username The user's username.
     * @return An online user, or null if the user isn't online.
     */
    @Nullable
    OnlineUser getOnline(String username);

    /**
     * Gets a user who is online based on their nickname.
     * @param nickname The user's GameProfile.
     * @return An online user, or null if no user has the inputted nickname.
     */
    @Nullable
    OnlineUser getOnlineNickname(String nickname);

    /**
     * Gets a user who is online based on their PlayerEntity.
     * @param player the PlayerEntity.
     * @return An online user.
     */
    OnlineUser getOnline(ServerPlayerEntity player);

    /**
     * Gets a user who is online using a server command source.
     * @param source The ServerCommandSource to get the user from.
     * @return An online user.
     * @throws CommandSyntaxException If the CommandSource is not a player.
     */
    OnlineUser getOnline(ServerCommandSource source) throws CommandSyntaxException;

    /**
     * Checks if a user is currently online
     * @param user to check
     * @return is user online
     */
    boolean isOnline(User user);

    void saveAllUsers();

    void onChangeNickname(User user, String oldNick);

    PunishmentManager getPunishmentManager();

    MutedPlayerList getMutedPlayerList();

    void onPunishmentPerformed(OnlineUser src, PunishmentEntry entry, Punishment.Type type, @Nullable String expiry, boolean silent);

    void onPunishmentRevoked(OnlineUser src, PunishmentEntry entry, Punishment.Type type, @Nullable String expiry, boolean silent);
}
