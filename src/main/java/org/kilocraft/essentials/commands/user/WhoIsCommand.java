package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.preference.UserPreferences;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.text.Texter;

public class WhoIsCommand extends EssentialCommand {
    public WhoIsCommand() {
        super("whois", CommandPermission.WHOIS_SELF, new String[]{"info"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.WHOIS_OTHERS))
                .executes(this::executeOthers);

        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(userArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        CommandSourceUser user = getCommandSource(ctx);
        return execute(user, getOnlineUser(ctx));
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = getCommandSource(ctx);
        getEssentials().getUserThenAcceptAsync(src, getUserArgumentInput(ctx, "user"), (user) -> {
            execute(src, user);
        });

        return AWAIT;
    }

    private int execute(CommandSourceUser src, User target) {
        Texter.InfoBlockStyle text = new Texter.InfoBlockStyle("Who's " + target.getNameTag(), Formatting.GOLD, Formatting.AQUA, Formatting.GRAY);

        text.append("DisplayName", target.getFormattedDisplayName())
                .space()
                .append("(").append(target.getUsername()).append(")")
                .space()
                .append(
                        Texter.appendButton(
                                Texter.newText("( More )").formatted(Formatting.GRAY),
                                Texter.newText("Click to see the name history"),
                                ClickEvent.Action.RUN_COMMAND,
                                "/whowas " + target.getUsername()
                        )
                );

        text.append("UUID",
                Texter.appendButton(
                        new LiteralText(target.getUuid().toString()),
                        new LiteralText(tl("general.click_copy")),
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        target.getUuid().toString()
                )
        );
        text.append("IP (Last Saved)",
                Texter.appendButton(
                        new LiteralText(target.getLastSocketAddress()),
                        new LiteralText(tl("general.click_copy")),
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        target.getLastSocketAddress()
                )
        );

        UserPreferences settings = target.getPreferences();
        text.append("Status",
                new String[]{"Invulnerable", "GameMode", "Online"},
                settings.get(Preferences.INVULNERABLE),
                settings.get(Preferences.GAME_MODE).getName(),
                target.isOnline()
        );

        if (target.isOnline()) {
            OnlineUser user = (OnlineUser) target;
            text.append("Survival status",
                    new String[]{"Health", "FoodLevel", "Saturation"},
                    ModConstants.DECIMAL_FORMAT.format(user.asPlayer().getHealth()),
                    ModConstants.DECIMAL_FORMAT.format(user.asPlayer().getHungerManager().getFoodLevel()),
                    ModConstants.DECIMAL_FORMAT.format(user.asPlayer().getHungerManager().getSaturationLevel())
            );
        }

        text.append("Artifacts",
                new String[]{"IsStaff", "May Sit"},
                ((ServerUser) target).isStaff(),
                settings.get(Preferences.CAN_SEAT)
        );

        if (target.getTicksPlayed() >= 0) {
            text.append("Playtime", TimeDifferenceUtil.convertSecondsToString(target.getTicksPlayed() / 20, '6', 'e'));
        }
        if (target.getFirstJoin() != null) {
            text.append("First joined", Texter.newText("&e" + TimeDifferenceUtil.formatDateDiff(target.getFirstJoin().getTime())).styled((style) -> {
                return style.withHoverEvent(Texter.Events.onHover("&d" + ModConstants.DATE_FORMAT.format(target.getFirstJoin())));
            }));
        }

        if (!target.isOnline() && target.getLastOnline() != null) {
            text.append("Last Online", Texter.newText("&e" +  TimeDifferenceUtil.formatDateDiff(target.getLastOnline().getTime())).styled((style) -> {
                return style.withHoverEvent(Texter.Events.onHover("&d" + ModConstants.DATE_FORMAT.format(target.getLastOnline())));
            }));
        }

        text.append("Meta", new String[]{"Homes", "RTP", "Selected channel"},
                UserHomeHandler.isEnabled() ? target.getHomesHandler().homes() : 0,
                target.getPreference(Preferences.RANDOM_TELEPORTS_LEFT),
                target.getPreference(Preferences.CHAT_CHANNEL).getId());

        text.append("Is Spying", new String[]{"On Commands", "On Social"},
                target.getPreference(Preferences.COMMAND_SPY),
                target.getPreference(Preferences.SOCIAL_SPY));

        Vec3dLocation vec = ((Vec3dLocation) target.getLocation()).shortDecimals();
        assert vec.getDimension() != null;
        MutableText loc = Texter.newText(vec.asFormattedString());
        text.append("Location", getButtonForVec(loc, vec));

        if (target.getLastSavedLocation() != null) {
            Vec3dLocation savedVec = ((Vec3dLocation) target.getLastSavedLocation()).shortDecimals();
            MutableText lastLoc = Texter.newText(savedVec.asFormattedString());
            text.append("Saved Location", getButtonForVec(lastLoc, savedVec));
        }

        src.sendMessage(text.build());
        return SUCCESS;
    }

    private MutableText getButtonForVec(MutableText text, Vec3dLocation vec) {
        assert vec.getDimension() != null;
        return Texter.appendButton(
                text,
                new LiteralText(tl("general.click_tp")),
                ClickEvent.Action.SUGGEST_COMMAND,
                "/tpin " + vec.getDimension().toString() + " " +
                        vec.getX() + " " + vec.getY() + " " + vec.getZ() + " @s"
        );
    }
}