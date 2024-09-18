package cz.gennario.gennarioframework.utils.packet.entity;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public abstract class PacketEntityExtender {

    private final int entityId;

    private final EntityType entityType;
    private Location location;
    private Vector vector;

    private final boolean crouching, invisible, glowing, elytraFlying, showName, silent, noGravity;
    private final String name;

    private final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment;

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
        /* SPAWN */
        PacketContainer packet = PacketUtils.spawnEntityPacket(entityType, location, entityId, vector);
        PacketUtils.sendPacket(player, packet);

        /* DATA WATCHER - metadata set */
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        byte flags = 0;
        if (isCrouching()) {
            flags += (byte) 0x02;
        }
        if (isInvisible()) {
            flags += (byte) 0x20;
        }
        if (isGlowing()) {
            flags += (byte) 0x40;
        }
        if (isElytraFlying()) {
            flags += (byte) 0x80;
        }
        PacketUtils.setMetadata(dataWatcher, 0, Byte.class, (byte) flags);

        if (!Objects.equals(this.name, "")) {
            Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(Utils.colorize(player, this.name))[0].getHandle());
            PacketUtils.setMetadata(dataWatcher, 2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), opt);
        }
        if (isShowName()) {
            PacketUtils.setMetadata(dataWatcher, 3, Boolean.class, true);
        }
        if (isNoGravity()) {
            PacketUtils.setMetadata(dataWatcher, 5, Boolean.class, true);
        }
        if (isSilent()) {
            PacketUtils.setMetadata(dataWatcher, 4, Boolean.class, true);
        }

        PacketContainer packet1 = PacketUtils.applyMetadata(entityId, dataWatcher);
        PacketUtils.sendPacket(player, packet1);

        for (Pair<EnumWrappers.ItemSlot, ItemStack> itemSlotItemStackPair : equipment) {
            PacketContainer packet2 = PacketUtils.getEquipmentPacket(entityId, itemSlotItemStackPair);
            PacketUtils.sendPacket(player, packet2);
        }
    }

    public void delete(Player player) {
        PacketContainer destroyPacket = PacketUtils.destroyEntityPacket(entityId);
        PacketUtils.sendPacket(player, destroyPacket);
    }

    public void teleport(Player player, Location location) {
        this.location = location;

        PacketContainer teleportPacket = PacketUtils.teleportEntityPacket(entityId, location);
        PacketContainer headRotatePacket = PacketUtils.getHeadRotatePacket(entityId, location);
        PacketContainer bodyRotatePacket = PacketUtils.getHeadLookPacket(entityId, location);
        PacketUtils.sendPacket(player, teleportPacket);
        PacketUtils.sendPacket(player, bodyRotatePacket);
        PacketUtils.sendPacket(player, headRotatePacket);
    }

    public abstract PacketEntityExtender setEntityType(EntityType entityType);

    public abstract PacketEntityExtender setLocation(Location location);

    public abstract PacketEntityExtender addEquipment(Pair<EnumWrappers.ItemSlot, ItemStack>... equipment);

    public abstract PacketEntityExtender addEquipment(Pair<EnumWrappers.ItemSlot, ItemStack> equipment);

    public abstract PacketEntityExtender setEquipment(List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment);

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
