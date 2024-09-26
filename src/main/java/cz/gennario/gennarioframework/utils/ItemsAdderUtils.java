package cz.gennario.gennarioframework.utils;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;

import java.util.List;

public final class ItemsAdderUtils {

    public static boolean areItemsLoaded() {

        return ItemsAdder.areItemsLoaded();
    }

    public static List<CustomStack> getItems() {
        return ItemsAdder.getAllItems();
    }

}
