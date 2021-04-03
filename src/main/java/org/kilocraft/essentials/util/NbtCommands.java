package org.kilocraft.essentials.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.util.player.UserUtils;

public class NbtCommands {
    public static boolean fromRightClick(PlayerEntity player, Hand hand) {
        return trigger(player, hand, !player.handSwinging);
    }

    private static boolean trigger(PlayerEntity player, Hand hand, boolean swingHand) {
        if (hand == Hand.OFF_HAND) {
            return false;
        }

        ItemStack stack = player.getMainHandStack();
        NbtCompound tag = stack.getTag();

        if (tag == null || tag.getSize() == 0 || !tag.contains("NBTCommands")) {
            return false;
        }

        NbtList NbtList = tag.getList("NBTCommands", 8);

        int succeededExecutions = 0;
        for (int i = 0; i < NbtList.size(); i++) {
            int value = CommandUtils.runCommandWithFormatting(player.getCommandSource(), NbtList.getString(i));
            if (value >= 1) {
                succeededExecutions++;
            }
        }

        if (succeededExecutions >= 1 && swingHand) {
            UserUtils.Animate.swingHand(player);
        }

        return true;
    }

}
