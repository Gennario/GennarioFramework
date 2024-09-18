package cz.gennario.gennarioframework.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConditionUtils {

    public static boolean checkCondition(Player player, String condition) {
        if (condition.startsWith("permission:")) {
            return player.hasPermission(condition.replace("permission:", ""));
        } else if (condition.startsWith("world:")) {
            return player.getWorld().getName().equalsIgnoreCase(condition.replace("world:", ""));
        } else {
            String regex = "^(.*)(==Aa|==|>=|<=|>|<|!=Aa|!=)(\\d+(\\.\\d+)?)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(condition);

            if (matcher.matches()) {
                String input = PlaceholderAPI.setPlaceholders(player, matcher.group(1));
                String type = matcher.group(2);
                String output = matcher.group(3);

                switch (type) {
                    case "==Aa":
                        return input.equalsIgnoreCase(output);
                    case "==":
                        return input.equals(output);
                    case ">=":
                        return Double.parseDouble(input) >= Double.parseDouble(output);
                    case "<=":
                        return Double.parseDouble(input) <= Double.parseDouble(output);
                    case ">":
                        return Double.parseDouble(input) > Double.parseDouble(output);
                    case "<":
                        return Double.parseDouble(input) < Double.parseDouble(output);
                    case "!=":
                        return !input.equals(output);
                    case "!=Aa":
                        return !input.equalsIgnoreCase(output);
                }
            }
        }
        return false;
    }

}
