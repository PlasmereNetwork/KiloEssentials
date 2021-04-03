package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixinPatch {

    @Shadow
    public abstract void playSound(PlayerEntity playerEntity, double d, double e, double f, SoundEvent soundEvent, SoundCategory soundCategory, float g, float h);

    @Inject(method = "syncGlobalEvent", at = @At(value = "HEAD"), cancellable = true)
    public void shouldWeAnnoyEveryone(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        if (!ServerSettings.getBoolean("patch.global_sound")) {
            ci.cancel();
            SoundEvent soundEvent = null;
            float g = 1.0F;
            float h = 1.0F;
            boolean b = true;
            switch (i) {
                case 1023:
                    soundEvent = SoundEvents.ENTITY_WITHER_SPAWN;
                    break;
                case 1028:
                    soundEvent = SoundEvents.ENTITY_ENDER_DRAGON_DEATH;
                    g = 5.0F;
                    break;
                case 1038:
                    soundEvent = SoundEvents.BLOCK_END_PORTAL_SPAWN;
                    break;
                default:
                    b = false;
            }
            if (b)
                this.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), soundEvent, SoundCategory.HOSTILE, g, h);
        }
    }

}
