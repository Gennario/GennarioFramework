package cz.gennario.gennarioframework.entities.types;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.types.display.types.PacketItemDisplay;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntityItemDisplay extends PacketEntity {

    private PacketItemDisplay packetItemDisplay;
    private ItemDisplayPlayerItem itemDisplayPlayerItem;
    private List<Player> hiddenPlayers = new ArrayList<>();

    public EntityItemDisplay(EntityVisiblity entityVisiblity, ItemDisplayPlayerItem itemDisplayPlayerItem) {
        super(-1, EntityType.ITEM_DISPLAY, entityVisiblity, false, 0, 40);
        this.itemDisplayPlayerItem = itemDisplayPlayerItem;
        packetItemDisplay = new PacketItemDisplay();
        setId(packetItemDisplay.getEntityId());
    }

    @Override
    public void spawn(Player player) {
        if(getPacketVisiblityCondition() != null) {
            if (!getPacketVisiblityCondition().canSee(player)) {
                if (new ArrayList<>(hiddenPlayers).contains(player)) {
                    destroy(player);
                }
                return;
            }
        }

        if(new ArrayList<>(hiddenPlayers).contains(player)) {
            if(new ArrayList<>(getSpawnedPlayers()).contains(player)) {
                destroy(player);
            }
            return;
        }

        packetItemDisplay.setItemStack(itemDisplayPlayerItem.getItemStack(player));
        packetItemDisplay.spawn(player);

        if(getPacketEntitySpawnOverwrite() != null) {
            getPacketEntitySpawnOverwrite().spawnOverwrite(player);
        }

        addSpawnedPlayer(player);
    }

    @Override
    public void destroy(Player player) {
        packetItemDisplay.delete(player);

        removeSpawnedPlayer(player);
    }

    @Override
    public void teleport(Location location) {
        packetItemDisplay.setLocation(location.clone());
        for (Player player : new ArrayList<>(getSpawnedPlayers())) {
            packetItemDisplay.teleport(player, location);
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        packetItemDisplay.teleport(player, location);
    }

    @Override
    public void update(Player player) {
        packetItemDisplay.setItemStack(itemDisplayPlayerItem.getItemStack(player));
        packetItemDisplay.update(player);
    }

    @Override
    public Location getLocation() {
        return packetItemDisplay.getLocation();
    }

    public void updateItemStack(Player player) {
        packetItemDisplay.setItemStack(itemDisplayPlayerItem.getItemStack(player));
    }

    public void updateScale(double scale, Player... player) {
        packetItemDisplay.setScale(scale);
        for (Player p : player) {
            packetItemDisplay.updateScale(p);
        }
    }

    public void updateItemStack(Player... players) {
        for (Player player : players) {
            packetItemDisplay.setItemStack(itemDisplayPlayerItem.getItemStack(player));
        }
    }

    public void registerEvent(PacketClickResponse packetClickResponse) {
        packetItemDisplay.addClickEvent(packetClickResponse);
    }

    public void hidePlayer(Player player) {
        hiddenPlayers.add(player);
        packetItemDisplay.delete(player);
    }

    public void showPlayer(Player player) {
        hiddenPlayers.remove(player);
        packetItemDisplay.spawn(player);
    }
}
