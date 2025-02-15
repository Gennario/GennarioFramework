package cz.gennario.gennarioframework.utils;

import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.OraxenItems;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

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


    public static Collection<ItemBuilder> getItems() {
        return OraxenItems.getItems();
    }

}
