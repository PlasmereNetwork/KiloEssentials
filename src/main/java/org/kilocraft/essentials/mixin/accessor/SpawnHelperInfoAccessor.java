package org.kilocraft.essentials.mixin.accessor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpawnHelper.Info.class)
public interface SpawnHelperInfoAccessor {

    @Accessor("spawningChunkCount")
    public int getSpawnChunkCount();

    @Accessor("groupToCountView")
    public Object2IntMap<SpawnGroup> getGroupToCountView();

}
