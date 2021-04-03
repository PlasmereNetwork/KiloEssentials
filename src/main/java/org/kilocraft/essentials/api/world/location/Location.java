package org.kilocraft.essentials.api.world.location;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public interface Location {
    double getX();
    double getY();
    double getZ();

    Identifier getDimension();

    DimensionType getDimensionType();

    EntityRotation getRotation();

    ServerWorld getWorld();
    boolean isSafe();
    boolean isSafeFor(OnlineUser user);
    boolean isSafeFor(ServerPlayerEntity player);

    NbtCompound toTag();
    void fromTag(NbtCompound tag);

    void setX(double x);
    void setY(double y);
    void setZ(double z);

    void setRotation(float yaw, float pitch);

    void setDimension(Identifier dimension);
    void setDimension(DimensionType type);

    ChunkPos toChunkPos();

    BlockPos toPos();

    Vec3d toVec3d();

    Vec3i toVec3i();

    Location up();

    Location down();

    default String asString() {
        return TextFormat.clearColorCodes(this.asFormattedString());
    }

    default String asFormattedString() {
        StringBuilder builder = new StringBuilder("&d");

        if (this.getWorld() != null) {
            builder.append(RegistryUtils.dimensionToName(this.getWorld().getDimension()));
        }

        builder.append("&8/&e").append(Math.round(this.getX())).append(", ").append(Math.round(this.getY())).append(", ").append(Math.round(this.getZ()));
        return TextFormat.translate(builder.toString());
    }

}
