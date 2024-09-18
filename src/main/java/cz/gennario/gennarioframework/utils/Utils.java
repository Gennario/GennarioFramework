package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.utils.centermessage.CenterMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    public static String colorize(String string) {
        return colorize(null, string);
    }

    public static String colorize(Player player, String string) {
        String playerName = "%player%";
        if (player != null && player.isOnline()) playerName = player.getName();
        string = string.replace("%player%", playerName);

        String s = string;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (player != null && player.isOnline()) {
                s = PlaceholderAPI.setPlaceholders(player, string);
            } else {
                s = PlaceholderAPI.setPlaceholders(null, string);
            }
        }

        s = s.replace("&", "§");
        try {
            if(!s.isEmpty()) s = ColorAPI.formatHexColor(s);
        }catch (Exception ignored){}

        if (s.startsWith("<center>")) {
            s = CenterMessage.getCenteredMessage(s.replaceFirst("<center>", ""));
        }

        return s;
    }

    public static String[] colorize(Player player, String... strings) {
        List<String> list = new ArrayList<>();
        for (String string : strings) {
            String playerName = "%player%";
            if (player != null && player.isOnline()) playerName = player.getName();
            string = string.replace("%player%", playerName);

            String s = string;
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                if (player != null && player.isOnline()) {
                    s = PlaceholderAPI.setPlaceholders(player, string);
                } else {
                    s = PlaceholderAPI.setPlaceholders(null, string);
                }
            }
            s = s.replace("&", "§");
            try {
                if(!s.isEmpty()) s = ColorAPI.formatHexColor(s);
            }catch (Exception ignored){}

            if (s.startsWith("<center>")) {
                s = CenterMessage.getCenteredMessage(s.replaceFirst("<center>", ""));
            }
            list.add(s);
        }

        return list.toArray(new String[0]);
    }

    public static List<String> colorize(Player player, List<String> strings) {
        List<String> list = new ArrayList<>();
        for (String string : strings) {
            String playerName = "%player%";
            if (player != null && player.isOnline()) playerName = player.getName();
            string = string.replace("%player%", playerName);

            String s = string;
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                if (player != null && player.isOnline()) {
                    s = PlaceholderAPI.setPlaceholders(player, string);
                } else {
                    s = PlaceholderAPI.setPlaceholders(null, string);
                }
            }
            s = s.replace("&", "§");
            try {
                if(!s.isEmpty()) s = ColorAPI.formatHexColor(s);
            }catch (Exception ignored){}

            if (s.startsWith("<center>")) {
                s = CenterMessage.getCenteredMessage(s.replaceFirst("<center>", ""));
            }
            list.add(s);
        }

        return list;
    }

    public static String getMinecraftVersion(Server server) {
        String version = server.getVersion();
        int start = version.indexOf("MC: ") + 4;
        int end = version.length() - 1;
        return version.substring(start, end);
    }

    public static boolean isOldVersion() {
        return Integer.parseInt(Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.")[1]) < 13;
    }

    public static boolean versionIs(int version) {
        return Integer.parseInt(Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.")[1]) == version;
    }

    public static boolean versionIsAfter(int version) {
        return Integer.parseInt(Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.")[1]) > version;
    }

    public static boolean versionIsBefore(int version) {
        return Integer.parseInt(Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.")[1]) < version;
    }

    public static boolean versionIsBeforeOrEqual(int version) {
        return Integer.parseInt(Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.")[1]) <= version;
    }

    public static boolean versionIsBeforeOrEqual(int version, int update) {
        String[] split = Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.");
        if (split[1].equals(version)) {
            if (split.length == 3) {
                return Integer.parseInt(split[1]) <= version && Integer.parseInt(split[2]) <= update;
            } else return Integer.parseInt(split[1]) <= version;
        } else return Integer.parseInt(split[1]) <= version;
    }

    public static boolean versionIsAfterOrEqual(int version) {
        return Integer.parseInt(Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.")[1]) >= version;
    }

    public static boolean versionIsAfterOrEqual(int version, int update) {
        String[] split = Utils.getMinecraftVersion(Bukkit.getServer()).split("\\.");
        return Integer.parseInt(split[1]) >= version && Integer.parseInt(split[2]) >= update;
    }

    public static boolean valueExist(StringReader reader, String value) {
        return reader.getDataByValue(value) != null;
    }

    public static String getData(StringReader reader, String value) {
        return reader.getDataByValue(value);
    }
}
