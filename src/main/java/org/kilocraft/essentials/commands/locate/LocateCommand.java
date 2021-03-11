package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class LocateCommand extends EssentialCommand {
    public LocateCommand() {
        super("ke_locate", CommandPermission.LOCATE);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LocateBiomeCommand.registerAsChild(argumentBuilder);
        LocateStructureCommand.registerAsChild(argumentBuilder);
    }

}
