package cz.gennario.gennarioframework.utils;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class MinigameEvents implements Listener {

    public enum EventType {
        BLOCK_BREAK,
        BLOCK_PLACE,
        BLOCK_PLAYER_INTERACT,
        BLOCK_DROP,
        BLOCK_PICKUP,
        BLOCK_PLAYER_DAMAGE,
        BLOCK_TELEPORT_TO_GAME_ZONE,
        BLOCK_CHAT,
        BLOCK_COMMAND
    }

    private MinigameAdapter minigameAdapter;
    private List<EventType> blockedEvents;

    public MinigameEvents(MinigameAdapter minigameAdapter, EventType... blockedEvents) {
        this.minigameAdapter = minigameAdapter;
        this.blockedEvents = new ArrayList<>(List.of(blockedEvents));

        minigameAdapter.getPlugin().getServer().getPluginManager().registerEvents(this, minigameAdapter.getPlugin());
    }

    public void blockEvents(EventType... events) {
        blockedEvents.addAll(List.of(events));
    }

    public void unblockEvents(EventType... events) {
        blockedEvents.removeAll(List.of(events));
    }

    public boolean isBlocked(EventType event) {
        return blockedEvents.contains(event);
    }

    public boolean isPlayerInGame(Player player) {
        return minigameAdapter.getActivePlayers().contains(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isBlocked(EventType.BLOCK_BREAK) && isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isBlocked(EventType.BLOCK_PLACE) && isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (isBlocked(EventType.BLOCK_PLAYER_INTERACT) && isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDrop(PlayerDropItemEvent event) {
        if (isBlocked(EventType.BLOCK_DROP) && isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPickup(EntityPickupItemEvent event) {
        if (isBlocked(EventType.BLOCK_PICKUP) && event.getEntity() instanceof Player && isPlayerInGame((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlayerDamage(EntityDamageByEntityEvent event) {
        if (isBlocked(EventType.BLOCK_PLAYER_DAMAGE) && event.getDamager() instanceof Player && isPlayerInGame((Player) event.getDamager())) {
            event.setCancelled(true);
        }else if (isBlocked(EventType.BLOCK_PLAYER_DAMAGE) && event.getEntity() instanceof Player && isPlayerInGame((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockTeleportToGameZone(PlayerTeleportEvent event) {
        if (
                isBlocked(EventType.BLOCK_TELEPORT_TO_GAME_ZONE)
                        && minigameAdapter.gameZoneExists()
                        && minigameAdapter.isInsideGameZone(event.getTo())) {
            if (minigameAdapter.isInGame(event.getPlayer())) return;
            event.setCancelled(true);
        }
    }

    // block command tyb complete
    @EventHandler
    public void onBlockTabComplete(PlayerChatTabCompleteEvent event) {
        if (isBlocked(EventType.BLOCK_COMMAND) && isPlayerInGame(event.getPlayer())) {
            if (event.getChatMessage().startsWith("/")) {
                event.getTabCompletions().clear();
            }
        }
    }

    @EventHandler
    public void onBlockChat(AsyncPlayerChatEvent event) {
        if (isBlocked(EventType.BLOCK_CHAT) && isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockCommand(PlayerCommandPreprocessEvent event) {
        if (isBlocked(EventType.BLOCK_COMMAND) && isPlayerInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

}
