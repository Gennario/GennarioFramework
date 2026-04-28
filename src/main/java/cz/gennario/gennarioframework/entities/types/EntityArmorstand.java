package cz.gennario.gennarioframework.entities.types;

import cz.gennario.gennarioframework.entities.PacketEntity;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.equipment.PacketEquipmentEntry;
import cz.gennario.gennarioframework.utils.packet.equipment.PacketEquipmentSlot;
import cz.gennario.gennarioframework.utils.packet.types.PacketArmorStand;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityArmorstand extends PacketEntity {

    private PacketArmorStand packetArmorStand;

    private List<EquipmentDef> equipment;
    private List<Player> hiddenPlayers = new ArrayList<>();

    public EntityArmorstand(Location location, EntityVisiblity entityVisiblity, EquipmentDef... equipment) {
        super(-1, EntityType.ITEM_DISPLAY, entityVisiblity, false, 0, 40);
        this.equipment = new ArrayList<>(List.of(equipment));
        packetArmorStand = new PacketArmorStand();
        packetArmorStand.setLocation(location);
        setId(packetArmorStand.getEntityId());
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

        packetArmorStand.setEquipment(resolveEquipment(player));
        packetArmorStand.spawn(player);

        if (getPacketEntitySpawnOverwrite() != null) {
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
        packetArmorStand.setEquipment(resolveEquipment(player));
        packetArmorStand.spawn(player);
    }

    @Override
    public Location getLocation() {
        return packetArmorStand.getLocation();
    }

    public void updateItemStack(Player player) {
        packetArmorStand.setEquipment(resolveEquipment(player));
        packetArmorStand.updateEquipment(player);
    }

    public void addEquipment(PacketEquipmentSlot slot, ItemDisplayPlayerItem item) {
        equipment.add(new EquipmentDef(slot, item));
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

    private List<PacketEquipmentEntry> resolveEquipment(Player player) {
        List<PacketEquipmentEntry> result = new ArrayList<>();
        for (EquipmentDef def : equipment) {
            result.add(new PacketEquipmentEntry(def.slot(), def.item().getItemStack(player)));
        }
        return result;
    }

    public record EquipmentDef(PacketEquipmentSlot slot, ItemDisplayPlayerItem item) {}
}
