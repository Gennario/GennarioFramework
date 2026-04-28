package cz.gennario.gennarioframework.utils.packet.types.display.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.types.display.PacketDisplay;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

@Setter
@Getter
public class PacketBlockDisplay extends PacketDisplay {

    protected BlockData saveBlockData;

    private BlockData blockData;
    private Material blockMaterial;

    public PacketBlockDisplay() {
        super();
    }

    public void spawn(Player player) {
        List<EntityData<?>> metadata = getDisplay(player, EntityType.BLOCK_DISPLAY);
        update(player, metadata);
    }

    public void update(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        update(player, metadata);
    }

    private void update(Player player, List<EntityData<?>> metadata) {
        BlockData bd = blockData != null ? blockData : (blockMaterial != null ? blockMaterial.createBlockData() : null);
        if (bd != null) PacketUtils.addMetadata(metadata, 23+versionOverwrite(player), EntityDataTypes.BLOCK_STATE,
                PacketUtils.getBlockStateId(bd));

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    public void hideDisplay(Player player) {
        this.saveBlockData = blockData;
        updateBlockMaterial(player, Material.AIR);
    }

    public void showDisplay(Player player) {
        updateBlockData(player, saveBlockData);
    }

    public PacketBlockDisplay updateBlockData(Player player) {
        return updateBlockData(player, blockData);
    }

    public PacketBlockDisplay updateBlockData(Player player, BlockData blockData) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (blockData != null) PacketUtils.addMetadata(metadata, 23+versionOverwrite(player), EntityDataTypes.BLOCK_STATE,
                PacketUtils.getBlockStateId(blockData));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketBlockDisplay updateBlockMaterial(Player player) {
        return updateBlockData(player, blockMaterial.createBlockData());
    }

    public PacketBlockDisplay updateBlockMaterial(Player player, Material material) {
        return updateBlockData(player, material.createBlockData());
    }

    public PacketBlockDisplay setBlockData(BlockData blockData) {
        this.blockData = blockData;
        return this;
    }

    public PacketBlockDisplay setBlockMaterial(Material blockMaterial) {
        this.blockMaterial = blockMaterial;
        this.blockData = blockMaterial.createBlockData();
        return this;
    }

    public int versionOverwrite() {
        return PacketUtils.getDisplayMetadataOffset();
    }
}
