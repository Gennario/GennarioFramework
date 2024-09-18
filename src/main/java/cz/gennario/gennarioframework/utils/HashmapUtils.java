package cz.gennario.gennarioframework.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class HashmapUtils {

    // Method to load a map from a string with generic types
    public static <K, V> Map<K, V> loadFromString(String string, Class<K> keyClass, Class<V> valueClass) {
        if (string == null || string.isEmpty() || string.equals("none")) {
            return new HashMap<>();
        }

        JSONObject jsonObject = new JSONObject(string);
        JSONArray list = jsonObject.getJSONArray("list");

        Map<K, V> map = new HashMap<>();

        for (Object o : list) {
            JSONObject object = (JSONObject) o;

            K key = keyClass.cast(object.get("key"));
            Object rawValue = object.get("value");

            V value;
            if (valueClass == Long.class && rawValue instanceof Integer) {
                value = valueClass.cast(((Integer) rawValue).longValue());  // Přetypování z Integer na Long
            } else {
                value = valueClass.cast(rawValue);
            }

            map.put(key, value);
        }

        return map;
    }

    // Method to convert a map to a string with generic types
    public static <K, V> String mapToString(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return "none";
        }

        JSONArray list = new JSONArray();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("key", entry.getKey());
            object.put("value", entry.getValue());
            list.put(object);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", list);

        return jsonObject.toString();
    }
}
