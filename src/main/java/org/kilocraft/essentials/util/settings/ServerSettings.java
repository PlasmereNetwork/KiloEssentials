package org.kilocraft.essentials.util.settings;

import net.minecraft.SharedConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.values.*;
import org.kilocraft.essentials.util.settings.values.util.RootSetting;

import java.util.*;

public class ServerSettings implements NBTStorage {

    public static RootSetting root = new RootSetting();

    public ServerSettings() {
        NBTStorageUtil.addCallback(this);
    }

    public static boolean getBoolean(String id) {
        return ((BooleanSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static double getDouble(String id) {
        return ((DoubleSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static float getFloat(String id) {
        return ((FloatSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static int getInt(String id) {
        return ((IntegerSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static void setBoolean(String id, boolean value) {
        ((BooleanSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setDouble(String id, double value) {
        ((DoubleSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setFloat(String id, float value) {
        ((FloatSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setInt(String id, int value) {
        ((IntegerSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public void registerSettings() {
        //Custom settings
        IntegerSetting viewDistance = (IntegerSetting) new IntegerSetting(10, "view_distance").onChanged(distance -> KiloEssentials.getServer().getMinecraftServer().getPlayerManager().setViewDistance(distance));
        BooleanSetting debug = (BooleanSetting) new BooleanSetting(false, "debug").onChanged(b -> SharedConstants.isDevelopment = b);

        //Patches
        CategorySetting patch = new CategorySetting("patch");
        //Donkey dupe
        BooleanSetting donkeyDupe = new BooleanSetting(true, "donkey_dupe");
        //Stuck Wither
        CategorySetting wither = new CategorySetting("wither");
        IntegerSetting check_distance = new IntegerSetting(2, "check_distance").range(-256, 256);
        DoubleSetting tp_distance = new DoubleSetting(1D, "tp_distance").range(-256D, 256D);
        wither.addChild(check_distance);
        wither.addChild(tp_distance);
        //per-player-mobcap
        BooleanSetting ppmobcap = new BooleanSetting(false, "ppmobcap");
        FloatSetting max = new FloatSetting(1.2F, "max");
        ppmobcap.addChild(max);

        //Enchanting
        CategorySetting enchanting = new CategorySetting("enchanting");
        BooleanSetting hasChanged = new BooleanSetting(false, "hasChanged");
        enchanting.addChild(hasChanged);


        //Entity merging
        CategorySetting item_merge = new CategorySetting("item_merge");
        DoubleSetting radius = new DoubleSetting(0.5D, "radius").range(0D, 256D);
        BooleanSetting adjust_movement = new BooleanSetting(true, "adjust_movement");
        item_merge.addChild(radius);
        item_merge.addChild(adjust_movement);
        //Shulker spawn chance
        DoubleSetting shulker_spawn_chance = new DoubleSetting(0D, "shulker_spawn_chance");

        patch.addChild(donkeyDupe);
        patch.addChild(wither);
        patch.addChild(ppmobcap);
        patch.addChild(enchanting);
        patch.addChild(item_merge);
        patch.addChild(shulker_spawn_chance);

        //Entity Limit
        CategorySetting entity_limit = new CategorySetting("entity_limit");
        List<String> limit_entries = new ArrayList<>();
        for (EntityType entityType : new EntityType[]{EntityType.GUARDIAN, EntityType.ITEM_FRAME, EntityType.CHICKEN}) {
            limit_entries.add(Registry.ENTITY_TYPE.getId(entityType).getPath());
        }
        limit_entries.add("animals");
        for (String limit_entry : limit_entries) {
            CategorySetting entity = new CategorySetting(limit_entry);
            IntegerSetting limit = new IntegerSetting(-1, "limit");
            IntegerSetting range = new IntegerSetting(-1, "range");
            entity.addChild(limit);
            entity.addChild(range);
            entity_limit.addChild(entity);
        }

        //Ticking
        CategorySetting tick = new CategorySetting("tick");
        IntegerSetting distance = new IntegerSetting(10, "distance");
        BooleanSetting entity = new BooleanSetting(true, "entity");
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            BooleanSetting value = new BooleanSetting(true, Registry.ENTITY_TYPE.getId(entityType).getPath());
            entity.addChild(value);
        }

        tick.addChild(distance);
        tick.addChild(entity);

        //Mobcap
        CategorySetting mobcap = new CategorySetting("mobcap");
        for (RegistryKey<World> registryKey : RegistryUtils.getWorldsKeySet()) {
            FloatSetting world = new FloatSetting(1F, registryKey.getValue().getPath()).range(0F, 100F);
            for (SpawnGroup spawnGroup : SpawnGroup.values()) {
                FloatSetting group = new FloatSetting(1F, spawnGroup.getName().toLowerCase()).range(0F, 100F);
                world.addChild(group);
            }
            mobcap.addChild(world);
        }
        root.addChild(viewDistance);
        root.addChild(debug);
        root.addChild(entity_limit);
        root.addChild(tick);
        root.addChild(mobcap);
        root.addChild(patch);
        KiloCommands.getInstance().register(new SettingCommand());
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("settings.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        return root.toTag();
    }

    @Override
    public void deserialize(@NotNull CompoundTag tag) {
        root.fromTag(tag);
    }

}
