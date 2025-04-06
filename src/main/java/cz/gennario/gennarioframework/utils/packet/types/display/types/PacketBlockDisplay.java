package cz.gennario.gennarioframework.utils.packet.types.display.types;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.types.display.PacketDisplay;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Setter
@Getter
public class PacketBlockDisplay extends PacketDisplay {

    /* For hide & show */
    protected BlockData saveBlockData;

    /* Others */
    private BlockData blockData;
    private Material blockMaterial;

    public PacketBlockDisplay() {
        super();
    }

    public void spawn(Player player) {
        /* DATA WATCHER */
        WrappedDataWatcher dataWatcher = getDisplay(player, EntityType.BLOCK_DISPLAY);

        update(player, dataWatcher);
    }

    public void update(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        update(player, dataWatcher);
    }

    private void update(Player player, WrappedDataWatcher dataWatcher) {
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getBlockDataSerializer(false);
        if (blockData != null) PacketUtils.setMetadata(dataWatcher, 23+versionOverwrite(), serializer, blockData);
        if (blockMaterial != null) PacketUtils.setMetadata(dataWatcher, 23+versionOverwrite(), serializer, blockMaterial.createBlockData());

        /* SEND PACKET */
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
    }

    /* HIDE BLOCK DISPLAY */
    public void hideDisplay(Player player) {
        this.saveBlockData = blockData;
        updateBlockMaterial(player, Material.AIR);
    }

    /* SHOW BLOCK DISPLAY */
    public void showDisplay(Player player) {
        updateBlockData(player, saveBlockData);
    }

    /* UPDATE SPECIFIC THINGS */


    /* BLOCK DATA */
    public PacketBlockDisplay updateBlockData(Player player) {
        return updateBlockData(player, blockData);
    }

    public PacketBlockDisplay updateBlockData(Player player, BlockData blockData) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getBlockDataSerializer(false);
        if (blockData != null) PacketUtils.setMetadata(dataWatcher, 23+versionOverwrite(), serializer, blockData);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));;
        return this;
    }

    /* MATERIAL */
    public PacketBlockDisplay updateBlockMaterial(Player player) {
        return updateBlockData(player, blockMaterial.createBlockData());
    }

    public PacketBlockDisplay updateBlockMaterial(Player player, Material material) {
        return updateBlockData(player, material.createBlockData());
    }

    /* SETTER */
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
        if(Main.getInstance().isVersionAdapter()) return -1;
        return 0;
    }
}
