package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReadyEvent;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.votifier.Votifier;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.WarpCommand;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

public class OnServerReady implements EventHandler<ServerReadyEvent> {
    @Override
    public void handle(@NotNull ServerReadyEvent event) {
        KiloEssentials.getInstance().getSettingManager().registerSettings();
        NBTStorageUtil.onLoad();
        BrandedServer.set();
        KiloServer.getServer().getMetaManager().load();
        WarpCommand.registerAliases();
        KiloServer.getServer().setName(KiloConfig.main().server().name == null ? "" : KiloConfig.main().server().name);
        KiloEssentials.getInstance().getFeatures().loadAll(false);
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onServerReady();

        if (KiloConfig.main().votifier().enabled) {
            new Votifier().onEnable();
        }
    }
}
