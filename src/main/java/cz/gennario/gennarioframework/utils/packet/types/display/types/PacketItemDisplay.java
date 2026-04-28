package cz.gennario.gennarioframework.utils.packet.types.display.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.types.display.PacketDisplay;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Setter
@Getter
public class PacketItemDisplay extends PacketDisplay {

    protected ItemStack saveItemStack;

    private ItemStack itemStack;
    private ItemDisplay.ItemDisplayTransform itemDisplayTransform;

    public PacketItemDisplay() {
        super();
    }

    @Override
    public void teleport(Player player, Location location) {
        super.teleport(player, location);
    }

    @Override
    public void teleportWithoutOverwrite(Player player, Location location) {
        super.teleportWithoutOverwrite(player, location);
    }

    public void spawn(Player player) {
        List<EntityData<?>> metadata = getDisplay(player, EntityType.ITEM_DISPLAY);
        update(player, metadata);
    }

    public void update(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        update(player, metadata);
    }

    private void update(Player player, List<EntityData<?>> metadata) {
        if (itemStack != null) PacketUtils.addMetadata(metadata, 23+versionOverwrite(player), EntityDataTypes.ITEMSTACK,
                SpigotConversionUtil.fromBukkitItemStack(itemStack));
        if (itemDisplayTransform != null) PacketUtils.addMetadata(metadata, 24+versionOverwrite(player), EntityDataTypes.BYTE,
                getPacketItemDisplayTransform(itemDisplayTransform));

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    private byte getPacketItemDisplayTransform(ItemDisplay.ItemDisplayTransform t) {
        return switch (t) {
            case THIRDPERSON_LEFTHAND -> 1;
            case THIRDPERSON_RIGHTHAND -> 2;
            case FIRSTPERSON_LEFTHAND -> 3;
            case FIRSTPERSON_RIGHTHAND -> 4;
            case HEAD -> 5;
            case GUI -> 6;
            case GROUND -> 7;
            case FIXED -> 8;
            default -> 0;
        };
    }

    public void hideDisplay(Player player) {
        this.saveItemStack = itemStack;
        updateItemStack(player, new ItemStack(Material.AIR));
    }

    public void showDisplay(Player player) {
        updateItemStack(player, saveItemStack);
    }

    public PacketItemDisplay updateItemStack(Player player) {
        return updateItemStack(player, itemStack);
    }

    public PacketItemDisplay updateItemStack(Player player, ItemStack itemStack) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (itemStack != null) PacketUtils.addMetadata(metadata, 23+versionOverwrite(player), EntityDataTypes.ITEMSTACK,
                SpigotConversionUtil.fromBukkitItemStack(itemStack));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketItemDisplay updateItemDisplayTransform(Player player) {
        return updateItemDisplayTransform(player, itemDisplayTransform);
    }

    public PacketItemDisplay updateItemDisplayTransform(Player player, ItemDisplay.ItemDisplayTransform itemDisplayTransform) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (itemDisplayTransform != null) PacketUtils.addMetadata(metadata, 24+versionOverwrite(player), EntityDataTypes.BYTE,
                getPacketItemDisplayTransform(itemDisplayTransform));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketItemDisplay setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public PacketItemDisplay setItemDisplayTransform(ItemDisplay.ItemDisplayTransform itemDisplayTransform) {
        this.itemDisplayTransform = itemDisplayTransform;
        return this;
    }

    public int versionOverwrite() {
        return PacketUtils.getDisplayMetadataOffset();
    }
}
