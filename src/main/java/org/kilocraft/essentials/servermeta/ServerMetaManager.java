package org.kilocraft.essentials.servermeta;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerMetaManager {
    private final ServerMetadata metadata;

    public ServerMetaManager(ServerMetadata metadata) {
        this.metadata = metadata;
    }

    public void load() {
        PlayerListMeta.load();
    }

    public void updateAll() {
        for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            if (playerEntity.networkHandler == null) {
                continue;
            }

            PlayerListMeta.update(playerEntity);
        }
    }

    public void updateDisplayName(ServerPlayerEntity player) {
        if (player != null) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
            KiloServer.getServer().sendGlobalPacket(packet);
        }
    }

    public void onPlayerJoined(ServerPlayerEntity player) {
        PlayerListMeta.update(player);
    }

    public final void setDescription(final Text description) throws IOException {
        this.metadata.setDescription(description);

        final Properties properties = new Properties();
        properties.load(new FileInputStream(KiloEssentials.getServerProperties().toFile()));
        properties.setProperty("motd", TextFormat.translate(description.getString()));
        properties.store(new FileOutputStream(KiloEssentials.getServerProperties().toFile()), "");
    }

    public final Text getDescription() {
        return this.metadata.getDescription();
    }

}
