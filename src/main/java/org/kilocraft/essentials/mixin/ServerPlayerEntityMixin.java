package org.kilocraft.essentials.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "worldChanged", cancellable = true, at = @At(value = "HEAD"))
    private void modify(ServerWorld serverWorld, CallbackInfo ci) {
        if (LocationUtil.shouldBlockAccessTo(serverWorld.getDimension())) {
            ci.cancel();
            KiloServer.getServer().getOnlineUser((ServerPlayerEntity) (Object)this).sendLangMessage("general.dimension_not_allowed", RegistryUtils.dimensionToName(serverWorld.getDimension()));
        }

        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "teleport", at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V"),cancellable = true)
    private void modify$Teleport(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

}
