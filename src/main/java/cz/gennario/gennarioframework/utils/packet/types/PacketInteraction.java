package cz.gennario.gennarioframework.utils.packet.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

@Setter
@Getter
public class PacketInteraction extends PacketEntity {

    private float width;
    private float height;
    private boolean responsive;

    public PacketInteraction() {
        super();

        this.width = 1.0f;
        this.height = 1.0f;
        this.responsive = false;

        setShowName(false);
    }

    public PacketInteraction(Location location) {
        this();
        setLocation(location);
    }

    public PacketInteraction(Location location, float width, float height) {
        this();
        setLocation(location);
        this.width = width;
        this.height = height;
    }

    public void spawn(Player player) {
        sendSpawn(player, EntityType.INTERACTION);
        updateInteraction(player);
    }

    public void updateInteraction(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        super.updateEntity(player, metadata);

        PacketUtils.addMetadata(metadata, 8, EntityDataTypes.FLOAT, width);
        PacketUtils.addMetadata(metadata, 9, EntityDataTypes.FLOAT, height);
        PacketUtils.addMetadata(metadata, 10, EntityDataTypes.BOOLEAN, responsive);

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    public PacketInteraction updateWidth(Player player) {
        return updateWidth(player, width);
    }

    public PacketInteraction updateWidth(Player player, float width) {
        this.width = width;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 8, EntityDataTypes.FLOAT, width);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketInteraction updateHeight(Player player) {
        return updateHeight(player, height);
    }

    public PacketInteraction updateHeight(Player player, float height) {
        this.height = height;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 9, EntityDataTypes.FLOAT, height);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketInteraction updateResponsive(Player player) {
        return updateResponsive(player, responsive);
    }

    public PacketInteraction updateResponsive(Player player, boolean responsive) {
        this.responsive = responsive;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 10, EntityDataTypes.BOOLEAN, responsive);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketInteraction updateSize(Player player, float width, float height) {
        this.width = width;
        this.height = height;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 8, EntityDataTypes.FLOAT, width);
        PacketUtils.addMetadata(metadata, 9, EntityDataTypes.FLOAT, height);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketInteraction setWidth(float width) {
        this.width = width;
        return this;
    }

    public PacketInteraction setHeight(float height) {
        this.height = height;
        return this;
    }

    public PacketInteraction setResponsive(boolean responsive) {
        this.responsive = responsive;
        return this;
    }

    public PacketInteraction setSize(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }
}
