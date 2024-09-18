package cz.gennario.gennarioframework.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FileUtils {

    public static void createDirectory(JavaPlugin instance, String path) {
        new File(instance.getDataFolder(), path).mkdirs();
    }

    public static void createFile(JavaPlugin instance, String path) {
        File file = new File(instance.getDataFolder(), path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFile(JavaPlugin instance, String path) {
        File file = new File(instance.getDataFolder(), path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteDirectory(JavaPlugin instance, String path) {
        File file = new File(instance.getDataFolder(), path);
        if (file.exists()) {
            file.delete();
        }
    }

}
