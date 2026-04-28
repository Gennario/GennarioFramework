package cz.gennario.gennarioframework.utils.packet.equipment;

import org.bukkit.inventory.ItemStack;

public record PacketEquipmentEntry(PacketEquipmentSlot slot, ItemStack itemStack) {

    public static PacketEquipmentEntry of(PacketEquipmentSlot slot, ItemStack itemStack) {
        return new PacketEquipmentEntry(slot, itemStack);
    }
}

