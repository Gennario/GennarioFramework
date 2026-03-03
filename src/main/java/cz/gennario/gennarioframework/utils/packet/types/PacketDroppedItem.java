package cz.gennario.gennarioframework.utils.packet.types;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * PacketDroppedItem - Dropped Item entity na zemi
 * Simuluje item spadlý na zem s možností nastavení velocity, rotace atd.
 */
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

    /**
     * Spawne dropped item entitu pro hráče
     */
    public void spawn(Player player) {
        PacketUtils.sendPacket(player, getEntity(EntityType.ITEM));
        updateDroppedItem(player);

        // Apply velocity if set
        if (getVelocity() != null) {
            updateVelocity(player);
        }
    }

    /**
     * Aktualizuje metadata dropped item entity
     */
    protected void updateDroppedItem(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        super.updateEntity(player, dataWatcher);

        // Dropped Item entity metadata:
        // Index 8: Item (Slot) - ItemStack který je zobrazen
        int baseIndex = getBaseMetadataIndex();

        if (item != null) {
            PacketUtils.setMetadata(dataWatcher, baseIndex, WrappedDataWatcher.Registry.getItemStackSerializer(false), item);
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
    }

    /**
     * Získá base metadata index podle verze
     */
    private int getBaseMetadataIndex() {
        // Item entity má item metadata na indexu 8
        return 8;
    }

    /* UPDATE ITEM */
    public PacketDroppedItem updateItem(Player player) {
        return updateItem(player, item);
    }

    public PacketDroppedItem updateItem(Player player, ItemStack item) {
        this.item = item;
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (item != null) {
            PacketUtils.setMetadata(dataWatcher, getBaseMetadataIndex(), WrappedDataWatcher.Registry.getItemStackSerializer(false), item);
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* SPAWN WITH THROW EFFECT - simuluje vyhození itemu s velocity */
    public void spawnWithThrow(Player player, Vector throwDirection) {
        setVelocity(throwDirection);
        spawn(player);
    }

    /* SPAWN WITH RANDOM THROW - náhodný směr vyhození */
    public void spawnWithRandomThrow(Player player) {
        double randomX = (Math.random() - 0.5) * 0.4;
        double randomY = Math.random() * 0.2 + 0.1;
        double randomZ = (Math.random() - 0.5) * 0.4;
        Vector randomVelocity = new Vector(randomX, randomY, randomZ);
        spawnWithThrow(player, randomVelocity);
    }

    /* UPDATE VELOCITY - přepíše parent metodu pro dropped item */
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

    /* FLUENT SETTERS */
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
