package cz.gennario.gennarioframework.entities.types;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.types.PacketArmorStand;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityArmorstand extends PacketEntity {

    private PacketArmorStand packetArmorStand;

    private List<Pair<EnumWrappers.ItemSlot, ItemDisplayPlayerItem>> equipment;
    private List<Player> hiddenPlayers = new ArrayList<>();

    public EntityArmorstand(Location location, EntityVisiblity entityVisiblity, Pair<EnumWrappers.ItemSlot, ItemDisplayPlayerItem>... equipment) {
        super(-1, EntityType.ITEM_DISPLAY, entityVisiblity, false, 0, 40);
        this.equipment = new ArrayList<>(List.of(equipment));
        packetArmorStand = new PacketArmorStand();
        packetArmorStand.setLocation(location);
        setId(packetArmorStand.getEntityId());
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

        if (new ArrayList<>(hiddenPlayers).contains(player)) {
            destroy(player);
            return;
        }

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> newEquipment = new ArrayList<>();
        for (Pair<EnumWrappers.ItemSlot, ItemDisplayPlayerItem> pair : equipment) {
            newEquipment.add(new Pair<>(pair.getFirst(), pair.getSecond().getItemStack(player)));
        }
        packetArmorStand.setEquipment(newEquipment);
        packetArmorStand.spawn(player);

        if(getPacketEntitySpawnOverwrite() != null) {
            getPacketEntitySpawnOverwrite().spawnOverwrite(player);
        }

        addSpawnedPlayer(player);
    }

    @Override
    public void destroy(Player player) {
        packetArmorStand.delete(player);

        removeSpawnedPlayer(player);
    }

    @Override
    public void teleport(Location location) {
        packetArmorStand.setLocation(location.clone());
        for (Player player : getSpawnedPlayers()) {
            packetArmorStand.teleport(player, location);
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        packetArmorStand.teleport(player, location);
    }

    @Override
    public void update(Player player) {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> newEquipment = new ArrayList<>();
        for (Pair<EnumWrappers.ItemSlot, ItemDisplayPlayerItem> pair : equipment) {
            newEquipment.add(new Pair<>(pair.getFirst(), pair.getSecond().getItemStack(player)));
        }
        packetArmorStand.setEquipment(newEquipment);
        packetArmorStand.spawn(player);
    }

    @Override
    public Location getLocation() {
        return packetArmorStand.getLocation();
    }

    public void updateItemStack(Player player) {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> newEquipment = new ArrayList<>();
        for (Pair<EnumWrappers.ItemSlot, ItemDisplayPlayerItem> pair : equipment) {
            newEquipment.add(new Pair<>(pair.getFirst(), pair.getSecond().getItemStack(player)));
        }
        packetArmorStand.setEquipment(newEquipment);
        packetArmorStand.updateEquipment(player);
    }

    public void updateItemStack(Player... players) {
        for (Player player : players) {
            updateItemStack(player);
        }
    }

    public void registerEvent(PacketClickResponse packetClickResponse) {
        packetArmorStand.addClickEvent(packetClickResponse);
    }

    public void hidePlayer(Player player) {
        hiddenPlayers.add(player);
        packetArmorStand.delete(player);
    }

    public void showPlayer(Player player) {
        hiddenPlayers.remove(player);
        packetArmorStand.spawn(player);
    }
}
