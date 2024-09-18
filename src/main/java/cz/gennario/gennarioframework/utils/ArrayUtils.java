package cz.gennario.gennarioframework.utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    String splitSymbol = "|Äß|";

    public static List<String> stringToArray(String string) {
        if (string.equals("none")) return new ArrayList<>();
        if(!string.contains("|Äß|")) {
            return List.of(string);
        }
        String[] split = string.split("\\|Äß\\|");
        return List.of(split);
    }

    public static String arrayToString(List<String> array) {
        if (array.isEmpty()) return "none";
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String string : array) {
            i++;
            builder.append(string);
            if(i <= array.size() - 1) {
                builder.append("|Äß|");
            }
        }
        return builder.toString();
    }

    public static List<Object> stringToArray(String string, ArrayConvertor convertor) {
        if(string.equals("none")) return new ArrayList<>();
        if(!string.contains("|Äß|")) {
            return List.of(convertor.convert(string));
        }
        String[] split = string.split("\\|Äß\\|");
        System.out.println(split);
        ArrayList<Object> array = new ArrayList<>();
        for (String string1 : split) {
            array.add(convertor.convert(string1));
        }
        return array;
    }

    public static String arrayToString(ArrayList<Object> array, ArrayConvertor convertor) {
        if(array.isEmpty()) return "none";
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Object object : array) {
            i++;
            builder.append(convertor.convertModel(object));
            if(i <= array.size() - 1) {
                builder.append("|Äß|");
            }
        }
        return builder.toString();
    }

    public abstract static class ArrayConvertor<T> {
        public abstract T convert(String string);
        public abstract String convertModel(T model);
    }

}
