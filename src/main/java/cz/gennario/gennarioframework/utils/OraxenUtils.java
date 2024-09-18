package cz.gennario.gennarioframework.utils;

import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public final class OraxenUtils {

    public static ItemStack getItem(String id) {
        return OraxenItems.getItemById(id).build();
    }

    public static boolean isAnItem(String itemId) {
        return OraxenItems.exists(itemId);
    }

    public static boolean isAnItem(ItemStack item) {
        return OraxenItems.exists(item);
    }

    public static void placeBlock(String id, Location location) {
        OraxenBlocks.place(id, location);
    }

    public static void placeFurniture(String id, Location location, Rotation rotation, BlockFace blockFace) {
        OraxenFurniture.place(id, location, rotation, blockFace);
    }

}
