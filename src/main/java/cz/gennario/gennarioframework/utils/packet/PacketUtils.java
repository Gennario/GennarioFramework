package cz.gennario.gennarioframework.utils.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import com.comphenix.protocol.scheduler.Task;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.backend.PacketBackend;
import cz.gennario.gennarioframework.utils.packet.backend.PacketBackendMode;
import cz.gennario.gennarioframework.utils.packet.backend.PacketEventsPacketBackend;
import cz.gennario.gennarioframework.utils.packet.backend.ProtocolLibPacketBackend;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public final class PacketUtils {

    private static final boolean debug = false;
    public static ProtocolManager protocolManager;
    public static int MINECRAFT_VERSION = -1;
    private static final int CLIENT_PROTOCOL_1_20 = 763;
    private static boolean loggedDisplayAdapterOverride = false;
    private static PacketBackendMode activeBackendMode = PacketBackendMode.PROTOCOLLIB;
    private static boolean backendFallbackEnabled = true;
    private static PacketBackendMode autoPriorityMode = PacketBackendMode.PROTOCOLLIB;
    private static final Map<PacketBackendMode, PacketBackend> BACKENDS = new EnumMap<>(PacketBackendMode.class);
    private static PacketBackend activeBackend;
    private static boolean loggedProtocolLibUnavailable = false;
    private static boolean protocolLibSchedulerBootstrapped = false;

    public static final Map<Integer, PacketClickResponse> entityClickMap = new HashMap<>();

    static {
        BACKENDS.put(PacketBackendMode.PROTOCOLLIB, new ProtocolLibPacketBackend());
        BACKENDS.put(PacketBackendMode.PACKETEVENTS, new PacketEventsPacketBackend());
        activeBackend = BACKENDS.get(PacketBackendMode.PROTOCOLLIB);
        protocolManager = tryGetProtocolManager();
        if (protocolManager != null) {
            MINECRAFT_VERSION = protocolManager.getMinecraftVersion().getMinor();
        }
    }

    private static ProtocolManager tryGetProtocolManager() {
        try {
            return ProtocolLibrary.getProtocolManager();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static ProtocolManager getProtocolManagerSafe() {
        if (protocolManager == null) {
            protocolManager = tryGetProtocolManager();
            if (protocolManager != null) {
                MINECRAFT_VERSION = protocolManager.getMinecraftVersion().getMinor();
            }
        }
        return protocolManager;
    }

    private static PacketContainer createPacketSafe(PacketType type) {
        try {
            ProtocolManager manager = getProtocolManagerSafe();
            if (manager != null) {
                PacketContainer packet = manager.createPacket(type);
                try {
                    packet.getModifier().writeDefaults();
                } catch (Exception ignored) {
                    // Some packet types do not expose defaults in the same way across versions.
                }

                return packet;
            }

            ensureProtocolLibScheduler();

            PacketContainer packet = new PacketContainer(type);
            try {
                packet.getModifier().writeDefaults();
            } catch (Exception ignored) {
                // Some packet types do not expose defaults in the same way across versions.
            }

            return packet;

        } catch (Throwable ignored) {
            if (!loggedProtocolLibUnavailable && Main.getInstance() != null) {
                Main.getInstance().getLogger().warning("[PacketUtils] ProtocolLib manager is unavailable; packet creation via ProtocolLib internals is disabled.");
                loggedProtocolLibUnavailable = true;
            }
            return null;
        }
    }

    private static void ensureProtocolLibScheduler() {
        if (protocolLibSchedulerBootstrapped) {
            return;
        }
        protocolLibSchedulerBootstrapped = true;

        try {
            if (ProtocolLibrary.getScheduler() != null) {
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            java.lang.reflect.Field schedulerField = ProtocolLibrary.class.getDeclaredField("scheduler");
            schedulerField.setAccessible(true);

            if (schedulerField.get(null) != null) {
                return;
            }

            schedulerField.set(null, createSafeProtocolScheduler());
        } catch (Throwable ignored) {
            // Best-effort bootstrap only.
        }
    }

    private static ProtocolScheduler createSafeProtocolScheduler() {
        return new ProtocolScheduler() {
            @Override
            public Task scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
                int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), wrapSafe(runnable), delay, period);
                return () -> Bukkit.getScheduler().cancelTask(taskId);
            }

            @Override
            public Task runTask(Runnable runnable) {
                int taskId = Bukkit.getScheduler().runTask(Main.getInstance(), wrapSafe(runnable)).getTaskId();
                return () -> Bukkit.getScheduler().cancelTask(taskId);
            }

            @Override
            public Task scheduleSyncDelayedTask(Runnable runnable, long delay) {
                int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), wrapSafe(runnable), delay);
                return () -> Bukkit.getScheduler().cancelTask(taskId);
            }

            @Override
            public Task runTaskAsync(Runnable runnable) {
                int taskId = Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), wrapSafe(runnable)).getTaskId();
                return () -> Bukkit.getScheduler().cancelTask(taskId);
            }
        };
    }

    private static Runnable wrapSafe(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (IllegalStateException ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("Unexpected protocol: CONFIGURATION")) {
                    return;
                }
                throw ex;
            }
        };
    }

    public static void configureBackend(PacketBackendMode requestedMode, boolean fallbackEnabled, PacketBackendMode autoPriority) {
        backendFallbackEnabled = fallbackEnabled;
        autoPriorityMode = (autoPriority == null || autoPriority == PacketBackendMode.AUTO)
                ? PacketBackendMode.PROTOCOLLIB
                : autoPriority;

        PacketBackendMode mode = requestedMode == null ? PacketBackendMode.AUTO : requestedMode;
        activeBackendMode = resolveBackend(mode);
        activeBackend = BACKENDS.get(activeBackendMode);

        if (Main.getInstance() != null) {
            Main.getInstance().getLogger().info("[PacketUtils] Backend requested=" + mode + ", active=" + activeBackendMode + ", fallback=" + backendFallbackEnabled);
        }
    }

    public static PacketBackendMode getActiveBackendMode() {
        return activeBackendMode;
    }

    public static boolean isBackendFallbackEnabled() {
        return backendFallbackEnabled;
    }

    private static PacketBackendMode resolveBackend(PacketBackendMode requestedMode) {
        if (requestedMode == PacketBackendMode.AUTO) {
            if (isBackendReady(autoPriorityMode)) return autoPriorityMode;
            PacketBackendMode secondary = autoPriorityMode == PacketBackendMode.PROTOCOLLIB
                    ? PacketBackendMode.PACKETEVENTS
                    : PacketBackendMode.PROTOCOLLIB;
            if (isBackendReady(secondary)) return secondary;
            throw new IllegalStateException("No supported packet backend is available in AUTO mode.");
        }

        if (isBackendReady(requestedMode)) {
            return requestedMode;
        }

        if (!backendFallbackEnabled) {
            throw new IllegalStateException("Requested packet backend " + requestedMode + " is not available.");
        }

        if (requestedMode != PacketBackendMode.PROTOCOLLIB && isBackendReady(PacketBackendMode.PROTOCOLLIB)) {
            Main.getInstance().getLogger().warning("[PacketUtils] Falling back to PROTOCOLLIB backend.");
            return PacketBackendMode.PROTOCOLLIB;
        }

        throw new IllegalStateException("No usable packet backend found for mode " + requestedMode + ".");
    }

    private static boolean isBackendReady(PacketBackendMode mode) {
        PacketBackend backend = BACKENDS.get(mode);
        return backend != null && backend.isAvailable();
    }

    public static void init() {
        protocolManager = getProtocolManagerSafe();
        Main.getInstance().getLogger().info("[PacketUtils] Active backend: " + activeBackendMode);

        // Pre-warm serializer cache and log results for debugging
        for (Class<?> c : new Class<?>[]{
                Byte.class, Integer.class, Float.class, String.class,
                org.joml.Vector3f.class, org.joml.Quaternionf.class
        }) {
            try {
                WrappedDataWatcher.Serializer s = resolveSerializer(c);
                if (s == null) {
                    Main.getInstance().getLogger().warning("[PacketUtils] Failed to resolve serializer for: " + c.getSimpleName());
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().warning("[PacketUtils] Exception resolving serializer for: " + c.getSimpleName() + " - " + e.getMessage());
            }
        }

        activeBackend.init();
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        if (packet == null) {
            if (debug) Main.getInstance().getLogger().warning("[PacketUtils] Tried to send null packet to " + player.getName());
            return;
        }
        try {
            activeBackend.sendPacket(player, packet);
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send packet " + packet.getType() + " via backend " + activeBackendMode + ": " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static int generateRandomEntityId() {
        return Integer.parseInt(RandomStringUtils.random(8, false, true));
    }

    public static PacketContainer spawnEntityPacket(EntityType entityType, Location location, int entityId, Vector vector) {
        PacketContainer packet = createPacketSafe(PacketType.Play.Server.SPAWN_ENTITY);
        if (packet == null) {
            return null;
        }

        // Entity ID
        packet.getIntegers().write(0, entityId);

        // Entity Type
        try {
            packet.getEntityTypeModifier().write(0, entityType);
        } catch (Exception e) {
            // Entity Type
            if (entityType.equals(EntityType.ARMOR_STAND)) {
                packet.getIntegers().write(6, 78);
            }

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
            PacketContainer packet = createPacketSafe(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, entityId);

            try {
                final List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
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
            return null;
        }
    }

    public static WrappedDataWatcher setMetadata(WrappedDataWatcher watcher, int index, Class<?> c, Object value) {
        try {
            WrappedDataWatcher.Serializer serializer = resolveSerializer(c);
            if (serializer == null) {
                if (debug) Main.getInstance().getLogger().warning("[PacketUtils] Serializer is null for type: " + c.getName() + " at index " + index);
                return watcher;
            }
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(index, serializer), value);
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
        return watcher;
    }

    /**
     * Resolves a WrappedDataWatcher serializer for the given class.
     * Uses multiple fallback strategies for compatibility across ProtocolLib and MC versions.
     */
    private static final Map<Class<?>, WrappedDataWatcher.Serializer> SERIALIZER_CACHE = new HashMap<>();

    private static WrappedDataWatcher.Serializer resolveSerializer(Class<?> c) {
        WrappedDataWatcher.Serializer cached = SERIALIZER_CACHE.get(c);
        if (cached != null) return cached;

        WrappedDataWatcher.Serializer serializer = resolveSerializerInternal(c);
        if (serializer != null) {
            SERIALIZER_CACHE.put(c, serializer);
        }
        return serializer;
    }

    private static WrappedDataWatcher.Serializer resolveSerializerInternal(Class<?> c) {
        // Strategy 1: For JOML Vector3f - use NMS EntityDataSerializers via reflection
        if (c == org.joml.Vector3f.class) {
            // Try known field names across different MC versions/mappings
            for (String name : new String[]{"VECTOR3", "ROTATIONS", "o", "p", "q"}) {
                try {
                    return getSerializerFromNMS(name);
                } catch (Exception ignored) {
                }
            }
            // Try finding by iterating all serializer fields
            try {
                WrappedDataWatcher.Serializer s = findSerializerByType(org.joml.Vector3f.class);
                if (s != null) return s;
            } catch (Exception ignored) {
            }
        }

        // Strategy 2: For JOML Quaternionf - use NMS EntityDataSerializers via reflection
        if (c == org.joml.Quaternionf.class) {
            for (String name : new String[]{"QUATERNION", "r", "s"}) {
                try {
                    return getSerializerFromNMS(name);
                } catch (Exception ignored) {
                }
            }
            try {
                WrappedDataWatcher.Serializer s = findSerializerByType(org.joml.Quaternionf.class);
                if (s != null) return s;
            } catch (Exception ignored) {
            }
        }

        // Strategy 3: Find serializer by generic type information.
        try {
            WrappedDataWatcher.Serializer byType = findSerializerByType(c);
            if (byType != null) return byType;
        } catch (Exception ignored) {
        }

        // Strategy 4: Brute force - iterate all NMS serializers and find one matching the class
        try {
            return findSerializerByBruteForce(c);
        } catch (Exception ignored) {
        }

        Main.getInstance().getLogger().warning("[PacketUtils] Cannot resolve serializer for type: " + c.getName());
        return null;
    }

    /**
     * Tries to get a serializer from NMS EntityDataSerializers by field name via reflection.
     */
    private static WrappedDataWatcher.Serializer getSerializerFromNMS(String fieldName) throws Exception {
        // Try net.minecraft.network.syncher.EntityDataSerializers
        Class<?> nmsClass;
        try {
            nmsClass = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        } catch (ClassNotFoundException e) {
            // Legacy path
            nmsClass = Class.forName("net.minecraft.server." + getServerVersion() + ".DataWatcherRegistry");
        }

        java.lang.reflect.Field field = nmsClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object nmsSerializer = field.get(null);
        return WrappedDataWatcher.Registry.fromHandle(nmsSerializer);
    }

    private static String getServerVersion() {
        String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");
        return parts.length > 3 ? parts[3] : "";
    }

    /**
     * Finds a serializer by checking each NMS EntityDataSerializers field's generic type parameter.
     * This works even when field names are obfuscated, because it inspects the serializer's actual type.
     */
    private static WrappedDataWatcher.Serializer findSerializerByType(Class<?> targetClass) throws Exception {
        Class<?> nmsClass;
        try {
            nmsClass = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        } catch (ClassNotFoundException e) {
            return null;
        }

        for (java.lang.reflect.Field field : nmsClass.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object value = field.get(null);
            if (value == null) continue;

            // Check the generic type parameter of EntityDataSerializer<T>
            java.lang.reflect.Type genericType = field.getGenericType();
            if (genericType instanceof java.lang.reflect.ParameterizedType pt) {
                java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> typeArg) {
                    if (typeArg == targetClass) {
                        try {
                            return WrappedDataWatcher.Registry.fromHandle(value);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Brute force: iterate all fields of EntityDataSerializers and find a serializer
     * whose type matches the given class by name comparison.
     */
    private static WrappedDataWatcher.Serializer findSerializerByBruteForce(Class<?> targetClass) throws Exception {
        Class<?> nmsClass;
        try {
            nmsClass = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        } catch (ClassNotFoundException e) {
            return null;
        }

        for (java.lang.reflect.Field field : nmsClass.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value != null) {
                    try {
                        WrappedDataWatcher.Serializer wrapped = WrappedDataWatcher.Registry.fromHandle(value);
                        // Check if this serializer handles our target class by trying to use it
                        if (wrapped != null) {
                            String serializerStr = wrapped.toString().toLowerCase();
                            String targetName = targetClass.getSimpleName().toLowerCase();
                            if (serializerStr.contains(targetName)) {
                                return wrapped;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }

    public static WrappedDataWatcher setMetadata(WrappedDataWatcher watcher, int index, WrappedDataWatcher.Serializer serializer, Object value) {
        try {
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(index, serializer), value);
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
        return watcher;
    }

    public static PacketContainer getEntityRotation(int entityId, float yaw, float pitch) {
        byte rotationYaw = (byte) (yaw * 256 / 360);
        byte rotationPitch = (byte) (pitch * 256 / 360);

        PacketContainer entityLookPacket = createPacketSafe(PacketType.Play.Server.ENTITY_LOOK);
        entityLookPacket.getIntegers().write(0, entityId);
        entityLookPacket.getBytes()
                .write(0, rotationYaw)
                .write(1, rotationPitch);
        entityLookPacket.getBooleans().write(0, true);
        return entityLookPacket;
    }

    public static PacketContainer getEntityVelocity(int entityId, Vector vector) {
        PacketContainer packet = createPacketSafe(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers().write(0, entityId);
        packet.getIntegers()
                .write(1, convertVelocity(vector.getX()))
                .write(2, convertVelocity(vector.getY()))
                .write(3, convertVelocity(vector.getZ()));
        return packet;
    }

    public static PacketContainer getEquipmentPacket(int entityId, Pair<EnumWrappers.ItemSlot, ItemStack>... items) {
        try {
            PacketContainer packet = createPacketSafe(PacketType.Play.Server.ENTITY_EQUIPMENT);

            packet.getIntegers().write(0, entityId);
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = Arrays.asList(items);
            packet.getSlotStackPairLists().writeSafely(0, list);
            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return null;
        }
    }

    public static PacketContainer teleportEntityPacket(int entityID, Location location) {
        PacketContainer syncPacket = createPositionSyncPacket(entityID, location, new Vector(0, 0, 0), false);
        if (syncPacket != null) {
            return syncPacket;
        }

        return createLegacyTeleportPacket(entityID, location, false);
    }

    private static PacketContainer createLegacyTeleportPacket(int entityID, Location location, boolean onGround) {
        try {
            PacketContainer packet = createPacketSafe(PacketType.Play.Server.ENTITY_TELEPORT);
            packet.getModifier().writeDefaults();

            packet.getIntegers().writeSafely(0, entityID);
            packet.getDoubles().writeSafely(0, location.getX());
            packet.getDoubles().writeSafely(1, location.getY());
            packet.getDoubles().writeSafely(2, location.getZ());
            packet.getBytes().writeSafely(0, (byte) (location.getYaw() * 256.0F / 360.0F));
            packet.getBytes().writeSafely(1, (byte) (location.getPitch() * 256.0F / 360.0F));
            packet.getBooleans().writeSafely(0, onGround);
            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return null;
        }
    }

    private static PacketContainer createPositionSyncPacket(int entityID, Location location, Vector velocity, boolean onGround) {
        try {
            PacketType syncType = getServerPacketType("ENTITY_POSITION_SYNC");
            if (syncType == null) {
                return null;
            }

            PacketContainer packet = createPacketSafe(syncType);
            packet.getModifier().writeDefaults();

            packet.getIntegers().writeSafely(0, entityID);

            InternalStructure move = packet.getStructures().readSafely(0);
            if (move != null) {
                move.getVectors().writeSafely(0, new Vector(location.getX(), location.getY(), location.getZ()));
                move.getVectors().writeSafely(1, velocity == null ? new Vector(0, 0, 0) : velocity);
                // Position sync uses non-compressed angles (float degrees).
                move.getFloat().writeSafely(0, location.getYaw());
                move.getFloat().writeSafely(1, location.getPitch());
            }

            packet.getBooleans().writeSafely(0, onGround);

            StructureModifier<Set> setModifier = packet.getModifier().withType(Set.class);
            if (setModifier.size() > 0) {
                setModifier.writeSafely(0, Collections.emptySet());
            }

            return packet;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return null;
        }
    }

    private static PacketType getServerPacketType(String fieldName) {
        try {
            java.lang.reflect.Field field = PacketType.Play.Server.class.getField(fieldName);
            Object packetType = field.get(null);
            if (packetType instanceof PacketType) {
                return (PacketType) packetType;
            }
        } catch (Exception ignored) {
        }
        return null;
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
            PacketContainer packet = createPacketSafe(PacketType.Play.Server.ENTITY_DESTROY);
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
            PacketContainer pc = createPacketSafe(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            pc.getModifier().writeDefaults();
            pc.getIntegers().write(0, entityId);
            pc.getBytes().write(0, (byte) getCompressedAngle(location.getYaw()));

            return pc;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return null;
        }
    }

    public static PacketContainer getHeadLookPacket(int entityId, Location location) {
        try {
            PacketContainer pc = createPacketSafe(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
            pc.getModifier().writeDefaults();
            pc.getIntegers().write(0, entityId);
            pc.getBytes().write(0, (byte) location.getYaw());
            pc.getBooleans().write(0, false);

            return pc;
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            return null;
        }
    }

    public static PacketContainer getPassengerPacket(int vehicleId, int passengerCount, int... passengers) {
        try {
            PacketContainer pc = createPacketSafe(PacketType.Play.Server.MOUNT);

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

    /**
     * Display metadata layout changed in 1.20+, so OLD adapter offset must never be used there.
     */
    public static int getDisplayMetadataOffset() {
        if (Utils.versionIsAfterOrEqual(20)) {
            if (!loggedDisplayAdapterOverride && Main.getInstance() != null && Main.getInstance().isVersionAdapter()) {
                Main.getInstance().getLogger().warning("[PacketUtils] OLD version-adapter is ignored for Display metadata on MC 1.20+.");
                loggedDisplayAdapterOverride = true;
            }
            return 0;
        }

        if (Main.getInstance() != null && Main.getInstance().isVersionAdapter()) {
            return -1;
        }

        return 0;
    }

    /**
     * Uses client protocol when available so ViaVersion clients get the correct Display metadata layout.
     */
    public static int getDisplayMetadataOffset(Player player) {
        if (player == null) {
            return getDisplayMetadataOffset();
        }

        try {
            Integer viaProtocol = getViaClientProtocol(player);
            if (viaProtocol != null && viaProtocol >= CLIENT_PROTOCOL_1_20) {
                return 0;
            }

            ProtocolManager manager = getProtocolManagerSafe();
            if (manager != null) {
                int clientProtocol = manager.getProtocolVersion(player);
                if (clientProtocol >= CLIENT_PROTOCOL_1_20) {
                    return 0;
                }
            }
        } catch (Exception ignored) {
            // Fall back to server-based logic.
        }

        return getDisplayMetadataOffset();
    }

    private static Integer getViaClientProtocol(Player player) {
        try {
            Class<?> viaClass;
            try {
                viaClass = Class.forName("com.viaversion.viaversion.api.Via");
            } catch (ClassNotFoundException ignored) {
                viaClass = Class.forName("us.myles.ViaVersion.api.Via");
            }

            Object api = viaClass.getMethod("getAPI").invoke(null);
            Object result = api.getClass().getMethod("getPlayerVersion", java.util.UUID.class).invoke(api, player.getUniqueId());
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
