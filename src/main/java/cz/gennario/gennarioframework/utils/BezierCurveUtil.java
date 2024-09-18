package cz.gennario.gennarioframework.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public final class BezierCurveUtil {

    public static List<Location> bezierCurveDisplay(Location playerLocation, Location targetLocation, double offset, int amount) {
        Location l = playerLocation.clone();
        Location upperPoint = l.add(l.getDirection().multiply(playerLocation.distance(targetLocation) / 2));
        upperPoint.setPitch(upperPoint.getPitch()-90);
        Vector multiply = upperPoint.getDirection().multiply(offset);
        upperPoint.add(multiply);
        return bezierCurveDisplay(amount, playerLocation, targetLocation, upperPoint);
    }

    public static List<Location> bezierCurveDisplay2(Location playerLocation, Location targetLocation, double offset, int amount) {
        List<Location> locations = MathUtils.drawLine(playerLocation, targetLocation, (playerLocation.distance(targetLocation) / 2)-0.1);
        Location upperPoint = locations.get(1);
        upperPoint.add(0, offset, 0);
        return bezierCurveDisplay(amount, playerLocation, targetLocation, upperPoint);
    }

    public static List<Location> bezierCurveDisplay(int amount, Location playerLocation, Location targetLocation, Location upperPoint) {
        Location p0 = playerLocation;
        Location p1 = upperPoint;
        Location p2 = targetLocation; //these 3 locations were debugged and are valid.
        return bezierCurve(amount, p0, p1, p2);
    }


    public static Location bezierPoint(float t, Location p0, Location p1, Location p2) {
        float a = (1 - t) * (1 - t);
        float b = 2 * (1 - t) * t;
        float c = t * t;

        Location p = p0.clone().multiply(a).add(p1.clone().multiply(b)).add(p2.clone().multiply(c));
        //System.out.println(p);
        return p;
    }

    public static List<Location> bezierCurve(int segmentCount, Location p0, Location p1, Location p2) {
        List<Location> points = new ArrayList<Location>();
        for (int i = 1; i < segmentCount; i++) {
            float t = i / (float) segmentCount;
            points.add(bezierPoint(t, p0, p1, p2));
        }
        return points;
    }

}