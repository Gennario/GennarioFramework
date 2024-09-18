package cz.gennario.gennarioframework.utils.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickType;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public final class PacketUtils {

    private static final boolean debug = true;
    public static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    public static final int MINECRAFT_VERSION = ProtocolLibrary.getProtocolManager().getMinecraftVersion().getMinor();
    public static boolean VERSION_1_20_4_AFTER_OR_EQUAL = Utils.versionIs(20) && Utils.versionIsAfterOrEqual(4);

    public static final Map<Integer, PacketClickResponse> entityClickMap = new HashMap<>();

    public static void init() {
        protocolManager.addPacketListener(new PacketAdapter(
                PacketAdapter.params(Main.getInstance(), PacketType.Play.Client.USE_ENTITY).listenerPriority(ListenerPriority.NORMAL)
                        .optionAsync()) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType().equals(PacketType.Play.Client.USE_ENTITY)) {
                    event.setCancelled(false);
                }
            }
        });

        /* Interact event on entity */
        protocolManager.addPacketListener(new PacketAdapter(Main.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                PacketContainer packet = e.getPacket();
                if (packet.getType() == PacketType.Play.Client.USE_ENTITY) {
                    int id = packet.getIntegers().read(0);
                    if (!entityClickMap.containsKey(id)) return;

                    List<PacketClickType> clickTypeList = new ArrayList<>();

                    EnumWrappers.EntityUseAction action = Utils.versionIsAfter(16) ? packet.getEnumEntityUseActions().readSafely(0).getAction() : packet.getEntityUseActions().readSafely(0);
                    boolean isShift = packet.getBooleans().readSafely(0);

                    switch (action.compareTo(EnumWrappers.EntityUseAction.INTERACT)) {
                        case 1:
                            clickTypeList.add(isShift ? PacketClickType.SHIFT_LEFT : PacketClickType.LEFT);
                            break;
                        case 2:
                            clickTypeList.add(isShift ? PacketClickType.SHIFT_RIGHT : PacketClickType.RIGHT);
                            break;
                        default:
                            return;
                    }

                    PacketClickResponse packetClickResponse = entityClickMap.get(id);
                    if (packetClickResponse == null) return;

                    packetClickResponse.onClick(clickTypeList, e.getPlayer());
                }
            }
        });
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
    }

    public static int generateRandomEntityId() {
        return Integer.parseInt(RandomStringUtils.random(8, false, true));
    }

    public static PacketContainer spawnEntityPacket(EntityType entityType, Location location, int entityId, Vector vector) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        // Entity ID
        packet.getIntegers().write(0, entityId);

        // Entity Type
        try {
            packet.getEntityTypeModifier().write(0, entityType);
        } catch (Exception e) {
            // Entity Type
            if (entityType.equals(EntityType.ARMOR_STAND)) {
                packet.getIntegers().write(6, 78);
            } else packet.getIntegers().write(6, (int) entityType.getTypeId());

            System.out.println("Entity type " + entityType + " is not supported by your server version!");

            // Set optional velocity (/8000)
            packet.getIntegers().write(1, 0);
            packet.getIntegers().write(2, 0);
            packet.getIntegers().write(3, 0);
            // Set yaw pitch
            packet.getIntegers().write(4, 0);
            packet.getIntegers().write(5, 0);
            // Set object data
            packet.getIntegers().write(7, 0);
        }

        // Set location
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());

        // Velocity
        if (vector != null) {
            packet.getIntegers()
                    .write(2, convertVelocity(vector.getX()))
                    .write(3, convertVelocity(vector.getY()))
                    .write(4, convertVelocity(vector.getZ()));
        }

        try {
            packet.getBytes().write(0, (byte) (location.getPitch() * 256.0F / 360.0F));
            packet.getBytes().write(1, (byte) (location.getYaw() * 256.0F / 360.0F));
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }

        // Set UUID
        packet.getUUIDs().write(0, UUID.randomUUID());

        return packet;
    }

    public static WrappedDataWatcher getDataWatcher() {
        return new WrappedDataWatcher();
    }

    public static PacketContainer applyMetadata(int entityId, WrappedDataWatcher watcher) {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, entityId);

            try {
                final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
                watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
                    final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
                    wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
                });
                packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
            } catch (Exception e) {
                packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
            }
            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return applyMetadata(entityId, watcher);
        }
    }

    public static WrappedDataWatcher setMetadata(WrappedDataWatcher watcher, int index, Class<?> c, Object value) {
        try {
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(index, WrappedDataWatcher.Registry.get(c)), value);
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return setMetadata(watcher, index, c, value);
        }
        return watcher;
    }

    public static WrappedDataWatcher setMetadata(WrappedDataWatcher watcher, int index, WrappedDataWatcher.Serializer serializer, Object value) {
        try {
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(index, serializer), value);
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return setMetadata(watcher, index, serializer, value);
        }
        return watcher;
    }

    public static PacketContainer getEntityRotation(int entityId, float yaw, float pitch) {
        byte rotationYaw = (byte) (yaw * 256 / 360);
        byte rotationPitch = (byte) (pitch * 256 / 360);

        PacketContainer entityLookPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK);
        entityLookPacket.getIntegers().write(0, entityId);
        entityLookPacket.getBytes()
                .write(0, rotationYaw)
                .write(1, rotationPitch);
        entityLookPacket.getBooleans().write(0, true);
        return entityLookPacket;
    }

    public static PacketContainer getEntityVelocity(int entityId, Vector vector) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers().write(0, entityId);
        packet.getIntegers()
                .write(1, convertVelocity(vector.getX()))
                .write(2, convertVelocity(vector.getY()))
                .write(3, convertVelocity(vector.getZ()));
        return packet;
    }

    public static PacketContainer getEquipmentPacket(int entityId, Pair<EnumWrappers.ItemSlot, ItemStack>... items) {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);

            packet.getIntegers().write(0, entityId);
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = Arrays.asList(items);
            packet.getSlotStackPairLists().write(0, list);
            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return getEquipmentPacket(entityId, items);
        }
    }

    public static PacketContainer teleportEntityPacket(int entityID, Location location) {
        try {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

            packet.getIntegers().write(0, entityID);
            packet.getDoubles().write(0, location.getX())
                    .write(1, location.getY())
                    .write(2, location.getZ());
            packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
            packet.getBytes().write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
            packet.getBooleans().write(0, false);

            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return teleportEntityPacket(entityID, location);
        }
    }

    public static void teleportToLocation(Player player, Location location) {
        PacketContainer packet = teleportEntityPacket(player.getEntityId(), location);
        PacketUtils.sendPacket(player, packet);
    }

    public static void teleportToLocation(Player sender, Entity entity, Location location) {
        PacketContainer packet = teleportEntityPacket(entity.getEntityId(), location);
        PacketUtils.sendPacket(sender, packet);
    }

    public static PacketContainer destroyEntityPacket(int entityID) {
        try {
            List<Integer> entityIDList = new ArrayList<>();
            entityIDList.add(entityID);
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getModifier().writeDefaults();
            try {
                packet.getIntLists().write(0, entityIDList);
            } catch (Exception e) {
                packet.getIntegerArrays().write(0, entityIDList.stream().mapToInt(i -> i).toArray());
            }

            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return null;
        }
    }

    public static PacketContainer getHeadRotatePacket(int entityId, Location location) {
        try {
            PacketContainer pc = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            pc.getModifier().writeDefaults();
            pc.getIntegers().write(0, entityId);
            pc.getBytes().write(0, (byte) getCompressedAngle(location.getYaw()));

            return pc;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return getHeadRotatePacket(entityId, location);
        }
    }

    public static PacketContainer getHeadLookPacket(int entityId, Location location) {
        try {
            PacketContainer pc = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
            pc.getModifier().writeDefaults();
            pc.getIntegers().write(0, entityId);
            pc.getBytes().write(0, (byte) location.getYaw());
            pc.getBooleans().write(0, false);

            return pc;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return getHeadLookPacket(entityId, location);
        }
    }

    public static PacketContainer getPassengerPacket(int vehicleId, int passengerCount, int... passengers) {
        try {
            PacketContainer pc = protocolManager.createPacket(PacketType.Play.Server.MOUNT);

            pc.getIntegers().write(0, vehicleId);
            pc.getIntegerArrays().write(0, passengers);

            return pc;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
        return null;
    }

    private static int getCompressedAngle(float value) {
        return (int) (value * 256.0F / 360.0F);
    }


    public static int convertVelocity(double velocity) {
        /*
          Minecraft represents a velocity within 4 blocks per second, in any direction,
          by using the entire Short range, meaning you can only move up to 4 blocks/second
          on any given direction
        */
        return (int) (clamp(velocity, -3.9, 3.9) * 8000);
    }

    public static double clamp(double targetNum, double min, double max) {
        // Makes sure a number is within a range
        return Math.max(min, Math.min(targetNum, max));
    }
}
