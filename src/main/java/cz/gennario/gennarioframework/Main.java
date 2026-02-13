package cz.gennario.gennarioframework;

import cz.gennario.gennarioframework.test.ResolutionSetupUtil;
import cz.gennario.gennarioframework.utils.*;
import cz.gennario.gennarioframework.utils.config.Config;
import cz.gennario.gennarioframework.utils.cooldown.CooldownUtil;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.types.display.types.PacketItemDisplay;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

@Getter
public final class Main extends JavaPlugin {

    private static Main instance;
    private boolean versionAdapter = false;

    private Config configFile;
    private CooldownUtil cooldown;

    private PluginUpdater pluginUpdater;

    private ColorFormat colorFormat;

    @Override
    public void onEnable() {
        instance = this;
        pluginUpdater = new PluginUpdater(0, this, PluginUpdater.Checker.POLYMART);

        Config config = new Config(this, "", "config", getResource("config.yml"));
        config.setUpdate(true);
        try {
            config.load();
            configFile = config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        YamlDocument yamlDocument = configFile.getYamlDocument();
        String string = yamlDocument.getString("color-format");
        colorFormat = ColorFormat.valueOf(string);
        if (colorFormat == null) {
            Bukkit.getConsoleSender().sendMessage("§c[GennarioFramework] §7Invalid color format in config.yml! Defaulting to GENNARIO_FORMAT");
            colorFormat = ColorFormat.GENNARIO_FORMAT;
        }

        PacketUtils.init();
        checkVersionAdapter();

        pluginUpdater.sendLoadMessage();

        new LocationCommand(this);
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
