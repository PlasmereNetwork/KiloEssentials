/*
 * MIT License
 *
 * Copyright (c) 2019 KiloCraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.kilocraft.essentials.api;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.feature.ConfigurableFeatures;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.LuckPermsCompatibility;
import org.kilocraft.essentials.util.PermissionUtil;
import org.kilocraft.essentials.util.StartupScript;
import org.kilocraft.essentials.util.messages.MessageUtil;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface KiloEssentials {
    static KiloEssentials getInstance() {
        return KiloEssentialsImpl.getInstance();
    }

    static Logger getLogger() {
        return KiloEssentialsImpl.getLogger();
    }

    static Server getServer() {
        return KiloServer.getServer();
    }

    static boolean hasPermissionNode(ServerCommandSource source, EssentialPermission perm) {
        return KiloEssentialsImpl.hasPermissionNode(source, perm);
    }

    static boolean hasPermissionNode(ServerCommandSource source, EssentialPermission perm, int minOpLevel) {
        return KiloEssentialsImpl.hasPermissionNode(source, perm, minOpLevel);
    }

    MessageUtil getMessageUtil();

    KiloCommands getCommandHandler();

    StartupScript getStartupScript();

    CompletableFuture<List<User>> getAllUsersThenAcceptAsync(OnlineUser requester, String loadingTitle, Consumer<? super List<User>> action);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(ServerCommandSource requester, String username, Consumer<? super User> action);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(ServerPlayerEntity requester, String username, Consumer<? super User> action);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(OnlineUser requester, String username, Consumer<? super User> action);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(OnlineUser requester, UUID uuid, Consumer<? super User> action);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(String username, Consumer<? super Optional<User>> action);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(UUID uuid, Consumer<? super Optional<User>> action);

    Optional<User> getUserThenAccept(UUID uuid, Consumer<? super Optional<User>> action);

    Optional<User> getUser(UUID uuid);

    CompletableFuture<Optional<User>> getUserThenAcceptAsync(String username, Consumer<? super Optional<User>> action, Executor executor);

    PermissionUtil getPermissionUtil();

    ConfigurableFeatures getFeatures();

    static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    static Path getDataDirPath() {
        return getEssentialsPath().resolve("data");
    }

    static Path getEssentialsPath() {
        return new File(KiloEssentials.getWorkingDirectory()).toPath().resolve("essentials");
    }

    static Path getServerProperties() {
        return new File(KiloEssentials.getWorkingDirectory()).toPath().resolve("server.properties");
    }

    Optional<LuckPermsCompatibility> getLuckPermsCompatibility();

    ServerSettings getSettingManager();
}
