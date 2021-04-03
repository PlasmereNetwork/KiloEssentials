package org.kilocraft.essentials.user;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.lang3.time.StopWatch;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserHandler {
    public static final short DATA_VERSION = 3;
    private static final File saveDir = KiloEssentials.getDataDirPath().resolve("users").toFile();

    void handleUser(final ServerUser serverUser) throws IOException {
        if (!this.loadUser(serverUser)) {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(serverUser).createNewFile();
            this.save(serverUser);
            this.handleUser(serverUser);
        }

    }

    void loadUserAndResolveName(final ServerUser user) throws IOException {
        if (this.getUserFile(user).exists()) {
            NbtCompound tag = NbtIo.readCompressed(new FileInputStream(this.getUserFile(user)));
            user.fromTag(tag);
            user.name = tag.getString("name");
        }
    }

    private boolean loadUser(final ServerUser serverUser) throws IOException {
        if (this.getUserFile(serverUser).exists()) {
            serverUser.fromTag(NbtIo.readCompressed(new FileInputStream(this.getUserFile(serverUser))));
            return true;
        }
        return false;
    }

    void save(final ServerUser user) throws IOException {
        if (this.getUserFile(user).exists()) {
            NbtIo.writeCompressed(
                    user.toTag(),
                    new FileOutputStream(this.getUserFile(user))
            );
        } else {
            UserHandler.saveDir.mkdirs();
            this.getUserFile(user).createNewFile();
            this.save(user);
        }
    }

    boolean userExists(final UUID uuid) {
        return this.getUserFile(uuid).exists();
    }

    private File getUserFile(final ServerUser serverUser) {
        return this.getUserFile(serverUser.uuid);
    }

    private File getUserFile(final UUID uuid) {
        return KiloEssentials.getDataDirPath().resolve("users").resolve(uuid.toString() + ".dat").toFile();
    }

    public File[] getUserFiles() {
        return KiloEssentials.getDataDirPath().resolve("users").toFile().listFiles();
    }


    public void upgrade() {
        File[] files = getUserFiles();
        if (files == null || files.length <= 0) {
            return;
        }

        int random = ThreadLocalRandom.current().nextInt(0, files.length);

        File file = files[random];
        UUID uuid = UUID.fromString(file.getName().replace(".dat", ""));

        try {
            if (upgrade(file, uuid)) {
                KiloEssentials.getLogger().info("Found old data format! Updating the user data format!");
                upgradeAll();
            }
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Failed at checking the user data!", e);
        }
    }

    private void upgradeAll() {
        int updated = 0;
        StopWatch watch = new StopWatch();
        watch.start();
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        for (File file : getUserFiles()) {
            String strId = file.getName().replace(".dat", "");
            Matcher matcher = pattern.matcher(strId);
            if (!matcher.matches()) {
                continue;
            }
            UUID uuid = UUID.fromString(strId);
            try {
                if (upgrade(file, uuid)) {
                    updated++;
                }
            } catch (IOException e) {
                KiloEssentials.getLogger().error("Failed to update User File [" + uuid + "]", e);
            }
        }

        watch.stop();
        String timeElapsed = new DecimalFormat("##.##").format(watch.getTime(TimeUnit.MILLISECONDS));
        KiloEssentials.getLogger().info("Successfully upgraded the User data for " + updated + " users, time elapsed: " + timeElapsed + "ms");
    }

    private boolean upgrade(File file, UUID uuid) throws IOException {
        NbtCompound tag;

        try {
            tag = NbtIo.readCompressed(new FileInputStream(file));
        } catch (Exception e) {
            KiloEssentials.getLogger().warn("Broken user data! [" + uuid + "] Please check their user file!");
            return true;
        }

        if (!tag.contains("dataVer")) {
            tag.putShort("dataVer", DATA_VERSION);
        }

        short dataVer = tag.getShort("dataVer");

        if (dataVer != 0) {
            if (dataVer < DATA_VERSION) {
                NbtIo.writeCompressed(tag, new FileOutputStream(file));

                if (SharedConstants.isDevelopment) {
                    KiloEssentials.getLogger().info("Updated User data for user [" + tag.getString("name") + "/" + uuid.toString() + "]");
                }

                return true;
            }
        }

        return false;
    }
}
