package org.kilocraft.essentials.extensions.warps.serverwidewarps;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.extensions.warps.Warp;

public class ServerWarp extends Warp {
    private boolean addCommand;

    public ServerWarp(String name, Location location, boolean addCommand) {
        super(name, location);
        this.addCommand = addCommand;
    }

    public ServerWarp(String name, CompoundTag tag) {
        super(name, null);
        fromTag(tag);
    }

    public boolean addCommand() {
        return this.addCommand;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compoundTag = super.toTag();

        if (this.addCommand) {
            compoundTag.putBoolean("addCmd", true);
        }

        return compoundTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);

        if (tag.contains("addCmd")) {
            this.addCommand = true;
        }
    }
}
