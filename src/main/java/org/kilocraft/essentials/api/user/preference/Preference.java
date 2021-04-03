package org.kilocraft.essentials.api.user.preference;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.user.preference.Preferences;

import java.util.function.Consumer;

public class Preference<T> {
    private final String id;
    private final T defaultValue;
    private Consumer<SerializerFunction> serializer;
    private Consumer<SerializerFunction> deserializer;
    private boolean hasCustomSerializer;

    public Preference(@NotNull final String id, @Nullable final T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        Preferences.list.add(this);
    }

    public Preference(@NotNull final String id, @Nullable final T defaultValue,
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

    public void toTag(@NotNull final NbtCompound tag, @NotNull final Object value) throws IllegalArgumentException {
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
            throw new IllegalArgumentException(this.getSerializeExceptionMessage());
        }
    }

    public Object fromTag(@NotNull final NbtCompound tag) throws IllegalArgumentException {
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
            throw new IllegalArgumentException(this.getSerializeExceptionMessage());
        }
    }

    public class SerializerFunction {
        private final NbtCompound tag;
        private T value;
        private final Preference<T> preference;

        public SerializerFunction(@NotNull final NbtCompound tag, @NotNull final T value, @NotNull final Preference<T> preference) {
            this.tag = tag;
            this.value = value;
            this.preference = preference;
        }

        public NbtCompound tag() {
            return this.tag;
        }

        public T value() {
            return this.value;
        }

        public Preference<T> setting() {
            return this.preference;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    private String getSerializeExceptionMessage() {
        return "Un-supported Data value Type! \"" + this.defaultValue + "\" is not supported! You need a custom Serializer function for this";
    }
}
