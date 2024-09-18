package cz.gennario.gennarioframework.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class OutlineUtils {

    public static List<Location> calculateOutline(Block block, double space) {
        Location clone = block.getLocation().clone();
        List<Location> locations = new ArrayList<>();

        locations.addAll(MathUtils.drawLine(clone, clone.clone().add(1, 0, 0), space));
        locations.addAll(MathUtils.drawLine(clone, clone.clone().add(0, 1, 0), space));
        locations.addAll(MathUtils.drawLine(clone, clone.clone().add(0, 0, 1), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(1, 1, 0), clone.clone().add(1, 0, 0), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(1, 1, 0), clone.clone().add(1, 1, 1), space));

        locations.addAll(MathUtils.drawLine(clone.clone().add(1, 1, 0), clone.clone().add(0, 1, 0), space));

        locations.addAll(MathUtils.drawLine(clone.clone().add(0, 1, 1), clone.clone().add(0, 0, 1), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(0, 1, 1), clone.clone().add(1, 1, 1), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(0, 1, 1), clone.clone().add(0, 1, 0), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(1, 0, 1), clone.clone().add(0, 0, 1), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(1, 0, 1), clone.clone().add(1, 1, 1), space));
        locations.addAll(MathUtils.drawLine(clone.clone().add(1, 0, 1), clone.clone().add(1, 0, 0), space));

        return locations;
    }

    public static void connectOutLinesList(List<Location> outlines) {
        List<Location> locations = new ArrayList<>();
        for (Location outline : outlines) {
            boolean canCreate = true;
            for (Location location : locations) {
                if (outline.distance(location) <= 0.15) {
                    canCreate = false;
                    break;
                }
            }
            if(canCreate) locations.add(outline);
        }
        outlines.clear();
        outlines.addAll(locations);
    }

}
