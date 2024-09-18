package cz.gennario.gennarioframework.utils;

import lombok.Data;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;

@Data
public class PingerManager {

    private String ip;
    private int port;

    public PingerManager(String ip, int port) {
        if (!PlaceholderAPI.isRegistered("pinger")) {
            Bukkit.broadcastMessage("Pinger expansion missing! Use /papi ecloud download Pinger and reload the PAPI plugin!");
            return;
        }

        this.ip = ip;
        this.port = port;

        String s = PlaceholderAPI.setPlaceholders(null, "%pinger_online_" + ip + ":" + port + "%");
        if(s.contains("Offline") || s.contains("Error")) {
            throw new RuntimeException("Server is offline or an error occurred while pinging the server: "+ip+":"+port+"!");
        }
    }

    public boolean isOnline() {
        String s = PlaceholderAPI.setPlaceholders(null, "%pinger_online_" + ip + ":" + port + "%");
        return s.contains("Online");
    }

    public int getOnlinePlayers() {
        String s = PlaceholderAPI.setPlaceholders(null, "%pinger_players_" + ip + ":" + port + "%");
        return Integer.parseInt(s.split(" ")[1]);
    }

    public int getMaxPlayers() {
        String s = PlaceholderAPI.setPlaceholders(null, "%pinger_max_" + ip + ":" + port + "%");
        return Integer.parseInt(s.split(" ")[3]);
    }

    public String getMotd() {
        String s = PlaceholderAPI.setPlaceholders(null, "%pinger_motd_" + ip + ":" + port + "%");
        return s.split(" ")[1];
    }

    public String getVersion() {
        String s = PlaceholderAPI.setPlaceholders(null, "%pinger_version_" + ip + ":" + port + "%");
        return s.split(" ")[1];
    }

    public String getFullVersion() {
        return PlaceholderAPI.setPlaceholders(null, "%pinger_version_" + ip + ":" + port + "%");
    }

}
