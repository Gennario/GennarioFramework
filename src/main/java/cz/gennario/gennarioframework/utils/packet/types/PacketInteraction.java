package cz.gennario.gennarioframework.utils.packet.types;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * PacketInteraction - Interaction entity (přidáno v MC 1.19.4)
 * Neviditelná entita určená pro detekci interakcí hráčů.
 * Má nastavitelnou šířku a výšku hitboxu a volitelně reaguje na útoky.
 */
@Setter
@Getter
public class PacketInteraction extends PacketEntity {

    private float width;
    private float height;
    private boolean responsive;

    public PacketInteraction() {
        super();

        this.width = 1.0f;
        this.height = 1.0f;
        this.responsive = false;

        // Interaction entity je ze své podstaty neviditelná (nemá renderer),
        // takže NENASTAVUJEME setInvisible(true) - to by skrylo i hitbox ve F3+B

        setShowName(false);
    }

    public PacketInteraction(Location location) {
        this();
        setLocation(location);
    }

    public PacketInteraction(Location location, float width, float height) {
        this();
        setLocation(location);
        this.width = width;
        this.height = height;
    }

    /**
     * Spawne interaction entitu pro hráče
     */
    public void spawn(Player player) {
        PacketUtils.sendPacket(player, getEntity(EntityType.INTERACTION));
        updateInteraction(player);
    }

    /**
     * Aktualizuje metadata interaction entity
     */
    public void updateInteraction(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        super.updateEntity(player, dataWatcher);

        // Interaction entity metadata:
        // Index 8: Width (float)
        // Index 9: Height (float)
        // Index 10: Responsive (boolean) - zda entita reaguje na interakce
        int baseIndex = getBaseMetadataIndex();

        PacketUtils.setMetadata(dataWatcher, baseIndex, Float.class, width);
        PacketUtils.setMetadata(dataWatcher, baseIndex + 1, Float.class, height);
        PacketUtils.setMetadata(dataWatcher, baseIndex + 2, Boolean.class, responsive);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
    }

    /**
     * Získá base metadata index podle verze
     */
    private int getBaseMetadataIndex() {
        // Interaction entity má metadata od indexu 8
        return 8;
    }

    /* UPDATE WIDTH */
    public PacketInteraction updateWidth(Player player) {
        return updateWidth(player, width);
    }

    public PacketInteraction updateWidth(Player player, float width) {
        this.width = width;
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        PacketUtils.setMetadata(dataWatcher, getBaseMetadataIndex(), Float.class, width);
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE HEIGHT */
    public PacketInteraction updateHeight(Player player) {
        return updateHeight(player, height);
    }

    public PacketInteraction updateHeight(Player player, float height) {
        this.height = height;
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        PacketUtils.setMetadata(dataWatcher, getBaseMetadataIndex() + 1, Float.class, height);
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE RESPONSIVE */
    public PacketInteraction updateResponsive(Player player) {
        return updateResponsive(player, responsive);
    }

    public PacketInteraction updateResponsive(Player player, boolean responsive) {
        this.responsive = responsive;
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        PacketUtils.setMetadata(dataWatcher, getBaseMetadataIndex() + 2, Boolean.class, responsive);
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE SIZE (width and height) */
    public PacketInteraction updateSize(Player player, float width, float height) {
        this.width = width;
        this.height = height;
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        PacketUtils.setMetadata(dataWatcher, getBaseMetadataIndex(), Float.class, width);
        PacketUtils.setMetadata(dataWatcher, getBaseMetadataIndex() + 1, Float.class, height);
        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* FLUENT SETTERS */
    public PacketInteraction setWidth(float width) {
        this.width = width;
        return this;
    }

    public PacketInteraction setHeight(float height) {
        this.height = height;
        return this;
    }

    public PacketInteraction setResponsive(boolean responsive) {
        this.responsive = responsive;
        return this;
    }

    public PacketInteraction setSize(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }
}
