package org.kilocraft.essentials.user;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.extensions.homes.commands.DelhomeCommand;
import org.kilocraft.essentials.extensions.homes.commands.HomeCommand;
import org.kilocraft.essentials.extensions.homes.commands.HomesCommand;
import org.kilocraft.essentials.extensions.homes.commands.SethomeCommand;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author CODY_AI
 * A better way of handeling the User (Instance of player) Homes
 *
 * @see ServerUser
 * @see ServerUserManager
 */

public class UserHomeHandler implements ConfigurableFeature {
    private static boolean isEnabled = false;
    private static List<Home> loadedHomes = new ArrayList<>();
    private List<Home> userHomes;
    private ServerUser serverUser;

    @Override
    public boolean register() {
        isEnabled = true;

        List<EssentialCommand> commands = new ArrayList<EssentialCommand>(){{
            add(new HomeCommand());
            add(new HomesCommand());
            add(new SethomeCommand());
            add(new DelhomeCommand());
        }};

        for (final EssentialCommand command : commands) {
            KiloCommands.getInstance().register(command);
        }

        return true;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public UserHomeHandler() {
    }

    public UserHomeHandler(ServerUser serverUser) {
        if (isEnabled()) {
            this.serverUser = serverUser;
            this.userHomes = new ArrayList<>();
        }
    }

    public void addHome(Home home) {
        this.userHomes.add(home);
        loadedHomes.add(home);
    }

    public void removeHome(String name) {
        removeHome(getHome(name));
    }

    public void removeHome(Home home) {
        this.userHomes.remove(home);
        loadedHomes.remove(home);
    }

    public Home getHome(String name) {
        for (Home home : this.userHomes) {
            if (home.getName().equals(name))
                return home;
        }

        return new Home();
    }

    public List<Home> getHomes() {
        return this.userHomes;
    }

    public int homes() {
        return this.userHomes.size();
    }

    public boolean hasHome(String name) {
        for (Home userHome : this.userHomes) {
            if (userHome.getName().equals(name))
                return true;
        }

        return false;
    }

    public void teleportToHome(OnlineUser user, String name) throws UnsafeHomeException {
        teleportToHome(user, getHome(name));
    }

    public void teleportToHome(OnlineUser user, Home home) throws UnsafeHomeException {
        if (user.isOnline()) {
            ServerWorld world = Objects.requireNonNull(KiloEssentials.getServer()).getWorld(RegistryUtils.dimensionTypeToRegistryKey(home.getLocation().getDimensionType()));

            if (world == null) {
                throw new UnsafeHomeException(home, Reason.MISSING_DIMENSION);
            }

            if (!userHomes.contains(home)) {
                user.sendLangMessage("command.home.invalid_home");
                return;
            }

            Home.teleportTo(user, home);
        }

    }

    public NbtCompound serialize(NbtCompound tag) {
        for (Home userHome : this.userHomes) {
            tag.put(userHome.getName(), userHome.toTag());
        }

        return tag;
    }

    public void deserialize(NbtCompound NbtCompound) {
        for (String key : NbtCompound.getKeys()) {
            Home home = new Home(NbtCompound.getCompound(key));
            home.setName(key);
            home.setOwner(this.serverUser.uuid);
            this.userHomes.add(home);
            loadedHomes.add(home);
        }

    }

    @Deprecated
    public static List<Home> getHomesOf(UUID uuid) {
        List<Home> list = new ArrayList<>();
        for (Home home : loadedHomes) {
            if  (home.getOwner().equals(uuid)) list.add(home);
        }

        return list;
    }

    public static CompletableFuture<Suggestions> suggestHomes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(KiloServer.getServer().getOnlineUser(
                context.getSource().getPlayer()).getHomesHandler().getHomes().stream().map(Home::getName), builder);
    }

    public enum Reason {
        UNSAFE_DESTINATION, MISSING_DIMENSION, NO_PERMISSION;
    }

}
