package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.utils.config.Config;
import lombok.Data;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
@Data
public class DefaultFolderCreator {

    private Config config;

    public DefaultFolderCreator(JavaPlugin javaPlugin, String path, String name, InputStream resource) {
        Config config = new Config(javaPlugin, path, name, resource);
        config.setUpdate(false);
        try {
            config.loadIfNotExist();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.config = config;
    }

}
