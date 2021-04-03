package org.kilocraft.essentials.api;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.provided.KiloFile;

public interface NBTStorage {

    /**
     * The File to save the data in
     *
     * @return Save File
     */
    KiloFile getSaveFile();

    /**
     * Serialize all the Data into a NbtCompound
     *
     * @return the Tag to save
     */
    NbtCompound serialize();

    /**
     * Deserialize all the Data from the NbtCompound
     *
     * @param NbtCompound the Tag to get the data from
     */
    void deserialize(@NotNull NbtCompound NbtCompound);

}
