package cz.gennario.gennarioframework.utils;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * It's a utility class that contains methods for generating random numbers, converting seconds to minutes, drawing arcs,
 * drawing lines, drawing circles, and calculating the Y value of a point on an arc
 */
@UtilityClass
public class MathUtils {

    private static final Random rand = new Random();


    /**
     * > Generate a random integer between min and max, inclusive
     *
     * @param min The minimum number that can be generated.
     * @param max The maximum number that can be generated.
     * @return A random integer between min and max.
     */
    public static int randomInt(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }


    /**
     * Returns a random boolean value.
     *
     * @return A random boolean value.
     */
    public static boolean randomBool() {
        return rand.nextBoolean();
    }


    /**
     * It returns a random float between 0 and 1.
     *
     * @return A random float between 0 and 1.
     */
    public static float randomFloat() {
        Random rnd = new Random();
        return rnd.nextFloat();
    }


    /**
     * It converts seconds to minutes and seconds.
     *
     * @param sec The number of seconds to convert to minutes.
     * @return The method returns a string that is the time in minutes and seconds.
     */
    public static String secToMin(int sec) {
        if (sec == 0) return "00:00";
        String s1 = Integer.toString(sec / 60);
        String s2 = Integer.toString(sec - (sec / 60) * 60);
        if (s1.length() == 1) s1 = "0" + s1;
        if (s2.length() == 1) s2 = "0" + s2;
        return s1 + ":" + s2;
    }


    /**
     * It draws a line between two points, then adds a height to each point based on the arc formula
     *
     * @param from The starting location
     * @param to The location to draw the arc to
     * @param points The amount of points you want to draw.
     * @param offset The height of the arc
     * @return A list of locations
     */
    public static List<Location> calculateArc(Location from, Location to, int points, double offset) {
        List<Location> locations = new ArrayList<>();

        Location toCopy = to.clone();
        toCopy.setY(from.getY());

        int point = 1;
        for (Location location : drawLine(from, toCopy, (from.distance(toCopy) / points))) {
            double height = calculateArcPointLocation(points, point, offset, from, to);
            locations.add(location.clone().add(0, height, 0));
            point++;
        }

        return locations;
    }

    /**
     * It calculates the Y value of a point on an arc
     *
     * @param points The amount of points in the arc.
     * @param point The current point in the arc
     * @param offset The offset of the arc.
     * @param from The location to start the arc from.
     * @param to The location to teleport to
     * @return The y-coordinate of the point on the arc.
     */
    private static double calculateArcPointLocation(int points, int point, double offset, Location from, Location to) {
        int originalPoint = point;
        boolean bigger = point > (points / 2);
        double maxY = (to.getY() - from.getY()) + offset;
        double jump = maxY / points;
        double finalY = (maxY / (points / 2)) * point;
        if (bigger) {
            point = (points / 2) - (point - (points / 2));
            finalY = ((offset / (points / 2)) * point) + (to.getY() - from.getY());
        }
        return finalY;
    }

    /**
     * It takes two locations, and returns a list of locations that are spaced out by a certain distance
     *
     * @param point1 The first point of the line
     * @param point2 The second point of the line.
     * @param space The distance between each point
     * @return A list of locations
     */
    public static List<Location> drawLine(Location point1, Location point2, double space) {
        List<Location> locations = new ArrayList<>();
        World world = point1.getWorld();
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            locations.add(new Location(world, p1.getX(), p1.getY(), p1.getZ()));
            length += space;
        }
        return locations;
    }

    /**
     * It draws a circle with the given radius, points, direction and type
     *
     * @param location The center of the circle
     * @param points The amount of points you want to draw.
     * @param radius The radius of the circle
     * @param direction The direction of the circle.
     * @param type The type of circle you want to draw.
     * @return A list of locations.
     */
    public static List<Location> drawCircle(Location location, int points, double radius, String direction, String type) {
        Location origin = location.clone();
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            if (direction.equals("RIGHT")) {
                Location point = null;
                if(type.equalsIgnoreCase("VERTICAL")) {
                    point = origin.clone().add(radius * Math.cos(angle), radius * Math.sin(angle),  0.0d);
                }else {
                    point = origin.clone().add(radius * Math.cos(angle), 0.0d, radius * Math.sin(angle));
                }
                locations.add(point);
            } else if (direction.equals("LEFT")) {
                Location point = null;
                if(type.equalsIgnoreCase("VERTICAL")) {
                    point = origin.clone().add(0.0d, radius * Math.sin(angle), radius * Math.cos(angle));
                }else {
                    point = origin.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
                }
                locations.add(point);
            }
        }
        return locations;
    }


    public static List<Location> calculatePath(Location start, double groundLevel, double gravity, double bounceFactor) {
        List<Location> path = new ArrayList<>();
        double velocityY = 0;
        double y = start.getY();

        // Pád k zemi
        while (y > groundLevel) {
            path.add(new Location(start.getWorld(), start.getX(), y, start.getZ()));
            velocityY -= gravity; // Simulace gravitace
            y += velocityY;
        }

        // Ujistíme se, že pád končí na groundLevel
        y = groundLevel;
        path.add(new Location(start.getWorld(), start.getX(), y, start.getZ()));

        // Simulace malého odrazu
        velocityY = -velocityY * bounceFactor; // Inverzní rychlost s tlumením
        while (velocityY > 0) {
            y += velocityY;
            path.add(new Location(start.getWorld(), start.getX(), y, start.getZ()));
            velocityY -= gravity; // Gravitace zpomaluje odraz
        }

        // Pád k zemi
        while (y > groundLevel) {
            path.add(new Location(start.getWorld(), start.getX(), y, start.getZ()));
            velocityY -= gravity; // Simulace gravitace
            y += velocityY;
        }

        // Ujistíme se, že pád končí na groundLevel
        y = groundLevel;
        path.add(new Location(start.getWorld(), start.getX(), y, start.getZ()));


        return path;
    }

    public List<Location> fallingPathWithDirection(Location start, double groundLevel, double yawDirection, double pushAmount) {
        List<Location> path = new ArrayList<>();

        double push = pushAmount;
        double velocityY = 0;
        boolean direction = true;

        Location clone = start.clone();
        clone.setYaw((float) yawDirection);
        clone.setPitch(0);

        while (clone.getY() > groundLevel) {

            Location prediction = clone.clone();
            prediction.add(prediction.getDirection().multiply(direction ? push : -push));
            prediction.setY(prediction.getY() - velocityY);
            if (!prediction.getBlock().getType().isAir()) {
                direction = !direction;
                prediction.add(prediction.getDirection().multiply(direction ? push : -push));
                prediction.setY(prediction.getY() - velocityY);
            } else {
                clone = prediction.clone();
            }

            if (velocityY <= 0) {
                velocityY += 0.04;
            }else {
                velocityY = velocityY * 1.3;
            }
            if (push > 0) {
                push = push * 0.9;
            }

            path.add(clone.clone());
        }

        return path;
    }
}
