package cz.gennario.gennarioframework.utils;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NumberFormatUtil {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(double value) {
        if (value < 1000) return String.valueOf(value); // čísla menší než 1000 zůstanou beze změny

        Map.Entry<Long, String> e = suffixes.floorEntry((long)Math.abs(value));
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        double truncated = value / divideBy; // část čísla pro zkrácení
        boolean hasDecimal = String.valueOf(truncated).contains("."); // zjistí, zda číslo obsahuje desetinnou část

        if (hasDecimal) {
            String s = String.valueOf(truncated).split("\\.")[1];
            char[] chars = s.toCharArray();
            if(chars.length > 1 && !Character.toString(chars[1]).equals("0")) {
                return String.format("%.2f%s", truncated, suffix);
            }else if (chars.length > 0 && !Character.toString(chars[0]).equals("0")) {
                return String.format("%.1f%s", truncated, suffix);
            }else {
                return String.format("%.0f%s", truncated, suffix);
            }
        } else {
            return String.format("%.0f%s", truncated, suffix);
        }
    }

}
