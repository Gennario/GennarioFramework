package cz.gennario.gennarioframework.utils.packet.entity;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.equipment.PacketEquipmentEntry;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public abstract class PacketEntityExtender {

    private final int entityId;

    private final EntityType entityType;
    private Location location;
    private Vector vector;

    private final boolean crouching, invisible, glowing, elytraFlying, showName, silent, noGravity;
    private final String name;

    private final List<PacketEquipmentEntry> equipment;

    public PacketEntityExtender() {
        this.entityId = PacketUtils.generateRandomEntityId();
        this.entityType = EntityType.PIG;

        this.invisible = false;
        this.crouching = false;
        this.glowing = false;
        this.elytraFlying = false;
        this.showName = false;
        this.noGravity = true;
        this.silent = true;

        this.name = "";

        this.equipment = new ArrayList<>();
    }

    public void spawn(Player player) {
        float yaw = location != null ? location.getYaw() : 0;
        float pitch = location != null ? location.getPitch() : 0;
        PacketUtils.sendSpawnPacket(player, entityType, location, entityId, vector, yaw, pitch);

        List<EntityData<?>> metadata = PacketUtils.createMetadata();

        byte flags = 0;
        if (crouching) flags += (byte) 0x02;
        if (invisible) flags += (byte) 0x20;
        if (glowing) flags += (byte) 0x40;
        if (elytraFlying) flags += (byte) 0x80;
        PacketUtils.addMetadata(metadata, 0, EntityDataTypes.BYTE, flags);

        if (name != null && !name.isEmpty()) {
            try {
                String colorized = Utils.colorize(player, name);
                Component component = LegacyComponentSerializer.legacySection().deserialize(colorized);
                PacketUtils.addMetadata(metadata, 2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component));
            } catch (Exception e) {
                PacketUtils.addMetadata(metadata, 2, EntityDataTypes.STRING, name);
            }
        }
        PacketUtils.addMetadata(metadata, 3, EntityDataTypes.BOOLEAN, showName);
        PacketUtils.addMetadata(metadata, 4, EntityDataTypes.BOOLEAN, silent);
        PacketUtils.addMetadata(metadata, 5, EntityDataTypes.BOOLEAN, noGravity);

        PacketUtils.sendMetadataPacket(player, entityId, metadata);

        if (!equipment.isEmpty()) {
            PacketUtils.sendEquipmentPacket(player, entityId, equipment);
        }
    }

    public void delete(Player player) {
        PacketUtils.sendDestroyPacket(player, entityId);
    }

    public void teleport(Player player, Location location) {
        this.location = location;
        PacketUtils.sendTeleportPacket(player, entityId, location);
        PacketUtils.sendBodyRotationPacket(player, entityId, location);
        PacketUtils.sendHeadRotationPacket(player, entityId, location.getYaw());
    }

    public abstract PacketEntityExtender setEntityType(EntityType entityType);

    public abstract PacketEntityExtender setLocation(Location location);

    public abstract PacketEntityExtender addEquipment(PacketEquipmentEntry... equipment);

    public abstract PacketEntityExtender addEquipment(PacketEquipmentEntry equipment);

    public abstract PacketEntityExtender setEquipment(List<PacketEquipmentEntry> equipment);

    public abstract PacketEntityExtender setInvisible(boolean invisible);

    public abstract PacketEntityExtender setShowName(boolean showName);

    public abstract PacketEntityExtender setName(String name);

    public abstract PacketEntityExtender setNoGravity(boolean hasNoGravity);

    public abstract PacketEntityExtender setSilent(boolean silent);

    public abstract PacketEntityExtender setCoreLocation(Location coreLocation);

    public abstract PacketEntityExtender setGlowing(boolean glowing);

    public abstract PacketEntityExtender setHeadRotation(EulerAngle headRotation);

    public abstract PacketEntityExtender setBodyRotation(EulerAngle bodyRotation);

    public abstract PacketEntityExtender setLeftArmRotation(EulerAngle leftArmRotation);

    public abstract PacketEntityExtender setLeftLegRotation(EulerAngle leftLegRotation);

    public abstract PacketEntityExtender setRightArmRotation(EulerAngle rightArmRotation);

    public abstract PacketEntityExtender setRightLegRotation(EulerAngle rightLegRotation);

    public abstract PacketEntityExtender setCrouching(boolean crouching);

    public abstract PacketEntityExtender setElytraFlying(boolean elytraFlying);
}
