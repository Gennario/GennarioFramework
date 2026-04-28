package cz.gennario.gennarioframework.utils.packet.equipment;

public enum PacketEquipmentSlot {
    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    public static PacketEquipmentSlot fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        try {
            return PacketEquipmentSlot.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

