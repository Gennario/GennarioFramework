package cz.gennario.gennarioframework.utils;

import cz.gennario.gennarioframework.utils.config.Config;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class InventoryBackupUtil implements Listener {
    private static final Map<UUID, ItemStack[]> inventoryCache = new HashMap<>();
    private final Config configFile;
    private final YamlDocument config;

    public InventoryBackupUtil(JavaPlugin plugin, String fileName) {
        this.configFile = new Config(plugin, "", fileName, plugin.getResource(fileName + ".yml"));
        try {
            configFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.config = configFile.getYamlDocument();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void saveInventory(Player player) {
        UUID uuid = player.getUniqueId();
        inventoryCache.put(uuid, player.getInventory().getContents());

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) {
                config.set("inventories." + uuid.toString() + "." + i, ItemSerializer.itemStackToString(item));
            }
        }

        saveConfig();
        player.getInventory().clear();
    }

    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        if (inventoryCache.containsKey(uuid)) {
            player.getInventory().setContents(inventoryCache.get(uuid));
            inventoryCache.remove(uuid);
            config.remove("inventories." + uuid.toString());

            saveConfig();
        } else if (config.contains("inventories." + uuid.toString())) {
            Section section = config.getSection("inventories." + uuid.toString());
            for (String routesAsString : section.getRoutesAsStrings(false)) {
                int slot = Integer.parseInt(routesAsString);
                ItemStack item = ItemSerializer.stringToItemStack(section.getString(routesAsString));
                player.getInventory().setItem(slot, item);
            }
            config.remove("inventories." + uuid.toString());

            saveConfig();
        }
    }

    public void removePlayerFromCache(Player player) {
        inventoryCache.remove(player.getUniqueId());
    }

    public void clearCache() {
        inventoryCache.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        restoreInventory(event.getPlayer());
    }

    private void saveConfig() {
        try {
            configFile.getYamlDocument().save();
        } catch (IOException e) {
            Bukkit.getLogger().severe("[MyPlugin] Could not save inventory file!");
        }
    }
}
