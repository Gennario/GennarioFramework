package cz.gennario.gennarioframework.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.DecimalFormat;

public final class LocationUtils {

    public static Location getLocation(String s) {
        String[] splitted = s.replace(")", "").split("\\(");
        if (splitted.length == 2) {
            World world = Bukkit.getWorld(splitted[0]);

            String[] split = splitted[1].split(",");
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            double yaw = 0;
            double pitch = 0;
            if(split.length > 3) yaw = Double.parseDouble(split[3]);
            if(split.length > 4) pitch = Double.parseDouble(split[4]);

            return new Location(world, x, y, z, (float) yaw, (float) pitch);
        }
        return null;
    }
    public static String locationToString(Location location) {
        return locationToString(location, true, true);
    }

    public static String locationToString(Location location, boolean yaw, boolean pitch) {
        String loc = location.getWorld().getName() + "(";
        DecimalFormat format = new DecimalFormat("0.00");
        loc += format.format(location.getX()) + ",";
        loc += format.format(location.getY()) + ",";
        loc += format.format(location.getZ());
        if (yaw) if(location.getYaw() != 0) loc += "," + Math.round(location.getYaw());
        if (pitch) if(location.getPitch() != 0) loc += "," + Math.round(location.getPitch());
        loc += ")";
        return loc;
    }

    public static String locationToStringCenter(Location location) {
        return locationToStringCenter(location, true, true);
    }

    public static String locationToStringCenter(Location location, boolean yaw, boolean pitch) {
        String loc = location.getWorld().getName() + "(";
        DecimalFormat format = new DecimalFormat("0.00");
        loc += format.format((location.getBlock().getX() + 0.5)) + ",";
        loc += format.format(location.getY()) + ",";
        loc += format.format((location.getBlock().getZ()) + 0.5);
        if (yaw) if(location.getYaw() != 0) loc += "," + Math.round(location.getYaw());
        if (pitch) if(location.getPitch() != 0) loc += "," + Math.round(location.getPitch());
        loc += ")";
        return loc;
    }

}
