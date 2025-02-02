package cz.gennario.gennarioframework.utils;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Data
public abstract class MinigameAdapter implements Listener {

    private final JavaPlugin plugin;
    private final InventoryBackupUtil inventoryBackupUtil;
    private final Map<UUID, Location> previousLocations = new HashMap<>();
    private final Set<UUID> activePlayers = new HashSet<>();
    private final GameMode mode;
    private final Location[] gameSpawns;

    private MinigameEvents minigameEvents;
    private Cuboid gameZone;

    public enum GameMode {
        SINGLE_GAME, MULTIPLAYER
    }

    public MinigameAdapter(JavaPlugin plugin, InventoryBackupUtil inventoryBackupUtil, GameMode mode, Location... gameSpawns) {
        this.plugin = plugin;
        this.inventoryBackupUtil = inventoryBackupUtil;
        this.mode = mode;
        this.gameSpawns = gameSpawns;
        this.minigameEvents = new MinigameEvents(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public abstract void onPlayerJoin(Player player);
    public abstract void onPlayerLeave(Player player);

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.add(uuid);
        previousLocations.put(uuid, player.getLocation());
        inventoryBackupUtil.saveInventory(player);

        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (gameSpawns.length > 1) {
            player.teleport(gameSpawns[new Random().nextInt(gameSpawns.length)]);
        } else {
            player.teleport(gameSpawns[0]);
        }

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (mode == GameMode.SINGLE_GAME) {
                other.hidePlayer(player);
                player.hidePlayer(other);
            } else if (mode == GameMode.MULTIPLAYER && !activePlayers.contains(other.getUniqueId())) {
                other.hidePlayer(player);
                player.hidePlayer(other);
            }
        }

        onPlayerJoin(player);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        inventoryBackupUtil.restoreInventory(player);

        if (previousLocations.containsKey(uuid)) {
            player.teleport(previousLocations.get(uuid));
            previousLocations.remove(uuid);
        }

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (mode == GameMode.MULTIPLAYER && activePlayers.contains(other.getUniqueId())) {
                other.showPlayer(player);
                player.showPlayer(other);
            } else if (mode == GameMode.SINGLE_GAME) {
                other.showPlayer(player);
                player.showPlayer(other);
            }
        }

        onPlayerLeave(player);
    }

    public boolean isInGame(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    public void createGameZone(Location pos1, Location pos2) {
        gameZone = new Cuboid(pos1, pos2);
    }

    public boolean gameZoneExists() {
        return gameZone != null;
    }

    public boolean isInsideGameZone(Player player) {
        return gameZone.contains(player.getLocation());
    }

    public boolean isInsideGameZone(Location location) {
        return gameZone.contains(location);
    }

    public void blockEvents(MinigameEvents.EventType... eventTypes) {
        minigameEvents.blockEvents(eventTypes);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();
        for (UUID activeUUID : activePlayers) {
            Player activePlayer = Bukkit.getPlayer(activeUUID);
            if (activePlayer != null) {
                if (mode == GameMode.SINGLE_GAME) {
                    joiningPlayer.hidePlayer(activePlayer);
                    activePlayer.hidePlayer(joiningPlayer);
                } else if (mode == GameMode.MULTIPLAYER) {
                    joiningPlayer.hidePlayer(activePlayer);
                    activePlayer.hidePlayer(joiningPlayer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player leavingPlayer = event.getPlayer();

        if (!isInGame(leavingPlayer)) {
            return;
        }

        removePlayer(leavingPlayer);
    }
}
