package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.preference.Preferences;

public class SilenceCommand extends EssentialCommand {
    public SilenceCommand() {
        super("silence");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        boolean set = !user.getPreference(Preferences.SOUNDS);
        user.getPreferences().set(Preferences.SOUNDS, set);

        if (set) {
            user.sendLangMessage("command.silence.off");
        } else {
            user.sendLangMessage("command.silence.on");
        }

        return SUCCESS;
    }

}
