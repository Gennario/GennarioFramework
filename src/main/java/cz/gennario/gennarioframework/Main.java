package cz.gennario.gennarioframework;

import cz.gennario.gennarioframework.utils.DefaultFolderCreator;
import cz.gennario.gennarioframework.utils.PluginUpdater;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.config.Config;
import cz.gennario.gennarioframework.utils.cooldown.CooldownUtil;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private static Main instance;
    private boolean versionAdapter = false;

    private Config configFile;
    private CooldownUtil cooldown;

    private PluginUpdater pluginUpdater;

    @Override
    public void onEnable() {
        instance = this;
        pluginUpdater = new PluginUpdater(0, this, PluginUpdater.Checker.POLYMART);

        DefaultFolderCreator config = new DefaultFolderCreator(this, "", "config", getResource("config.yml"));
        configFile = config.getConfig();

        PacketUtils.init();
        checkVersionAdapter();

        pluginUpdater.sendLoadMessage();
    }

    @Override
    public void onDisable() {
    }

    public static Main getInstance() {
        return instance;
    }

    public void checkVersionAdapter() {
        String string = configFile.getYamlDocument().getString("version-adapter");
        if (string.equalsIgnoreCase("AUTO")) {
            if (Utils.versionIsBefore(20)) {
                versionAdapter = true;
                Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Auto detected version before 1.20 (OLD METHOD)");
                Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Change it in config.yml to NEW");
            } else {
                versionAdapter = false;
                Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Auto detected version 1.20 and newer (NEW METHOD)");
                Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Change it in config.yml to OLD");
            }
        } else if (string.equalsIgnoreCase("OLD")) {
            versionAdapter = true;
            Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7OLD method applied (before 1.20)");
            Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Invisible packet displays? Change it in config.yml to NEW");
        } else if (string.equalsIgnoreCase("NEW")) {
            versionAdapter = false;
            Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7NEW method applied (1.20 and newer)");
            Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Invisible packet displays? Change it in config.yml to OLD");
        }
    }
}
