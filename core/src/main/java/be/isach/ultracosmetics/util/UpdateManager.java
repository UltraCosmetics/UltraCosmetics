package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.task.UltraTask;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Level;

/**
 * Manages update checking.
 * <p>
 * Package: be.isach.ultracosmetics.util
 * Created by: sachalewin
 * Date: 5/08/16
 * Project: UltraCosmetics
 */
public class UpdateManager extends UltraTask {

    private static final String RESOURCE_URL = "https://api.spiget.org/v2/resources/10905/";
    // String starts with a space on purpose, because that's what's returned from the API for some reason.
    private static final String VERSIONS_PREFIX = " Supported versions: ";
    /**
     * Current UC version.
     */
    private final Version currentVersion;

    /**
     * Last Version published on spigotmc.org.
     */
    private Version spigotVersion;

    private final UltraCosmetics ultraCosmetics;

    /**
     * Whether the plugin is outdated or not.
     */
    private boolean outdated = false;

    private String status;

    public UpdateManager(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        Reader reader = UltraCosmeticsData.get().getPlugin().getFileReader("build_info.yml");
        YamlConfiguration buildInfo = YamlConfiguration.loadConfiguration(reader);
        String gitHash = buildInfo.getString("git-hash");
        this.currentVersion = new Version(ultraCosmetics.getDescription().getVersion(), gitHash);
    }

    /**
     * Checks for new update.
     */
    @Override
    public void run() {
        determineStatus();
        ultraCosmetics.getSmartLogger().write(status);
        if (outdated && SettingsManager.getConfig().getBoolean("Auto-Update")) {
            update();
        }
    }

    @Override
    public void schedule() {
        task = getScheduler().runLaterAsync(this::run, 1);
    }

    private void determineStatus() {
        String spigotVersionString = fetchSpigotVersion();
        if (spigotVersionString == null) {
            status = "Cannot update, unknown version";
            return;
        }
        spigotVersion = new Version(spigotVersionString);
        if (currentVersion.compareTo(spigotVersion) == 0) {
            status = "You are running the latest version on Spigot.";
            return;
        }
        if (currentVersion.compareTo(spigotVersion) > 0) {
            status = "You are running a version newer than the latest one on Spigot.";
            return;
        }
        if (!checkMinecraftVersion()) {
            status = "A new version is available on Spigot, but it doesn't support this server version.";
            return;
        }

        outdated = true;
        status = "New version available on Spigot: " + spigotVersion.numbersOnly();
    }

    public boolean update() {
        if (!download()) {
            ultraCosmetics.getSmartLogger().write("Failed to download update");
            return false;
        }
        outdated = false;
        status = "Successfully downloaded new version, restart server to apply update.";
        ultraCosmetics.getSmartLogger().write(status);
        return true;
    }

    /**
     * Fetches latest version published on Spigot.
     *
     * @return latest version published on Spigot.
     */
    private String fetchSpigotVersion() {
        JsonObject jsonVersion = (JsonObject) apiRequest("versions/latest");
        if (jsonVersion == null) {
            return null;
        }
        String version = jsonVersion.get("name").getAsString();
        return version;
    }

    public Version getSpigotVersion() {
        return spigotVersion;
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    private JsonElement apiRequest(String suffix) {
        InputStreamReader reader = null;
        try {
            URL url = new URL(RESOURCE_URL + suffix);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "UltraCosmetics Update Checker"); // Sets the user-agent

            InputStream inputStream = connection.getInputStream();
            reader = new InputStreamReader(inputStream);

            // Earlier versions of GSON don't have the static
            // parsing methods present in recent versions.
            @SuppressWarnings("deprecation")
            JsonElement response = new JsonParser().parse(reader);
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            ultraCosmetics.getSmartLogger().write(LogLevel.ERROR, "Failed to check for an update on spigot.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Downloads the file
     * <p>
     * Borrowed from <a href="https://github.com/Stipess1/AutoUpdater/blob/master/src/main/java/com/stipess1/updater/Updater.java">AutoUpdater</a>
     */
    private boolean download() {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            URL url = new URL(RESOURCE_URL + "download");
            in = new BufferedInputStream(url.openStream());
            File outputFile = new File(Bukkit.getUpdateFolderFile(), "UltraCosmetics-" + spigotVersion.numbersOnly() + "-RELEASE.jar");
            outputFile.getParentFile().mkdirs();
            fout = new FileOutputStream(outputFile);

            final byte[] data = new byte[4096];
            int count;
            while ((count = in.read(data, 0, 4096)) != -1) {
                fout.write(data, 0, count);
            }
            return true;
        } catch (Exception e) {
            ultraCosmetics.getLogger().log(Level.SEVERE, null, e);
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                ultraCosmetics.getLogger().log(Level.SEVERE, null, e);
                e.printStackTrace();
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (final IOException e) {
                ultraCosmetics.getLogger().log(Level.SEVERE, null, e);
                e.printStackTrace();
            }
        }
    }

    private boolean checkMinecraftVersion() {
        JsonObject update = (JsonObject) apiRequest("updates/latest");
        // Gets the property "description" of the returned JSON object,
        // base64-decodes it, and stores it in `description`.
        String description = new String(Base64.getDecoder().decode(update.get("description").getAsString()));

        // Basically the way this works, is each update description has a line at the end like this:
        // "Supported versions: 1.8.8, 1.12.2, 1.16.5, 1.17.1, 1.18.2"
        // So we need to parse it and find out if this server's MC version is in this list
        String[] lines = description.split("\\<br\\>");
        String supportedVersionsLine = lines[lines.length - 1];

        if (!supportedVersionsLine.startsWith(VERSIONS_PREFIX)) {
            ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, "Can't read supported versions line:");
            ultraCosmetics.getSmartLogger().write(LogLevel.WARNING, supportedVersionsLine);
            return false;
        }
        supportedVersionsLine = supportedVersionsLine.substring(VERSIONS_PREFIX.length());
        String[] supportedVersions = supportedVersionsLine.split(", ");
        // Returns a string like "1.18.2-R0.1-SNAPSHOT"
        String thisMinecraftVersion = Bukkit.getBukkitVersion();
        // Cuts the string to something like "1.18.2"
        thisMinecraftVersion = thisMinecraftVersion.substring(0, thisMinecraftVersion.indexOf('-'));

        // Since 3.0, the range of supported versions is wide enough we can just check if
        // the server's version is less than or equal to the highest version listed supported.
        String[] tmvParts = thisMinecraftVersion.split("\\.");
        String[] supportedParts = supportedVersions[supportedVersions.length - 1].split("\\.");
        for (int i = 0; i < tmvParts.length; i++) {
            if (i >= supportedParts.length) {
                // If the server's version has more parts than the highest supported version, it's not supported.
                return false;
            }
            int tmvPart = Integer.parseInt(tmvParts[i]);
            int supportedPart = Integer.parseInt(supportedParts[i]);
            // For the first non-equal parts, if the new version is greater than this version, new version wins
            if (supportedPart > tmvPart) return true;
            // Or, if the new version is less than this version (??), this version wins.
            if (supportedPart < tmvPart) return false;
        }
        // If all parts are equal, new version wins.
        return true;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getStatus() {
        return status;
    }
}
