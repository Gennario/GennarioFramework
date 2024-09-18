package cz.gennario.gennarioframework.utils.packet.entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public abstract class PacketEntity {
    private int entityId;
    private Vector velocity;

    private EntityType entityType;
    private boolean crouching, fire, swimming, sprinting, invisible, glowing, elytraFlying, showName, silent, gravity;
    private String name;
    private Location location;
    private float rotationYaw = -1, rotationPitch = -1;
    private int ticksFrozen, airTicks = 300;

    private List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment;
    public PacketContainer entityPacketContainer;

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
        /* SPAWN */
        PacketUtils.sendPacket(player, getEntity(entityType));

        /* DATA WATCHER - metadata set */
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, updateEntity(player, dataWatcher)));
    }

    protected PacketContainer getEntity(EntityType entityType) {
        PacketContainer packetContainer = PacketUtils.spawnEntityPacket(entityType, location, entityId, velocity);
        if (rotationYaw != -1 || rotationPitch != -1) {
            if (rotationPitch != -1) packetContainer.getBytes().write(0, (byte) (rotationPitch * 256.0F / 360.0F));
            if (rotationYaw != -1) packetContainer.getBytes().write(1, (byte) (rotationYaw * 256.0F / 360.0F));
        }

        return (entityPacketContainer = packetContainer);
    }

    protected WrappedDataWatcher updateEntity(Player player, WrappedDataWatcher dataWatcher) {
        byte flags = 0;
        if (fire) flags += (byte) 0x01;
        if (crouching) flags += (byte) 0x02;
        if (sprinting) flags += (byte) 0x08;
        if (swimming) flags += (byte) 0x10;
        if (invisible) flags += (byte) 0x20;
        if (glowing) flags += (byte) 0x40;
        if (elytraFlying) flags += (byte) 0x80;

        PacketUtils.setMetadata(dataWatcher, 0, Byte.class, (byte) flags);
        PacketUtils.setMetadata(dataWatcher, 1, Integer.class, airTicks);

        if (name != null) {
            Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(Utils.colorize(player, this.name))[0].getHandle());
            try {
                PacketUtils.setMetadata(dataWatcher, 2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), opt);
            } catch (Exception e) {
                PacketUtils.setMetadata(dataWatcher, 2, String.class, this.name);
            }
        }


        PacketUtils.setMetadata(dataWatcher, 3, Boolean.class, showName);
        PacketUtils.setMetadata(dataWatcher, 4, Boolean.class, silent);
        PacketUtils.setMetadata(dataWatcher, 5, Boolean.class, !gravity);
        PacketUtils.setMetadata(dataWatcher, 7, Integer.class, ticksFrozen);


        for (Pair<EnumWrappers.ItemSlot, ItemStack> itemSlotItemStackPair : getEquipment()) {
            PacketContainer packet2 = PacketUtils.getEquipmentPacket(getEntityId(), new Pair<>(itemSlotItemStackPair.getFirst(), itemSlotItemStackPair.getSecond()));
            PacketUtils.sendPacket(player, packet2);
        }

        return dataWatcher;
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

    /* Click event */
    public void addClickEvent(PacketClickResponse clickResponse) {
        PacketUtils.entityClickMap.put(entityId, clickResponse);
    }

    /* UPDATE SPECIFIC THINS */

    /* CUSTOM NAME */
    public PacketEntity updateName(Player player) {
        return updateName(player, name);
    }

    public PacketEntity updateName(Player player, String name) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (name != null) {
            Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(Utils.colorize(player, this.name))[0].getHandle());
            PacketUtils.setMetadata(dataWatcher, 2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), opt);
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }

    /* EQUIPMENT */
    public PacketEntity updateEquipment(Player player) {
        return updateEquipment(player, equipment);
    }

    public PacketEntity updateEquipment(Player player, List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (equipment != null) {
            for (Pair<EnumWrappers.ItemSlot, ItemStack> itemSlotItemStackPair : equipment) {
                PacketContainer packet2 = PacketUtils.getEquipmentPacket(entityId, new Pair<>(itemSlotItemStackPair.getFirst(), itemSlotItemStackPair.getSecond()));
                PacketUtils.sendPacket(player, packet2);
            }
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }

    /* ROTATION */
    public PacketEntity updateRotation(Player player) {
        return updateRotation(player, rotationYaw, rotationPitch);
    }

    public PacketEntity updateRotation(Player player, float yaw, float pitch) {
        if (pitch == -1 && yaw == -1) return this;

        PacketContainer packet = PacketUtils.protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK);
        packet.getIntegers().write(0, entityId);

        if (yaw != -1) {
            packet.getBytes().write(0, (byte) (yaw * 256.0F / 360.0F));
        }

        if (pitch != -1) {
            packet.getBytes().write(1, (byte) (pitch * 256.0F / 360.0F));
        }

        packet.getBooleans().write(0, true);
        PacketUtils.sendPacket(player, packet);
        return this;
    }

    /* Velocity */
    public PacketEntity updateVelocity(Player player) {
        return updateVelocity(player, velocity);
    }

    public PacketEntity updateVelocity(Player player, Vector vector) {
        if (vector == null) return this;

        PacketContainer packet = PacketUtils.protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers().write(0, entityId);
        packet.getIntegers().write(1, PacketUtils.convertVelocity(vector.getX()));
        packet.getIntegers().write(2, PacketUtils.convertVelocity(vector.getY()));
        packet.getIntegers().write(3, PacketUtils.convertVelocity(vector.getZ()));
        PacketUtils.sendPacket(player, packet);
        return this;
    }

    /* FLAGS */
    public PacketEntity updateEntityFlags(Player player) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    public PacketEntity updateEntityFlags(Player player, boolean fire, boolean crouching, boolean swimming, boolean sprinting, boolean invisible, boolean glowing, boolean elytraFlying) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        byte flags = 0;
        if (fire) flags += (byte) 0x01;
        if (crouching) flags += (byte) 0x02;
        if (sprinting) flags += (byte) 0x08;
        if (swimming) flags += (byte) 0x10;
        if (invisible) flags += (byte) 0x20;
        if (glowing) flags += (byte) 0x40;
        if (elytraFlying) flags += (byte) 0x80;

        PacketUtils.setMetadata(dataWatcher, 0, Byte.class, (byte) flags);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }

    /* UPDATE FIRE FLAG */
    public PacketEntity updateFire(Player player, boolean fire) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* UPDATE CROUCHING FLAG */
    public PacketEntity updateCrouching(Player player, boolean crouching) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* UPDATE SWIMMING FLAG */
    public PacketEntity updateSwimming(Player player, boolean swimming) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* UPDATE SPRINTING FLAG */
    public PacketEntity updateSprinting(Player player, boolean sprinting) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* UPDATE INVISIBLE FLAG */
    public PacketEntity updateInvisible(Player player, boolean invisible) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* UPDATE GLOWING FLAG */
    public PacketEntity updateGlowing(Player player, boolean glowing) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* UPDATE ELYTRA FLYING FLAG */
    public PacketEntity updateElytraFlying(Player player, boolean elytraFlying) {
        return updateEntityFlags(player, fire, crouching, swimming, sprinting, invisible, glowing, elytraFlying);
    }

    /* AIR TICKS */
    public PacketEntity updateAirTicks(Player player) {
        return updateAirTicks(player, airTicks);
    }

    public PacketEntity updateAirTicks(Player player, int airTicks) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        PacketUtils.setMetadata(dataWatcher, 1, Integer.class, airTicks);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }


    /* SHOW NAME */
    public PacketEntity updateShowName(Player player) {
        return updateShowName(player, showName);
    }

    public PacketEntity updateShowName(Player player, boolean showName) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (showName) PacketUtils.setMetadata(dataWatcher, 3, Boolean.class, true);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* SILENT */
    public PacketEntity updateSilent(Player player) {
        return updateSilent(player, silent);
    }

    public PacketEntity updateSilent(Player player, boolean silent) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (silent) PacketUtils.setMetadata(dataWatcher, 4, Boolean.class, true);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }

    /* NO GRAVITY */
    public PacketEntity updateNoGravity(Player player) {
        return updateNoGravity(player, gravity);
    }

    public PacketEntity updateNoGravity(Player player, boolean noGravity) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (noGravity) PacketUtils.setMetadata(dataWatcher, 5, Boolean.class, true);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }

    /* TICKS FROZEN */
    public PacketEntity updateTicksFrozen(Player player) {
        return updateTicksFrozen(player, ticksFrozen);
    }

    public PacketEntity updateTicksFrozen(Player player, int ticksFrozen) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (ticksFrozen != 0) PacketUtils.setMetadata(dataWatcher, 6, Integer.class, ticksFrozen);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(entityId, dataWatcher));
        return this;
    }

    public PacketEntity addEquipment(Pair<EnumWrappers.ItemSlot, ItemStack>... equipment) {
        this.equipment.addAll(Arrays.asList(equipment));
        return this;
    }

    public PacketEntity addEquipment(Pair<EnumWrappers.ItemSlot, ItemStack> equipment) {
        this.equipment.add(equipment);
        return this;
    }

    public PacketEntity setEquipment(List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment) {
        this.equipment = equipment;
        return this;
    }

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
