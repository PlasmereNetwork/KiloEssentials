package org.kilocraft.essentials.config.main.sections;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ModerationConfigSection {

    @Setting("defaults")
    private Defaults defaults = new Defaults();

    @Setting("disconnectReasons")
    private Messages messages = new Messages();

    @Setting("meta")
    private Meta meta = new Meta();

    public Defaults defaults() {
        return defaults;
    }

    public Messages messages() {
        return messages;
    }

    public Meta meta() {
        return meta;
    }

    @ConfigSerializable
    public static class Defaults {
        @Setting(value = "mute")
        public String mute = "Muted by an operator";

        @Setting(value = "ban", comment = "The Default kick reason")
        public String ban = "Banned by an operator";
    }

    @ConfigSerializable
    public static class Messages {
        @Setting(value = "ban", comment = "Disconnect message for permanent bans")
        public String permBan = "&c&lYou have been banned!\n\n&cReason: &f{BAN_REASON}\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "temporaryBan", comment = "Disconnect message for temporary ip-bans")
        public String tempBan = "&c&lYou have been banned!\n\n&cReason: &f{BAN_REASON} \n&cUntil: &f{BAN_EXPIRY}\n&f{BAN_LEFT} left\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "ipBan", comment = "Disconnect message for permanent bans")
        public String permIpBan = "&c&lYou have been ip-banned!\n\n&cReason: &f{BAN_REASON}\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "temporaryIpBan", comment = "Disconnect message for temporary ip-bans")
        public String tempIpBan = "&c&lYou have been ip-banned!\n\n&cReason: &f{BAN_REASON} \n&cUntil: &f{BAN_EXPIRY}\n&f{BAN_LEFT} left\n&cBy: &f{BAN_SOURCE}\n\n&9Appeal at: &fdiscord.gg/uzuQEe9";

        @Setting(value = "whitelist", comment = "Disconnect message for not being whitelisted, leave it empty to use the default in-game translation")
        public String whitelist = "You are not white-listed on this server!";

        @Setting(value = "mute")
        public String mute = "&cYou are muted because of \"{MUTE_REASON}\"";

        @Setting(value = "tempMute")
        public String tempMute = "&cYou are muted for {MUTE_LEFT} because of \"{MUTE_REASON}\"";
    }

    @ConfigSerializable
    public static class Meta {
        @Setting(value = "broadcast", comment = "Defines whether bans should be announced in global or only in staff chat")
        public boolean broadcast = true;

        @Setting(value = "performedMessage", comment = "Sets the broadcast message")
        public String performed = "{VICTIM} was {TYPE} by {SOURCE} for \"{REASON}\" &7(&c{LENGTH}&7)";

        @Setting(value = "revokedMessage")
        public String revoked = "&c{VICTIM} was Un-{TYPE} by {SOURCE}";

        @Setting(value = "silentPrefix")
        public String silentPrefix = "&f[&7Silent&f]";

        @Setting(value = "wordBanned")
        public String wordBanned = "Banned";

        @Setting(value = "wordMuted")
        public String wordMuted = "Muted";

        @Setting(value = "wordPermanent")
        public String wordPermanent = "Permanent";
    }

}
