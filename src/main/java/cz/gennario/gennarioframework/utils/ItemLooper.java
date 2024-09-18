package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemLooper implements Listener {

    private Inventory inventory;
    private Player player;
    private ItemLooperCallback callback;

    public interface ItemLooperCallback {
        void onItemLoop(List<ItemStack> items);
    }

    public ItemLooper(Player player, ItemLooperCallback callback) {
        this.player = player;
        this.callback = callback;

        inventory = Bukkit.createInventory(player, 3*9, "Insert items");

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

        player.openInventory(inventory);
    }

    public void unregisterListener() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() == player) {
            if (event.getInventory().equals(inventory)) {
                List<ItemStack> items = new ArrayList<>();

                for (ItemStack content : inventory.getContents()) {
                    if (content != null && content.getType() != Material.AIR) {
                        items.add(content);
                        player.getInventory().addItem(content);
                    }
                }

                unregisterListener();

                callback.onItemLoop(items);
            }
        }
    }

}
