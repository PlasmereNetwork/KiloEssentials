package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpawnHelper.class)
public interface SpawnHelperAccessor {

    @Accessor("CHUNK_AREA")
    public static int getChunkArea() {
        throw new AssertionError();
    }

}
