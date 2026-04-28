package cz.gennario.gennarioframework.utils.packet.entity;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.equipment.PacketEquipmentEntry;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
public class PacketEntity {
    private int entityId;
    private Vector velocity;

    private EntityType entityType;
    private boolean crouching, fire, swimming, sprinting, invisible, glowing, elytraFlying, showName, silent, gravity;
    private String name;
    private Location location;
    private float rotationYaw = -1, rotationPitch = -1;
    private int ticksFrozen, airTicks = 300;

    private List<PacketEquipmentEntry> equipment;
    /** Kept for API compatibility — no longer holds a ProtocolLib PacketContainer. */
    public Object entityPacketContainer;

    public PacketEntity() {
        this.entityId = PacketUtils.generateRandomEntityId();
        this.entityType = EntityType.PIG;
        this.invisible = false;
        this.crouching = false;
        this.glowing = false;
        this.elytraFlying = false;
        this.showName = false;
        this.gravity = true;
        this.silent = true;
        this.name = "";
        this.equipment = new ArrayList<>();
    }

    public void spawnEntity(Player player) {
        sendSpawn(player, entityType);
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.sendMetadataPacket(player, entityId, updateEntity(player, metadata));
    }

    protected void sendSpawn(Player player, EntityType type) {
        float yaw = rotationYaw != -1 ? rotationYaw : (location != null ? location.getYaw() : 0);
        float pitch = rotationPitch != -1 ? rotationPitch : (location != null ? location.getPitch() : 0);
        PacketUtils.sendSpawnPacket(player, type, location, entityId, velocity, yaw, pitch);
    }

    protected List<EntityData<?>> updateEntity(Player player, List<EntityData<?>> metadata) {
        byte flags = 0;
        if (fire) flags += (byte) 0x01;
        if (crouching) flags += (byte) 0x02;
        if (sprinting) flags += (byte) 0x08;
        if (swimming) flags += (byte) 0x10;
        if (invisible) flags += (byte) 0x20;
        if (glowing) flags += (byte) 0x40;
        if (elytraFlying) flags += (byte) 0x80;

        PacketUtils.addMetadata(metadata, 0, EntityDataTypes.BYTE, flags);
        PacketUtils.addMetadata(metadata, 1, EntityDataTypes.INT, airTicks);

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
        PacketUtils.addMetadata(metadata, 5, EntityDataTypes.BOOLEAN, !gravity);
        PacketUtils.addMetadata(metadata, 7, EntityDataTypes.INT, ticksFrozen);

        for (PacketEquipmentEntry entry : getEquipment()) {
            PacketUtils.sendEquipmentPacket(player, entityId, List.of(entry));
        }

        return metadata;
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

    public void addClickEvent(PacketClickResponse clickResponse) {
        PacketUtils.entityClickMap.put(entityId, clickResponse);
    }

    // ── UPDATE SPECIFIC THINGS ────────────────────────────────────────────────

    public PacketEntity updateName(Player player) {
        return updateName(player, name);
    }

    public PacketEntity updateName(Player player, String name) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (name != null && !name.isEmpty()) {
            try {
                String colorized = Utils.colorize(player, name);
                Component component = LegacyComponentSerializer.legacySection().deserialize(colorized);
                PacketUtils.addMetadata(metadata, 2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component));
            } catch (Exception e) {
                PacketUtils.addMetadata(metadata, 2, EntityDataTypes.STRING, name);
            }
        }
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateEquipment(Player player) {
        return updateEquipment(player, equipment);
    }

    public PacketEntity updateEquipment(Player player, List<PacketEquipmentEntry> equipment) {
        if (equipment != null && !equipment.isEmpty()) {
            PacketUtils.sendEquipmentPacket(player, entityId, equipment);
        }
        return this;
    }

    public PacketEntity updateRotation(Player player) {
        return updateRotation(player, rotationYaw, rotationPitch);
    }

    public PacketEntity updateRotation(Player player, float yaw, float pitch) {
        if (pitch == -1 && yaw == -1) return this;
        PacketUtils.sendRotationPacket(player, entityId,
                yaw != -1 ? yaw : 0,
                pitch != -1 ? pitch : 0);
        return this;
    }

    public PacketEntity updateVelocity(Player player) {
        return updateVelocity(player, velocity);
    }

    public PacketEntity updateVelocity(Player player, Vector vector) {
        if (vector == null) return this;
        PacketUtils.sendVelocityPacket(player, entityId, vector);
        return this;
    }

    public PacketEntity updateEntityFlags(Player player) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateEntityFlags(Player player, boolean fire, boolean crouching, boolean swimming,
                                          boolean sprinting, boolean invisible, boolean glowing, boolean elytraFlying) {
        byte flags = 0;
        if (fire) flags += (byte) 0x01;
        if (crouching) flags += (byte) 0x02;
        if (sprinting) flags += (byte) 0x08;
        if (swimming) flags += (byte) 0x10;
        if (invisible) flags += (byte) 0x20;
        if (glowing) flags += (byte) 0x40;
        if (elytraFlying) flags += (byte) 0x80;

        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 0, EntityDataTypes.BYTE, flags);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateFire(Player player, boolean fire) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateCrouching(Player player, boolean crouching) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateSwimming(Player player, boolean swimming) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateSprinting(Player player, boolean sprinting) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateInvisible(Player player, boolean invisible) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateGlowing(Player player, boolean glowing) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateElytraFlying(Player player, boolean elytraFlying) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateAirTicks(Player player) {
        return updateAirTicks(player, airTicks);
    }

    public PacketEntity updateAirTicks(Player player, int airTicks) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 1, EntityDataTypes.INT, airTicks);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateShowName(Player player) {
        return updateShowName(player, showName);
    }

    public PacketEntity updateShowName(Player player, boolean showName) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 3, EntityDataTypes.BOOLEAN, showName);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateSilent(Player player) {
        return updateSilent(player, silent);
    }

    public PacketEntity updateSilent(Player player, boolean silent) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 4, EntityDataTypes.BOOLEAN, silent);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateNoGravity(Player player) {
        return updateNoGravity(player, gravity);
    }

    public PacketEntity updateNoGravity(Player player, boolean noGravity) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 5, EntityDataTypes.BOOLEAN, !noGravity);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateTicksFrozen(Player player) {
        return updateTicksFrozen(player, ticksFrozen);
    }

    public PacketEntity updateTicksFrozen(Player player, int ticksFrozen) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 6, EntityDataTypes.INT, ticksFrozen);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    public PacketEntity updateSlimeSize(Player player, int size) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 16, EntityDataTypes.INT, size);
        PacketUtils.sendMetadataPacket(player, entityId, metadata);
        return this;
    }

    // ── Equipment setters ─────────────────────────────────────────────────────

    @SafeVarargs
    public final PacketEntity addEquipment(PacketEquipmentEntry... entries) {
        this.equipment.addAll(Arrays.asList(entries));
        return this;
    }

    public PacketEntity addEquipment(PacketEquipmentEntry entry) {
        this.equipment.add(entry);
        return this;
    }

    public PacketEntity setEquipment(List<PacketEquipmentEntry> equipment) {
        this.equipment = equipment;
        return this;
    }

    // ── Fluent setters ────────────────────────────────────────────────────────

    public PacketEntity setInvisible(boolean invisible) {
        this.invisible = invisible;
        return this;
    }

    public PacketEntity setShowName(boolean showName) {
        this.showName = showName;
        return this;
    }

    public PacketEntity setName(String name) {
        this.name = name;
        return this;
    }

    public PacketEntity setGravity(boolean gravity) {
        this.gravity = gravity;
        return this;
    }

    public PacketEntity setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }

    public PacketEntity setLocation(Location location) {
        this.location = location;
        return this;
    }

    public PacketEntity setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public PacketEntity setCrouching(boolean crouching) {
        this.crouching = crouching;
        return this;
    }

    public PacketEntity setElytraFlying(boolean elytraFlying) {
        this.elytraFlying = elytraFlying;
        return this;
    }

    public PacketEntity setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public PacketEntity setTicksFrozen(int ticksFrozen) {
        this.ticksFrozen = ticksFrozen;
        return this;
    }

    public PacketEntity setVelocity(Vector velocity) {
        this.velocity = velocity;
        return this;
    }

    public PacketEntity setRotation(float yaw, float pitch) {
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        return this;
    }

    public PacketEntity setRotationYaw(float yaw) {
        this.rotationYaw = yaw;
        return this;
    }

    public PacketEntity setRotationPitch(float pitch) {
        this.rotationPitch = pitch;
        return this;
    }

    public PacketEntity setFire(boolean fire) {
        this.fire = fire;
        return this;
    }

    public PacketEntity setSwimming(boolean swimming) {
        this.swimming = swimming;
        return this;
    }

    public PacketEntity setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        return this;
    }

    public PacketEntity setAirTicks(int airTicks) {
        this.airTicks = airTicks;
        return this;
    }
}
