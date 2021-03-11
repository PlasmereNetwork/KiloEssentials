package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerStopRidingEvent;
import org.kilocraft.essentials.api.user.OnlineUser;

public class PlayerStopRidingEventImpl implements PlayerStopRidingEvent {
    private boolean cancelled = false;
    private ServerPlayerEntity player;

    public PlayerStopRidingEventImpl(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @Override
    public OnlineUser getUser() {
        return KiloServer.getServer().getOnlineUser(this.player);
    }
}
