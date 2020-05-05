package org.kilocraft.essentials.api.user.settting;

import com.google.inject.internal.asm.$Label;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.user.setting.Settings;

import java.util.List;
import java.util.function.Consumer;

public class Setting<T> {
    private final String id;
    private final T defaultValue;
    private Consumer<SerializerFunction> serializer;
    private Consumer<SerializerFunction> deserializer;
    private boolean hasCustomSerializer;

    public Setting(@NotNull final String id, @Nullable final T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        Settings.list.add(this);
    }

    public Setting(@NotNull final String id, @Nullable final T defaultValue,
                   @NotNull final Consumer<SerializerFunction> serializer,
                   @NotNull final Consumer<SerializerFunction> deserializer) {
        this(id, defaultValue);
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.hasCustomSerializer = true;
    }

    public String getId() {
        return this.id;
    }

    public T getDefault() {
        return this.defaultValue;
    }

    public void toTag(@NotNull final CompoundTag tag, @NotNull final Object value) throws IllegalArgumentException {
        if (this.hasCustomSerializer) {
            this.serializer.accept(new SerializerFunction(tag, (T) value, this));
        } else if (value instanceof String) {
            tag.putString(this.id, (String) value);
        } else if (value instanceof Integer) {
            tag.putInt(this.id, (Integer) value);
        } else if (value instanceof Boolean) {
            tag.putBoolean(this.id, (Boolean) value);
        } else if (value instanceof Double) {
            tag.putDouble(this.id, (Double) value);
        } else if (value instanceof Long) {
            tag.putLong(this.id, (Long) value);
        } else if (value instanceof Short) {
            tag.putShort(this.id, (Short) value);
        } else if (value instanceof Byte) {
            tag.putByte(this.id, (Byte) value);
        } else {
            throw new IllegalArgumentException("UnSupported Data value Type! [" + value.toString() + "] is not supported");
        }
    }

    public Object fromTag(@NotNull final CompoundTag tag) throws IllegalArgumentException {
        if (this.hasCustomSerializer) {
            SerializerFunction function = new SerializerFunction(tag, this.defaultValue, this);
            this.deserializer.accept(function);
            return function.value;
        } else if (this.defaultValue instanceof String) {
            return tag.getString(this.id);
        } else if (this.defaultValue instanceof Integer) {
            return tag.getInt(this.id);
        } else if (this.defaultValue instanceof Boolean) {
            return tag.getBoolean(this.id);
        } else if (this.defaultValue instanceof Double) {
            return tag.getDouble(this.id);
        } else if (this.defaultValue instanceof Long) {
            return tag.getLong(this.id);
        } else if (this.defaultValue instanceof Short) {
            return tag.getShort(this.id);
        } else if (this.defaultValue instanceof Byte) {
            return tag.getByte(this.id);
        } else {
            throw new IllegalArgumentException("UnSupported Data value Type! [" + this.defaultValue.toString() + "] is not supported");
        }
    }

    public class SerializerFunction {
        private final CompoundTag tag;
        private T value;
        private final Setting<T> setting;

        public SerializerFunction(@NotNull final CompoundTag tag, @NotNull final T value, @NotNull final Setting<T> setting) {
            this.tag = tag;
            this.value = value;
            this.setting = setting;
        }

        public CompoundTag tag() {
            return this.tag;
        }

        public T value() {
            return this.value;
        }

        public Setting<T> setting() {
            return this.setting;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}
