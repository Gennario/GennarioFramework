package cz.gennario.gennarioframework.utils;

import com.google.gson.JsonParser;
import cz.gennario.gennarioframework.Main;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
public class PluginUpdater {

    public enum Checker {
        POLYMART,
        SPIGOTMC
    }

    private int resourceId;
    private JavaPlugin plugin;
    private Checker checker;

    private Map<String, String> data;
    private List<String> dataList;

    private String pluginVersion, sitesVersion;

    public PluginUpdater(int resourceId, JavaPlugin plugin, Checker checker) {
        this.resourceId = resourceId;
        this.plugin = plugin;
        this.checker = checker;

        this.data = new HashMap<>();
        this.dataList = new ArrayList<>();

        try {
            checkVersions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLoadMessage() {
        PluginDescriptionFile description = plugin.getDescription();
        pluginVersion = description.getVersion();
        Logger logger = Main.getInstance().getLogger();
        logger.log(Level.INFO, "-----------------------------------------------------------------------------------------");
        logger.log(Level.INFO, "Loading plugin " + description.getName() + " v." + description.getVersion());
        logger.log(Level.INFO, "-----------------------------------------------------------------------------------------");
        logger.log(Level.INFO, "");
        for (String key : data.keySet()) {
            logger.log(Level.INFO, " " + key + ": " + data.get(key));
        }
        for (String s : dataList) {
            logger.log(Level.INFO, s);
        }
        logger.log(Level.INFO, "");
        logger.log(Level.INFO, "This plugin is running on " + description.getVersion() + "...");
        if (sitesVersion != null)
            logger.log(Level.INFO, "Current plugin version on polymart is " + sitesVersion + "...");
        if (sitesVersion != null) {
            if (Objects.equals(pluginVersion, sitesVersion)) {
                logger.log(Level.INFO, "So your plugin is on the latest version...");
            } else {
                logger.log(Level.INFO, "So your plugin is outdated. Please update the plugin...");
            }
        }
        logger.log(Level.INFO, "");
        logger.log(Level.INFO, " Plugin author: " + description.getAuthors());
        logger.log(Level.INFO, "");
        logger.log(Level.INFO, "Thanks for choosing Gennario's Development...");
        logger.log(Level.INFO, "-----------------------------------------------------------------------------------------");
    }

    public void sendErrorOnLoadMessage(String message) {
        PluginDescriptionFile description = plugin.getDescription();
        pluginVersion = description.getVersion();

        Main.getInstance().getLogger().log(Level.WARNING, "-----------------------------------------------------------------------------------------");
        Main.getInstance().getLogger().log(Level.WARNING, "Loading plugin " + description.getName() + " v." + description.getVersion());
        Main.getInstance().getLogger().log(Level.WARNING, "-----------------------------------------------------------------------------------------");
        Main.getInstance().getLogger().log(Level.WARNING, "");
        Main.getInstance().getLogger().log(Level.WARNING, "An error occurred while loading the plugin: " + message);
        Main.getInstance().getLogger().log(Level.WARNING, "Disabling plugin...");
        Main.getInstance().getLogger().log(Level.WARNING, "");
        Main.getInstance().getLogger().log(Level.WARNING, "Thanks for choosing " + "Gennario's Development" + "...");
        Main.getInstance().getLogger().log(Level.WARNING, "-----------------------------------------------------------------------------------------");
    }

    public void checkVersions() throws IOException {
        if (resourceId == 0) return;

        String url = "https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=" + resourceId + "&key=version";
        if (checker == Checker.SPIGOTMC) {
            url = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=" + resourceId;
        }

        URLConnection con = new URL(url).openConnection();
        this.sitesVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        if (checker == Checker.SPIGOTMC) {
            sitesVersion = new JsonParser().parse(sitesVersion).getAsJsonObject().get("current_version").getAsString();
        }
    }

}
