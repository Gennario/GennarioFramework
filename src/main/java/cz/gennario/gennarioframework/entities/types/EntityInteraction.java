package cz.gennario.gennarioframework.entities.types;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.types.PacketInteraction;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * EntityInteraction - Wrapper pro PacketInteraction entitu
 * Interaction entity je neviditelná entita určená pro detekci interakcí hráčů.
 * Přidáno v MC 1.19.4
 */
@Getter
public class EntityInteraction extends PacketEntity {

    private PacketInteraction packetInteraction;
    private List<Player> hiddenPlayers = new ArrayList<>();

    public EntityInteraction(Location location, EntityVisiblity entityVisiblity) {
        super(-1, EntityType.INTERACTION, entityVisiblity, false, 0, 40);
        packetInteraction = new PacketInteraction(location);
        setId(packetInteraction.getEntityId());
    }

    public EntityInteraction(Location location, EntityVisiblity entityVisiblity, float width, float height) {
        super(-1, EntityType.INTERACTION, entityVisiblity, false, 0, 40);
        packetInteraction = new PacketInteraction(location, width, height);
        setId(packetInteraction.getEntityId());
    }

    public EntityInteraction(Location location, EntityVisiblity entityVisiblity, float width, float height, boolean responsive) {
        super(-1, EntityType.INTERACTION, entityVisiblity, false, 0, 40);
        packetInteraction = new PacketInteraction(location, width, height);
        packetInteraction.setResponsive(responsive);
        setId(packetInteraction.getEntityId());
    }

    @Override
    public void spawn(Player player) {
        if (getPacketVisiblityCondition() != null) {
            if (!getPacketVisiblityCondition().canSee(player)) {
                if (new ArrayList<>(hiddenPlayers).contains(player)) {
                    destroy(player);
                }
                return;
            }
        }

        if (new ArrayList<>(hiddenPlayers).contains(player)) {
            destroy(player);
            return;
        }

        packetInteraction.spawn(player);

        if (getPacketEntitySpawnOverwrite() != null) {
            getPacketEntitySpawnOverwrite().spawnOverwrite(player);
        }

        addSpawnedPlayer(player);
    }

    @Override
    public void destroy(Player player) {
        packetInteraction.delete(player);
        removeSpawnedPlayer(player);
    }

    @Override
    public void teleport(Location location) {
        packetInteraction.setLocation(location.clone());
        for (Player player : getSpawnedPlayers()) {
            packetInteraction.teleport(player, location);
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        packetInteraction.teleport(player, location);
    }

    @Override
    public void update(Player player) {
        // Re-spawn pro update metadata
        packetInteraction.spawn(player);
    }

    @Override
    public Location getLocation() {
        return packetInteraction.getLocation();
    }

    /* INTERACTION SPECIFIC METHODS */

    public void setWidth(float width) {
        packetInteraction.setWidth(width);
    }

    public void setHeight(float height) {
        packetInteraction.setHeight(height);
    }

    public void setSize(float width, float height) {
        packetInteraction.setSize(width, height);
    }

    public void setResponsive(boolean responsive) {
        packetInteraction.setResponsive(responsive);
    }

    public void updateWidth(Player player, float width) {
        packetInteraction.updateWidth(player, width);
    }

    public void updateHeight(Player player, float height) {
        packetInteraction.updateHeight(player, height);
    }

    public void updateSize(Player player, float width, float height) {
        packetInteraction.updateSize(player, width, height);
    }

    public void updateResponsive(Player player, boolean responsive) {
        packetInteraction.updateResponsive(player, responsive);
    }

    public void addClickEvent(PacketClickResponse clickResponse) {
        packetInteraction.addClickEvent(clickResponse);
    }

    public void hideForPlayer(Player player) {
        if (!hiddenPlayers.contains(player)) {
            hiddenPlayers.add(player);
        }
        destroy(player);
    }

    public void showForPlayer(Player player) {
        hiddenPlayers.remove(player);
    }

    public float getWidth() {
        return packetInteraction.getWidth();
    }

    public float getHeight() {
        return packetInteraction.getHeight();
    }

    public boolean isResponsive() {
        return packetInteraction.isResponsive();
    }
}
