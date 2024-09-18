package cz.gennario.gennarioframework.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public abstract class PacketEntity {

    public enum EntityType {
        BLOCK_DISPLAY,
        ITEM_DISPLAY,
        ENTITY_DISPLAY,
        TEXT_DISPLAY,
        ARMOR_STAND,
        HOLOGRAM
    }
    public enum EntityVisiblity {
        PUBLIC,
        PRIVATE
    }

    private int id;
    private EntityType entityType;
    private EntityVisiblity entityVisiblity;
    private boolean update;
    private int updateTime;
    private int viewDistance;
    private List<String> visiblityList = new ArrayList<>();
    private List<Player> spawnedPlayers = new ArrayList<>();
    private PacketVisiblityCondition packetVisiblityCondition;
    private PacketEntitySpawnOverwrite packetEntitySpawnOverwrite;

    public PacketEntity(int id, EntityType entityType, EntityVisiblity entityVisiblity, boolean update, int updateTime, int viewDistance) {
        this.id = id;
        this.entityType = entityType;
        this.entityVisiblity = entityVisiblity;
        this.update = update;
        this.updateTime = updateTime;
        this.viewDistance = viewDistance;
    }

    public abstract void spawn(Player player);
    public abstract void destroy(Player player);
    public void destroyAll() {
        for (Player player : new ArrayList<>(spawnedPlayers)) {
            destroy(player);
        }

    }
    public abstract void teleport(Location location);
    public abstract void teleport(Player player, Location location);
    public abstract void update(Player player);
    public void updateAll() {
        for (Player player : new ArrayList<>(spawnedPlayers)) {
            update(player);
        }
    }
    public abstract Location getLocation();

    public void addVisiblity(String name) {
        visiblityList.add(name);
    }

    public void removeVisiblity(String name) {
        visiblityList.remove(name);
    }

    public void addSpawnedPlayer(Player player) {
        if(new ArrayList<>(spawnedPlayers).contains(player))
            return;
        spawnedPlayers.add(player);
    }

    public void removeSpawnedPlayer(Player player) {
        if(!new ArrayList<>(spawnedPlayers).contains(player))
            return;
        spawnedPlayers.remove(player);
    }
}
