package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListHeaderS2CPacket.class)
public interface PlayerListHeaderS2CPacketMixin extends Packet<ClientPlayPacketListener> {

    @Accessor("header")
    void setHeader(Text text);

    @Accessor("footer")
    void setFooter(Text text);

}
