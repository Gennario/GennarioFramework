package cz.gennario.gennarioframework.utils.items;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HeadItem {

    /**
     * Generation head type enum
     */
    public enum HeadType {
        PLAYER_HEAD,
        BASE64
    }

    /**
     * With this method you can get a player's head by nickname or a base64 head by base64 code
     *
     * @param type  Determines whether you want to get the head by name or by base64
     * @param value If you want a player's head, then the player's name. If you want base64, then base64 code.
     * @return Head itemStack
     */
    public static ItemStack convert(HeadType type, String value) {
        String texture = getPlayerHeadTexture(value);
        if (type.equals(HeadType.PLAYER_HEAD)) {
            return getSkullByTexture(texture);
        } else {
            return getSkullByTexture(value);
        }
    }

    public static ItemStack getSkullByTexture(String value) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (value.isEmpty() || value.equals("none")) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();

        try {
            Method setProfile = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            setProfile.setAccessible(true);

            GameProfile profile = new GameProfile(UUID.randomUUID(), "skull-texture");
            profile.getProperties().put("textures", new Property("textures", value));

            setProfile.invoke(meta, profile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", value));
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException oldExe) {
                System.out.println("§c§lBase64 not found!");
            }
        }

        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack getSkullByUrl(String value) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (value.isEmpty() || value.equals("none")) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures playerTextures = playerProfile.getTextures();
        try {
            playerTextures.setSkin(new URL("http://textures.minecraft.net/texture/" + value));
        } catch (MalformedURLException e) {e.printStackTrace();}
        playerProfile.setTextures(playerTextures);
        skullMeta.setOwnerProfile(playerProfile);
        head.setItemMeta(skullMeta);

        return head;
    }

    public static String getPlayerHeadTexture(String username) {
        if (getPlayerId(username).equals("none")) return "none";
        String url = "https://api.minetools.eu/profile/" + getPlayerId(username);
        try {
            JSONParser jsonParser = new JSONParser();
            String userData = readUrl(url);
            Object parsedData = jsonParser.parse(userData);

            JSONObject jsonData = (JSONObject) parsedData;
            JSONObject decoded = (JSONObject) jsonData.get("raw");
            JSONArray textures = (JSONArray) decoded.get("properties");
            JSONObject data = (JSONObject) textures.get(0);

            return data.get("value").toString();
        } catch (Exception ex) {
            return "none";
        }
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }


    private static String getPlayerId(String playerName) {
        try {
            String url = "https://api.minetools.eu/uuid/" + playerName;
            JSONParser jsonParser = new JSONParser();
            String userData = readUrl(url);
            Object parsedData = jsonParser.parse(userData);

            JSONObject jsonData = (JSONObject) parsedData;

            if (jsonData.get("id") != null) return jsonData.get("id").toString();
            return "";
        } catch (Exception ex) {
            return "none";
        }
    }
}
