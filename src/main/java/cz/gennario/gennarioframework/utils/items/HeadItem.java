package cz.gennario.gennarioframework.utils.items;

import com.cryptomorin.xseries.XMaterial;
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
import java.net.URL;
import java.util.Base64;
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

    public static ItemStack getSkullByTexture(String base64) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (base64 == null || base64.isEmpty() || base64.equals("none")) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();

        try {
            // Dekóduj base64 a extrahuj URL textury
            String decoded = new String(Base64.getDecoder().decode(base64));
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(decoded);
            JSONObject textures = (JSONObject) json.get("textures");
            JSONObject skin = (JSONObject) textures.get("SKIN");
            String skinUrl = (String) skin.get("url");

            // Použij moderní Bukkit API
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures playerTextures = profile.getTextures();
            playerTextures.setSkin(new URL(skinUrl));
            profile.setTextures(playerTextures);
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            // Fallback - pokus se použít URL přímo
            try {
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                PlayerTextures playerTextures = profile.getTextures();
                playerTextures.setSkin(new URL("http://textures.minecraft.net/texture/" + base64));
                profile.setTextures(playerTextures);
                meta.setOwnerProfile(profile);
            } catch (Exception ex) {
                System.out.println("§c§lFailed to set skull texture!");
            }
        }

        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack getSkullByUrl(String value) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (value == null || value.isEmpty() || value.equals("none")) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        try {
            PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures playerTextures = playerProfile.getTextures();
            playerTextures.setSkin(new URL("http://textures.minecraft.net/texture/" + value));
            playerProfile.setTextures(playerTextures);
            skullMeta.setOwnerProfile(playerProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            StringBuilder buffer = new StringBuilder();
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
