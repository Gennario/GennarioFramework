package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.utils.config.Config;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@Getter
public class FolderLooper {

    private ArrayList<YamlDocument> finalList = new ArrayList<>();
    private JavaPlugin plugin;

    public FolderLooper(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        try {
            loopFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loopFile(File file) throws IOException {
        if (file.isDirectory()) {
            for (File file1 : file.listFiles()) {
                loopFile(file1);
            }
        } else {
            if (file.getName().endsWith(".yml")) {
                Config config = new Config(plugin, file.getPath().replace(plugin.getDataFolder().getPath(), ""));
                config.setUpdate(false);
                config.load();
                finalList.add(config.getYamlDocument());
            }
        }
    }

}