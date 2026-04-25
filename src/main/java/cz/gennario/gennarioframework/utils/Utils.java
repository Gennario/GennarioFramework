package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.centermessage.CenterMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    private record VersionParts(int major, int update) {
    }

    private static VersionParts getVersionParts(Server server) {
        String[] split = getMinecraftVersion(server).split("\\.");

        int first = parseIntSafe(split, 0);
        // Legacy format: 1.21.1 -> major=21, update=1
        if (first == 1 && split.length > 1) {
            return new VersionParts(parseIntSafe(split, 1), parseIntSafe(split, 2));
        }

        // New format: 26.1.2 -> major=26, update=1
        return new VersionParts(first, parseIntSafe(split, 1));
    }

    private static int parseIntSafe(String[] split, int index) {
        if (index < 0 || index >= split.length) {
            return 0;
        }

        try {
            return Integer.parseInt(split[index]);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

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
            if(!s.isEmpty()) {
                switch (Main.getInstance().getColorFormat()) {
                    case GENNARIO_FORMAT -> s = ColorAPI.formatHexColor(s);
                    case LEGACY -> s = s.replace("&", "§");
                    case MINIMESSAGE -> {
                        MiniMessage mm = MiniMessage.miniMessage();
                        Component component = mm.deserialize(s);
                        s = LegacyComponentSerializer.legacySection().serialize(component);
                    }
                }
            }
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
                switch (Main.getInstance().getColorFormat()) {
                    case GENNARIO_FORMAT -> s = ColorAPI.formatHexColor(s);
                    case LEGACY -> s = s.replace("&", "§");
                    case MINIMESSAGE -> {
                        MiniMessage mm = MiniMessage.miniMessage();
                        Component component = mm.deserialize(s);
                        s = LegacyComponentSerializer.legacySection().serialize(component);
                    }
                }
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
                switch (Main.getInstance().getColorFormat()) {
                    case GENNARIO_FORMAT -> s = ColorAPI.formatHexColor(s);
                    case LEGACY -> s = s.replace("&", "§");
                    case MINIMESSAGE -> {
                        MiniMessage mm = MiniMessage.miniMessage();
                        Component component = mm.deserialize(s);
                        s = LegacyComponentSerializer.legacySection().serialize(component);
                    }
                }
            }catch (Exception ignored){}

            if (s.startsWith("<center>")) {
                s = CenterMessage.getCenteredMessage(s.replaceFirst("<center>", ""));
            }
            list.add(s);
        }

        return list;
    }

    public String parsePlaceholderAPI(String string, Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (player != null && player.isOnline()) {
                return PlaceholderAPI.setPlaceholders(player, string);
            } else {
                return PlaceholderAPI.setPlaceholders(null, string);
            }
        }
        return string;
    }

    public static String getMinecraftVersion(Server server) {
        String version = server.getVersion();
        int start = version.indexOf("MC: ") + 4;
        int end = version.length() - 1;
        return version.substring(start, end);
    }

    public static boolean isOldVersion() {
        return getVersionParts(Bukkit.getServer()).major() < 13;
    }

    public static boolean versionIs(int version) {
        return getVersionParts(Bukkit.getServer()).major() == version;
    }

    public static boolean versionIsAfter(int version) {
        return getVersionParts(Bukkit.getServer()).major() > version;
    }

    public static boolean versionIsBefore(int version) {
        return getVersionParts(Bukkit.getServer()).major() < version;
    }

    public static boolean versionIsBeforeOrEqual(int version) {
        return getVersionParts(Bukkit.getServer()).major() <= version;
    }

    public static boolean versionIsBeforeOrEqual(int version, int update) {
        VersionParts parts = getVersionParts(Bukkit.getServer());
        if (parts.major() < version) return true;
        if (parts.major() > version) return false;
        return parts.update() <= update;
    }

    public static boolean versionIsAfterOrEqual(int version) {
        return getVersionParts(Bukkit.getServer()).major() >= version;
    }

    public static boolean versionIsAfterOrEqual(int version, int update) {
        VersionParts parts = getVersionParts(Bukkit.getServer());
        if (parts.major() > version) return true;
        if (parts.major() < version) return false;
        return parts.update() >= update;
    }

    public static boolean valueExist(StringReader reader, String value) {
        return reader.getDataByValue(value) != null;
    }

    public static String getData(StringReader reader, String value) {
        return reader.getDataByValue(value);
    }
}
