package org.kilocraft.essentials.extensions.warps;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.NBTSerializable;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;

public abstract class Warp implements NBTSerializable {
    private String name;
    private Location location;

    public Warp(@NotNull final String name, @Nullable final Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return this.name;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("loc", this.location.toTag());

        return compoundTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (this.location == null) {
            this.location = Vec3dLocation.dummy();
        }

        this.location.fromTag(tag.getCompound("loc"));
    }
}
