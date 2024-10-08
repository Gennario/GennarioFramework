package cz.gennario.gennarioframework.utils.commands;

import cz.gennario.gennarioframework.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandArg {

    private final CommandAPI api;
    private final String arg;

    public CommandArg(CommandAPI api, String arg) {
        this.arg = arg;
        this.api = api;
    }

    public boolean isInt() {
        try {
            int i = Integer.parseInt(arg);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean isDouble() {
        try {
            double i = Double.parseDouble(arg);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean isFloat() {
        try {
            float i = Float.parseFloat(arg);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean isLong() {
        try {
            long i = Long.parseLong(arg);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean isPlayer() {
        return Bukkit.getPlayer(arg) != null;
    }

    public boolean isOfflinePlayer() {
        if (Bukkit.getPlayer(arg) != null) {
            return true;
        }else if(Bukkit.getOfflinePlayer(arg).hasPlayedBefore()) {
            return true;
        }
        return false;
    }

    public boolean isEntity() {
        return EntityType.fromName(arg.toUpperCase()) != null;
    }

    public boolean isMaterial() {
        try {
            Material.valueOf(arg.toUpperCase());
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }


    public String getAsString() {
        return arg;
    }

    public int getAsInt() {
        return Integer.parseInt(arg);
    }

    public double getAsDouble() {
        return Double.parseDouble(arg);
    }

    public float getAsFloat() {
        return Float.parseFloat(arg);
    }

    public long getAsLong() {
        return Long.parseLong(arg);
    }

    public Player getAsPlayer() {
        return Bukkit.getPlayer(arg);
    }

    public OfflinePlayer getAsOfflinePlayer() {
        return Bukkit.getOfflinePlayer(arg);
    }

    public EntityType getAsEntity() {
        return EntityType.fromName(arg.toUpperCase());
    }

    public Material getAsMaterial() {
        return Material.valueOf(arg.toUpperCase());
    }

    public Location getAsLocation() {
        return LocationUtils.getLocation(arg);
    }

}
