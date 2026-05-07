package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.task.UltraTask;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;
import be.isach.ultracosmetics.version.ServerVersion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    private static final String RESOURCE_URL = "https://api.modrinth.com/v2/project/ultracosmetics/";
    private final UltraCosmetics ultraCosmetics;
    /**
     * Current UC version.
     */
    private final Version currentVersion;
    private final String userAgent;
    private final String serverBrand;

    private boolean apiGone = false;

    /**
     * Best available version available on modrinth
     */
    private Version modrinthVersion;
    private String modrinthSHA512;
    private String updateUrl;
    private String updateFilename;

    /**
     * Whether the plugin is outdated or not.
     */
    private boolean outdated = false;

    private String status = "no update check performed";

    public UpdateManager(UltraCosmetics ultraCosmetics) {
        this.ultraCosmetics = ultraCosmetics;
        Reader reader = UltraCosmeticsData.get().getPlugin().getFileReader("build_info.yml");
        YamlConfiguration buildInfo = YamlConfiguration.loadConfiguration(reader);
        String gitHash = buildInfo.getString("git-hash");
        this.currentVersion = new Version("0.0"/*ultraCosmetics.getDescription().getVersion()*/, gitHash);
        // email broken up to hopefully confuse dumb scrapers
        this.userAgent = "UltraCosmetics/UltraCosmetics/" + currentVersion + " (adm" + "in@ult" + "racosmetics.net)";

        switch (Bukkit.getName()) {
            case "Spigot":
            case "Paper":
            case "Folia":
                this.serverBrand = Bukkit.getName().toLowerCase(Locale.ROOT);
                break;
            default:
                this.serverBrand = "spigot";
        }
    }

    /**
     * Checks for new update.
     */
    @Override
    public void run() {
        status = fetchModrinthVersion();
        ultraCosmetics.getSmartLogger().write(status);
        if (outdated && SettingsManager.getConfig().getBoolean("Auto-Update")) {
            update();
        }
    }

    @Override
    public void schedule() {
        task = getScheduler().runLaterAsync(this::run, 1);
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
     * Get the latest version info from Modrinth
     *
     * @return Information about the update status (error, update available, etc.)
     */
    private String fetchModrinthVersion() {
        Map<String, String> params = new HashMap<>();
        params.put("loaders", "[\"" + serverBrand + "\"]");
        params.put("game_versions", "[\"" + ServerVersion.getMinecraftVersion(true) + "\"]");
        params.put("include_changelog", "false");
        JsonElement element = apiRequest("version", params);
        if (element == null || !element.isJsonArray()) {
            return "Failed to check for updates: invalid or no response from API";
        }
        JsonArray versions = element.getAsJsonArray();
        JsonObject version = null;
        for (JsonElement el : versions) {
            // Filter for release versions. Could also make this configurable
            if ("release".equals(el.getAsJsonObject().get("version_type").getAsString())) {
                version = el.getAsJsonObject();
                break;
            }
        }
        if (version == null) {
            return "No updates available";
        }
        String versionString = version.get("version_number").getAsString();
        if (versionString == null) {
            return "Cannot update, unknown version";
        }
        modrinthVersion = new Version(versionString);
        if (currentVersion.compareTo(modrinthVersion) == 0) {
            return "You are running the latest stable version available on Modrinth for this server version.";
        }
        if (currentVersion.compareTo(modrinthVersion) > 0) {
            return "You are running a version newer than the latest stable one on Modrinth for this server version.";
        }

        // We have a candidate for the update, now get the information we'd need to perform the update
        updateFilename = null;
        JsonElement el = version.get("files");
        if (el != null && el.isJsonArray()) {
            JsonArray files = el.getAsJsonArray();
            for (JsonElement fileEl : files) {
                JsonObject file = fileEl.getAsJsonObject();
                if (file != null && file.get("primary").getAsBoolean()) {
                    updateFilename = file.get("filename").getAsString();
                    updateUrl = file.get("url").getAsString();
                    JsonObject hashes = file.getAsJsonObject("hashes");
                    modrinthSHA512 = hashes.get("sha512").getAsString();
                    break;
                }
            }
        }
        if (updateFilename == null) {
            return "Cannot update, no download available through Modrinth";
        }

        outdated = true;
        return "New version available on Modrinth: " + modrinthVersion.versionWithClassifier();
    }

    public Version getModrinthVersion() {
        return modrinthVersion;
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    private JsonElement apiRequest(String path, Map<String, String> queryParams) {
        if (apiGone) {
            return null;
        }
        StringBuilder builder = new StringBuilder(path);
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            builder.append(first ? '?' : '&');
            first = false;
            builder.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        URL url = null;
        try {
            url = new URL(RESOURCE_URL + builder);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", userAgent);
            if (connection.getResponseCode() == 410) {
                // no further contact with API if it returns 410
                apiGone = true;
                return null;
            }

            InputStream inputStream = connection.getInputStream();
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                // Earlier versions of GSON don't have the static
                // parsing methods present in recent versions.
                @SuppressWarnings("deprecation")
                JsonElement response = new JsonParser().parse(reader);
                return response;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static byte[] hexStringToByteArray(String s) {
        // https://stackoverflow.com/a/19119453
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Downloads the file
     * <p>
     * Adapted from <a href="https://github.com/Stipess1/AutoUpdater/blob/master/src/main/java/com/stipess1/updater/Updater.java">AutoUpdater</a>
     */
    private boolean download() {
        URL url;
        MessageDigest hash;
        try {
            url = new URL(updateUrl);
            hash = MessageDigest.getInstance("SHA-512");
        } catch (MalformedURLException | NoSuchAlgorithmException e) {
            // should not happen
            throw new RuntimeException(e);
        }
        File outputFile = new File(Bukkit.getUpdateFolderFile(), updateFilename);
        outputFile.getParentFile().mkdirs();

        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fout = new FileOutputStream(outputFile)) {

            final byte[] data = new byte[4096];
            int count;
            while ((count = in.read(data, 0, 4096)) != -1) {
                fout.write(data, 0, count);
                hash.update(data, 0, count);
            }
        } catch (Exception e) {
            ultraCosmetics.getLogger().log(Level.SEVERE, null, e);
            return false;
        }
        if (modrinthSHA512 != null) {
            if (!Arrays.equals(hexStringToByteArray(modrinthSHA512), hash.digest())) {
                ultraCosmetics.getSmartLogger().write(LogLevel.ERROR, "Hash check failed, discarding update!");
                outputFile.delete();
                return false;
            }
        }
        return true;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getStatus() {
        return status;
    }
}
