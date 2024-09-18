package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.utils.replacement.ReplacementPackage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ActionUtils {

    public static void runActions(Player player, ReplacementPackage replacement, String... actions) {
        for (String action : actions) {
            runAction(action, player, replacement);
        }
    }

    public static void runActions(Player player, String... actions) {
        for (String action : actions) {
            runAction(action, player, new ReplacementPackage());
        }
    }

    public static void runActions(Player player, List<String> actions) {
        for (String action : actions) {
            runAction(action, player, new ReplacementPackage());
        }
    }

    public static void runAction(String action, Player player, ReplacementPackage replacement) {
        String regex = "^([^()]+)\\(([^)]+)\\)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(action);

        ReplacementPackage replacementPackage = new ReplacementPackage()
                .append("%ahoj%", 40)
                .append("%player%", Player::getName)
                .append("%player_ip%", player1 -> player1.getAddress().toString());
        replacementPackage.replace(player, "tvuj text bude tady laky");

        if (matcher.matches()) {
            String actionType = matcher.group(1).toLowerCase();
            String actionValue = Utils.colorize(player, replacement.replace(player, matcher.group(2)));

            switch (actionType) {
                case "message":
                    player.sendMessage(actionValue);
                    break;
                case "broadcast":
                    Bukkit.broadcastMessage(actionValue);
                    break;
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), actionValue);
                    break;
                case "player":
                    player.performCommand(actionValue);
                    break;
                case "player-op":
                    boolean hasOp = player.isOp();
                    player.setOp(true);
                    player.performCommand(actionValue);
                    if (!hasOp) player.setOp(false);
                    break;
                case "sound":
                    String[] splitted = actionValue.split("//");
                    if (splitted.length == 1) {
                        player.playSound(player.getLocation(), splitted[0], 100, 1);
                    } else if (splitted.length == 2) {
                        player.playSound(player.getLocation(), splitted[0], Float.parseFloat(splitted[1]), 1);
                    } else if (splitted.length == 3) {
                        player.playSound(player.getLocation(), splitted[0], Float.parseFloat(splitted[1]), Float.parseFloat(splitted[2]));
                    }
                    break;
                case "title":
                    splitted = actionValue.split("//");
                    if (splitted.length == 1) {
                        player.sendTitle(Utils.colorize(player, splitted[0]), "", 10, 70, 20);
                    } else if (splitted.length == 2) {
                        player.sendTitle(Utils.colorize(player, splitted[0]), Utils.colorize(player, splitted[1]), 10, 70, 20);
                    } else if (splitted.length == 3) {
                        player.sendTitle(Utils.colorize(player, splitted[0]), Utils.colorize(player, splitted[1]), Integer.parseInt(splitted[2]), 70, 20);
                    } else if (splitted.length == 4) {
                        player.sendTitle(Utils.colorize(player, splitted[0]), Utils.colorize(player, splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), 20);
                    } else if (splitted.length == 5) {
                        player.sendTitle(Utils.colorize(player, splitted[0]), Utils.colorize(player, splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]));
                    }
                    break;
            }
        }
    }

}
