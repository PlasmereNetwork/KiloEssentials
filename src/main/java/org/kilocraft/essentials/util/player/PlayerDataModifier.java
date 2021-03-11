package org.kilocraft.essentials.util.player;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataModifier {
    private UUID uuid;
    private CompoundTag compoundTag;

    public PlayerDataModifier(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean load() {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");
        if (!file.exists())
            return false;

        try {
            this.compoundTag = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) { }

        return true;
    }

    public boolean save() {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");
        if (!file.exists())
            return false;

        try {
            NbtIo.writeCompressed(this.compoundTag, new FileOutputStream(file));
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public CompoundTag getTag() {
        return this.compoundTag;
    }

    public void setCustomName(Text text) {
        if (text != null)
            this.compoundTag.putString("CustomName", Text.Serializer.toJson(text));
        else
            this.compoundTag.remove("CustomName");
    }

    public EnderChestInventory getEnderChest() {
        EnderChestInventory inv = new EnderChestInventory();
        inv.readTags(this.compoundTag.getList("EnderItems", 10));
        return inv;
    }

}
