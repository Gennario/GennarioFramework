package cz.gennario.gennarioframework.utils.packet.types.display.types;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.backend.PacketBackendMode;
import cz.gennario.gennarioframework.utils.packet.types.display.PacketDisplay;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Setter
@Getter
public class PacketItemDisplay extends PacketDisplay {

    /* For hide & show */
    protected ItemStack saveItemStack;

    /* Others */
    private ItemStack itemStack;
    private ItemDisplay.ItemDisplayTransform itemDisplayTransform;

    public PacketItemDisplay() {
        super();
    }

    @Override
    public void teleport(Player player, Location location) {
        if (shouldRespawnForTeleport()) {
            respawnAt(player, location);
            return;
        }
        super.teleport(player, location);
    }

    @Override
    public void teleportWithoutOverwrite(Player player, Location location) {
        if (shouldRespawnForTeleport()) {
            respawnAt(player, location);
            return;
        }
        super.teleportWithoutOverwrite(player, location);
    }

    private boolean shouldRespawnForTeleport() {
        // Respawn workaround causes visible flicker; keep it only for ProtocolLib path.
        return Utils.versionIsAfterOrEqual(21, 2)
                && PacketUtils.getActiveBackendMode() == PacketBackendMode.PROTOCOLLIB;
    }

    private void respawnAt(Player player, Location location) {
        setLocation(location.clone());
        PacketUtils.sendPacket(player, PacketUtils.destroyEntityPacket(getEntityId()));
        WrappedDataWatcher dataWatcher = getDisplay(player, EntityType.ITEM_DISPLAY);
        update(player, dataWatcher);
    }

    public void spawn(Player player) {
        /* DATA WATCHER */
        WrappedDataWatcher dataWatcher = getDisplay(player, EntityType.ITEM_DISPLAY);
        update(player, dataWatcher);
    }

    public void update(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        update(player, dataWatcher);
    }

    private void update(Player player, WrappedDataWatcher dataWatcher) {
        if (itemStack != null) PacketUtils.setMetadata(dataWatcher, 23+versionOverwrite(player), WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);
        if (itemDisplayTransform != null) PacketUtils.setMetadata(dataWatcher, 24+versionOverwrite(player), Byte.class, getPacketItemDisplayTransform(itemDisplayTransform));

        /* SEND PACKET */
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
    }

    private byte getPacketItemDisplayTransform(ItemDisplay.ItemDisplayTransform itemDisplayTransform) {
        if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND) {
            return 1;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND) {
            return 2;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.FIRSTPERSON_LEFTHAND) {
            return 3;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.FIRSTPERSON_RIGHTHAND) {
            return 4;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.HEAD) {
            return 5;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.GUI) {
            return 6;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.GROUND) {
            return 7;
        } else if (itemDisplayTransform == ItemDisplay.ItemDisplayTransform.FIXED) {
            return 8;
        }
        return 0;
    }

    /* HIDE ITEM DISPLAY */
    public void hideDisplay(Player player) {
        this.saveItemStack = itemStack;
        updateItemStack(player, new ItemStack(Material.AIR));
    }

    /* SHOW ITEM DISPLAY */
    public void showDisplay(Player player) {
        updateItemStack(player, saveItemStack);
    }

    /* UPDATE SPECIFIC THINGS */

    /* ITEM-STACK */
    public PacketItemDisplay updateItemStack(Player player) {
        return updateItemStack(player, itemStack);
    }

    public PacketItemDisplay updateItemStack(Player player, ItemStack itemStack) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (itemStack != null) PacketUtils.setMetadata(dataWatcher, 23+versionOverwrite(player), WrappedDataWatcher.Registry.getItemStackSerializer(false), itemStack);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }


    /* ITEM-DISPLAY-TRANSFORM */

    public PacketItemDisplay updateItemDisplayTransform(Player player) {
        return updateItemDisplayTransform(player, itemDisplayTransform);
    }

    public PacketItemDisplay updateItemDisplayTransform(Player player, ItemDisplay.ItemDisplayTransform itemDisplayTransform) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (itemDisplayTransform != null) PacketUtils.setMetadata(dataWatcher, 24+versionOverwrite(player), Byte.class, getPacketItemDisplayTransform(itemDisplayTransform));

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* SETTER */
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
