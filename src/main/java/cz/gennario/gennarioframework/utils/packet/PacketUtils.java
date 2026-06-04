package cz.gennario.gennarioframework.utils.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.backend.PacketBackend;
import cz.gennario.gennarioframework.utils.packet.backend.PacketBackendMode;
import cz.gennario.gennarioframework.utils.packet.backend.PacketEventsPacketBackend;
import cz.gennario.gennarioframework.utils.packet.click.PacketClickResponse;
import cz.gennario.gennarioframework.utils.packet.equipment.PacketEquipmentEntry;
import cz.gennario.gennarioframework.utils.packet.equipment.PacketEquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public final class PacketUtils {

    private static final boolean debug = false;
    public static int MINECRAFT_VERSION = -1;
    private static final int CLIENT_PROTOCOL_1_20 = 763;
    private static boolean loggedDisplayAdapterOverride = false;
    private static PacketBackend backend;

    public static final Map<Integer, PacketClickResponse> entityClickMap = new HashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public static void init() {
        MINECRAFT_VERSION = detectMinecraftVersion();
        Main.getInstance().getLogger().info("[PacketUtils] Using PacketEvents backend, MC version: " + MINECRAFT_VERSION);
        backend = new PacketEventsPacketBackend();
        if (!backend.isAvailable()) {
            throw new IllegalStateException("[PacketUtils] PacketEvents plugin is not available! Install the PacketEvents plugin.");
        }
        backend.init();
    }

    public static void shutdown() {
        if (backend != null) backend.shutdown();
    }

    /** Kept for backwards compatibility — no-op, PacketEvents is the only backend. */
    public static void configureBackend(PacketBackendMode requestedMode, boolean fallbackEnabled, PacketBackendMode autoPriority) {}

    public static PacketBackendMode getActiveBackendMode() {
        return PacketBackendMode.PACKETEVENTS;
    }

    public static boolean isBackendFallbackEnabled() {
        return false;
    }

    private static int detectMinecraftVersion() {
        try {
            String version = Bukkit.getServer().getBukkitVersion();
            String[] parts = version.split("-")[0].split("\\.");
            return Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}
        try {
            String name = PacketEvents.getAPI().getServerManager().getVersion().getReleaseName();
            String[] parts = name.split("\\.");
            return Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}
        return 21;
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    public static List<EntityData<?>> createMetadata() {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> List<EntityData<?>> addMetadata(List<EntityData<?>> list, int index, EntityDataType<T> type, T value) {
        if (value == null) return list;
        list.add(new EntityData<>(index, type, value));
        return list;
    }

    // ── Packet sending ────────────────────────────────────────────────────────

    public static void sendSpawnPacket(Player player, EntityType entityType, Location location, int entityId, Vector velocity, float yaw, float pitch) {
        try {
            com.github.retrooper.packetevents.protocol.entity.type.EntityType peType =
                    SpigotConversionUtil.fromBukkitEntityType(entityType);
            Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());
            Optional<Vector3d> vel = velocity != null
                    ? Optional.of(new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ()))
                    : Optional.empty();
            WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(
                    entityId,
                    Optional.of(UUID.randomUUID()),
                    peType,
                    pos,
                    pitch, yaw, yaw,
                    0,
                    vel
            );
            send(player, wrapper);
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send spawn packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendMetadataPacket(Player player, int entityId, List<EntityData<?>> metadata) {
        if (metadata == null || metadata.isEmpty()) return;
        try {
            send(player, new WrapperPlayServerEntityMetadata(entityId, metadata));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send metadata packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendEquipmentPacket(Player player, int entityId, List<PacketEquipmentEntry> entries) {
        if (entries == null || entries.isEmpty()) return;
        try {
            List<Equipment> peEquipment = new ArrayList<>();
            for (PacketEquipmentEntry entry : entries) {
                EquipmentSlot slot = toPacketEventsSlot(entry.slot());
                com.github.retrooper.packetevents.protocol.item.ItemStack peItem =
                        SpigotConversionUtil.fromBukkitItemStack(entry.itemStack());
                peEquipment.add(new Equipment(slot, peItem));
            }
            send(player, new WrapperPlayServerEntityEquipment(entityId, peEquipment));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send equipment packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendDestroyPacket(Player player, int entityId) {
        try {
            send(player, new WrapperPlayServerDestroyEntities(entityId));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send destroy packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendTeleportPacket(Player player, int entityId, Location location) {
        try {
            send(player, new WrapperPlayServerEntityTeleport(
                    entityId,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    location.getYaw(), location.getPitch(),
                    false
            ));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send teleport packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendHeadRotationPacket(Player player, int entityId, float yaw) {
        try {
            send(player, new WrapperPlayServerEntityHeadLook(entityId, yaw));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send head rotation packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendBodyRotationPacket(Player player, int entityId, Location location) {
        try {
            send(player, new WrapperPlayServerEntityRotation(entityId, location.getYaw(), location.getPitch(), false));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send body rotation packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendRotationPacket(Player player, int entityId, float yaw, float pitch) {
        try {
            send(player, new WrapperPlayServerEntityRotation(entityId, yaw, pitch, false));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send rotation packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    public static void sendVelocityPacket(Player player, int entityId, Vector velocity) {
        if (velocity == null) return;
        try {
            send(player, new WrapperPlayServerEntityVelocity(
                    entityId,
                    new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ())
            ));
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("[PacketUtils] Failed to send velocity packet: " + e.getMessage());
            if (debug) e.printStackTrace();
        }
    }

    // ── Public helpers (kept for external plugin compatibility) ───────────────

    public static int generateRandomEntityId() {
        // Use IDs in range 1_000_000_000..2_147_483_647 to avoid collision with
        // real server entity IDs (server assigns sequential IDs starting from 1;
        // reaching 1 billion would require years of continuous operation).
        return 1_000_000_000 + ThreadLocalRandom.current().nextInt(1_147_483_647);
    }

    public static void teleportToLocation(Player player, Location location) {
        sendTeleportPacket(player, player.getEntityId(), location);
        sendHeadRotationPacket(player, player.getEntityId(), location.getYaw());
    }

    public static void teleportToLocation(Player sender, Entity entity, Location location) {
        sendTeleportPacket(sender, entity.getEntityId(), location);
        sendHeadRotationPacket(sender, entity.getEntityId(), location.getYaw());
    }

    public static int convertVelocity(double velocity) {
        return (int) (clamp(velocity, -3.9, 3.9) * 8000);
    }

    public static double clamp(double targetNum, double min, double max) {
        return Math.max(min, Math.min(targetNum, max));
    }

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

    public static int getDisplayMetadataOffset(Player player) {
        if (player == null) return getDisplayMetadataOffset();
        try {
            Integer viaProtocol = getViaClientProtocol(player);
            if (viaProtocol != null && viaProtocol >= CLIENT_PROTOCOL_1_20) {
                return 0;
            }
        } catch (Exception ignored) {}
        return getDisplayMetadataOffset();
    }

    public static int getBlockStateId(org.bukkit.block.data.BlockData blockData) {
        try {
            java.lang.reflect.Method getState = blockData.getClass().getMethod("getState");
            Object state = getState.invoke(blockData);
            return (int) state.getClass().getMethod("getId").invoke(state);
        } catch (Exception ignored) {}
        return 0;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static void send(Player player, com.github.retrooper.packetevents.wrapper.PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
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
            Object result = api.getClass().getMethod("getPlayerVersion", UUID.class).invoke(api, player.getUniqueId());
            if (result instanceof Integer) return (Integer) result;
        } catch (Exception ignored) {}
        return null;
    }

    private static EquipmentSlot toPacketEventsSlot(PacketEquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> EquipmentSlot.MAIN_HAND;
            case OFFHAND -> EquipmentSlot.OFF_HAND;
            case FEET -> EquipmentSlot.BOOTS;
            case LEGS -> EquipmentSlot.LEGGINGS;
            case CHEST -> EquipmentSlot.CHEST_PLATE;
            case HEAD -> EquipmentSlot.HELMET;
        };
    }
}
