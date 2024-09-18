package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class MessagesUtil {

    public enum MessageType {
        INFO("<#ed1cc3>ℹ", "<#f8d3ff>"),
        SUCCESS("<#ed1cc3>✔", "<#f8d3ff>"),
        DENY("<#ed1cc3>❌", "<#f8d3ff>"),
        ERROR("<#ff392b>⛔", "<#ffc1bd>"),
        WARNING("<#ff392b>⚠", "<#ffc1bd>"),
        TIP("<#ed1cc3>\uD83D\uDCA1", "<#f8d3ff>"),
        UNKNOWN("<#ed1cc3>?", "<#f8d3ff>");

        @Getter
        private String name;
        @Getter
        private String secondaryColor;

        MessageType(String name, String secondaryColor) {
            this.name = name;
            this.secondaryColor = secondaryColor;
        }
    }

    public static void sendMessage(Object receiver, MessageType messageType, String path) {
        sendMessage(receiver, messageType, path, new ReplacementPackage());
    }

    public static void sendMessage(Object receiver, MessageType messageType, String path, ReplacementPackage replacement) {
        if(receiver instanceof Player player) {
            List<String> messageForPlayer = new ArrayList<>();
            for (String s : messageForPlayer) {
                player.sendMessage(Utils.colorize(player, ("§f"+messageType.getName()+" §8| §f"+replacement.replace(player, s)).replace("%c%", messageType.secondaryColor)));
            }
        }
    }

}
