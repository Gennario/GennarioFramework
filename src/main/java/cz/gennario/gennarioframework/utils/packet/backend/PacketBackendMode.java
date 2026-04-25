package cz.gennario.gennarioframework.utils.packet.backend;

public enum PacketBackendMode {
    AUTO,
    PROTOCOLLIB,
    PACKETEVENTS;

    public static PacketBackendMode fromString(String value, PacketBackendMode fallback) {
        if (value == null) return fallback;
        try {
            return PacketBackendMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}

