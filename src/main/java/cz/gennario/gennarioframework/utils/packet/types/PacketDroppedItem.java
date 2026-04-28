package cz.gennario.gennarioframework.utils.packet.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

@Setter
@Getter
public class PacketDroppedItem extends PacketEntity {

    private ItemStack item;

    public PacketDroppedItem() {
        super();

        this.item = new ItemStack(Material.STONE);

        setGravity(true);
        setSilent(true);
    }

    public PacketDroppedItem(Location location) {
        this();
        setLocation(location);
    }

    public PacketDroppedItem(Location location, ItemStack item) {
        this();
        setLocation(location);
        this.item = item;
    }

    public PacketDroppedItem(Location location, ItemStack item, Vector velocity) {
        this();
        setLocation(location);
        this.item = item;
        setVelocity(velocity);
    }

    public void spawn(Player player) {
        sendSpawn(player, EntityType.ITEM);
        updateDroppedItem(player);

        if (getVelocity() != null) {
            updateVelocity(player);
        }
    }

    protected void updateDroppedItem(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        super.updateEntity(player, metadata);

        if (item != null) {
            PacketUtils.addMetadata(metadata, 8, EntityDataTypes.ITEMSTACK,
                    SpigotConversionUtil.fromBukkitItemStack(item));
        }

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    public PacketDroppedItem updateItem(Player player) {
        return updateItem(player, item);
    }

    public PacketDroppedItem updateItem(Player player, ItemStack item) {
        this.item = item;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();

        if (item != null) {
            PacketUtils.addMetadata(metadata, 8, EntityDataTypes.ITEMSTACK,
                    SpigotConversionUtil.fromBukkitItemStack(item));
        }

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public void spawnWithThrow(Player player, Vector throwDirection) {
        setVelocity(throwDirection);
        spawn(player);
    }

    public void spawnWithRandomThrow(Player player) {
        double randomX = (Math.random() - 0.5) * 0.4;
        double randomY = Math.random() * 0.2 + 0.1;
        double randomZ = (Math.random() - 0.5) * 0.4;
        spawnWithThrow(player, new Vector(randomX, randomY, randomZ));
    }

    @Override
    public PacketDroppedItem updateVelocity(Player player) {
        super.updateVelocity(player);
        return this;
    }

    @Override
    public PacketDroppedItem updateVelocity(Player player, Vector vector) {
        super.updateVelocity(player, vector);
        return this;
    }

    public PacketDroppedItem setItem(ItemStack item) {
        this.item = item;
        return this;
    }

    public PacketDroppedItem setItemMaterial(Material material) {
        this.item = new ItemStack(material);
        return this;
    }

    public PacketDroppedItem setItemMaterial(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        return this;
    }

    @Override
    public PacketDroppedItem setLocation(Location location) {
        super.setLocation(location);
        return this;
    }

    @Override
    public PacketDroppedItem setVelocity(Vector velocity) {
        super.setVelocity(velocity);
        return this;
    }

    @Override
    public PacketDroppedItem setGravity(boolean gravity) {
        super.setGravity(gravity);
        return this;
    }

    @Override
    public PacketDroppedItem setGlowing(boolean glowing) {
        super.setGlowing(glowing);
        return this;
    }

    @Override
    public PacketDroppedItem setInvisible(boolean invisible) {
        super.setInvisible(invisible);
        return this;
    }

    @Override
    public PacketDroppedItem setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public PacketDroppedItem setShowName(boolean showName) {
        super.setShowName(showName);
        return this;
    }
}
