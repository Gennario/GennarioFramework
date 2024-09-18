package cz.gennario.gennarioframework.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
        String loc = location.getWorld().getName() + "(";
        loc += location.getX() + ",";
        loc += location.getY() + ",";
        loc += location.getZ();
        if(location.getYaw() != 0) loc += "," + location.getYaw();
        if(location.getPitch() != 0) loc += "," + location.getPitch();
        loc += ")";
        return loc;
    }

    public static String locationToStringCenter(Location location) {
        String loc = location.getWorld().getName() + "(";
        loc += (location.getX() + 0.5) + ",";
        loc += location.getY() + ",";
        loc += (location.getZ() + 0.5);
        if(location.getYaw() != 0) loc += "," + location.getYaw();
        if(location.getPitch() != 0) loc += "," + location.getPitch();
        loc += ")";
        return loc;
    }

}
