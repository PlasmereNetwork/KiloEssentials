package org.kilocraft.essentials.config.messages.sections.commands;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class RtpCommandConfigSection {

    @Setting(value = "start")
    public String start = "&ePlease wait...";

    @Setting(value = "empty")
    public String empty = "&cYou don't have any Random Teleports left!";

    @Setting(value = "dimensionException")
    public String dimensionException = "&cYou can only randomly teleport in the Overworld!";

    @Setting(value = "teleported", comment = "Local Variables: {BIOME}, {RTP_LEFT}, {cord.X}, {cord.Y}, {cord.Z}, {ELAPSED_TIME}")
    public String teleported = "&6{ELAPSED_TIME}&e elapsed, You now have &6{RTP_LEFT}&e Random Teleports left!";
}
