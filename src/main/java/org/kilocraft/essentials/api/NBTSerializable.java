package org.kilocraft.essentials.api;

import net.minecraft.nbt.CompoundTag;

public interface NBTSerializable {
    /**
     * Convert all the data into a CompoundTag for serialization
     *
     * @return The Tag to Save
     */
    CompoundTag toTag();

    /**
     * Get all the data from the CompoundTag for deserialization
     *
     * @param tag Tag to get the data from
     */
    void fromTag(CompoundTag tag);
}
