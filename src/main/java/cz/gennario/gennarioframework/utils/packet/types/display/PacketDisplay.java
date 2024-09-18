package cz.gennario.gennarioframework.utils.packet.types.display;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Getter
public abstract class PacketDisplay extends PacketEntity {

    private int interpolationDelay, transformationInterpolationDelay, positionInterpolationDelay, brightness;
    private Vector3f translation, scale;
    private Quaternionf rotationLeft, rotationRight;
    private float viewRange, shadowRadius, shadowStrength, width, height;
    private Display.Billboard billboard;
    protected PacketContainer displayPacketContainer;

    public PacketDisplay() {
        super();
    }

    protected WrappedDataWatcher getDisplay(Player player, EntityType entityType) {
        /* SPAWN */
        this.displayPacketContainer = getEntity(entityType);
        PacketUtils.sendPacket(player, displayPacketContainer);

        /* DATA WATCHER */
        return updateDisplay(player);
    }

    protected WrappedDataWatcher updateDisplay(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        super.updateEntity(player, dataWatcher);

        if (interpolationDelay != 0) PacketUtils.setMetadata(dataWatcher, 8+versionOverwrite(), Integer.class, interpolationDelay);
        if (transformationInterpolationDelay != 0) PacketUtils.setMetadata(dataWatcher, 9+versionOverwrite(), Integer.class, transformationInterpolationDelay);
        if (positionInterpolationDelay != 0) PacketUtils.setMetadata(dataWatcher, 10+versionOverwrite(), Integer.class, positionInterpolationDelay);

        if (translation != null) PacketUtils.setMetadata(dataWatcher, 11+versionOverwrite(), Vector3f.class, translation);
        if (scale != null) PacketUtils.setMetadata(dataWatcher, 12+versionOverwrite(), Vector3f.class, scale);

        if (rotationLeft != null) PacketUtils.setMetadata(dataWatcher, 13+versionOverwrite(), Quaternionf.class, rotationLeft);
        if (rotationRight != null) PacketUtils.setMetadata(dataWatcher, 14+versionOverwrite(), Quaternionf.class, rotationRight);

        if (billboard != null) PacketUtils.setMetadata(dataWatcher, 15+versionOverwrite(), Byte.class, (byte) billboard.ordinal());
        if (brightness != 0) PacketUtils.setMetadata(dataWatcher, 16+versionOverwrite(), Integer.class, brightness);
        if (viewRange != 0) PacketUtils.setMetadata(dataWatcher, 17+versionOverwrite(), Float.class, viewRange);
        if (shadowRadius != 0) PacketUtils.setMetadata(dataWatcher, 18+versionOverwrite(), Float.class, shadowRadius);
        if (shadowStrength != 0) PacketUtils.setMetadata(dataWatcher, 19+versionOverwrite(), Float.class, shadowStrength);
        if (width != 0) PacketUtils.setMetadata(dataWatcher, 20+versionOverwrite(), Float.class, width);
        if (height != 0) PacketUtils.setMetadata(dataWatcher, 21+versionOverwrite(), Float.class, height);

        this.entityPacketContainer = displayPacketContainer;
        return dataWatcher;
    }

    protected Display.Billboard getPacketBillboard(byte b) {
        if (b == 1) {
            return Display.Billboard.VERTICAL;
        } else if (b == 2) {
            return Display.Billboard.HORIZONTAL;
        } else if (b == 3) {
            return Display.Billboard.CENTER;
        }

        return Display.Billboard.FIXED;
    }

    public void delete(Player player) {
        PacketContainer packetContainer = PacketUtils.destroyEntityPacket(getEntityId());
        PacketUtils.sendPacket(player, packetContainer);
    }

    public void teleport(Player player, Location location) {
        PacketContainer packetContainer = PacketUtils.teleportEntityPacket(getEntityId(), location);
        PacketUtils.sendPacket(player, packetContainer);
        setLocation(location);
    }

    /* MOVE LOCATION */
    public void moveHere(Player player) {
        moveLocation(player, player.getLocation());
    }

    public void moveLocation(Player player, Location location) {
        setLocation(location);

        PacketContainer packet = PacketUtils.teleportEntityPacket(getEntityId(), location);
        PacketUtils.sendPacket(player, packet);
    }

    /* Interpolation delay */
    public PacketDisplay updateInterpolationDelay(Player player) {
        return updateInterpolationDelay(player, interpolationDelay);
    }

    public PacketDisplay updateInterpolationDelay(Player player, int interpolationDelay) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (interpolationDelay != 0) PacketUtils.setMetadata(dataWatcher, 8+versionOverwrite(), Integer.class, interpolationDelay);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Transformation interpolation delay */
    public PacketDisplay updateTransformationInterpolationDelay(Player player) {
        return updateTransformationInterpolationDelay(player, transformationInterpolationDelay);
    }

    public PacketDisplay updateTransformationInterpolationDelay(Player player, int transformationInterpolationDelay) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (transformationInterpolationDelay != 0) PacketUtils.setMetadata(dataWatcher, 9+versionOverwrite(), Integer.class, transformationInterpolationDelay);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Position interpolation delay */
    public PacketDisplay updatePositionInterpolationDelay(Player player) {
        return updatePositionInterpolationDelay(player, positionInterpolationDelay);
    }

    public PacketDisplay updatePositionInterpolationDelay(Player player, int positionInterpolationDelay) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (positionInterpolationDelay != 0) PacketUtils.setMetadata(dataWatcher, 10+versionOverwrite(), Integer.class, positionInterpolationDelay);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Translation */
    public PacketDisplay updateTranslation(Player player) {
        return updateTranslation(player, translation);
    }

    public PacketDisplay updateTranslation(Player player, Vector3f translation) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (translation != null) PacketUtils.setMetadata(dataWatcher, 11+versionOverwrite(), Vector3f.class, translation);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Scale */
    public PacketDisplay updateScale(Player player) {
        return updateScale(player, scale);
    }

    public PacketDisplay updateScale(Player player, double scale) {
        return updateScale(player, new Vector3f((float) scale));
    }

    public PacketDisplay updateScale(Player player, Vector3f scale) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (scale != null) PacketUtils.setMetadata(dataWatcher, 12+versionOverwrite(), Vector3f.class, scale);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Rotation left */
    public PacketDisplay updateRotationLeft(Player player) {
        return updateRotationLeft(player, rotationLeft);
    }

    public PacketDisplay updateRotationLeft(Player player, Quaternionf rotationLeft) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (rotationLeft != null) PacketUtils.setMetadata(dataWatcher, 13+versionOverwrite(), Quaternionf.class, rotationLeft);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Rotation right */
    public PacketDisplay updateRotationRight(Player player) {
        return updateRotationRight(player, rotationRight);
    }

    public PacketDisplay updateRotationRight(Player player, Quaternionf rotationRight) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (rotationRight != null) PacketUtils.setMetadata(dataWatcher, 14+versionOverwrite(), Quaternionf.class, rotationRight);

        PacketContainer packet1 = PacketUtils.applyMetadata(getEntityId(), dataWatcher);
        PacketUtils.sendPacket(player, packet1);
        return this;
    }

    /* Billboard */
    public PacketDisplay updateBillboard(Player player) {
        return updateBillboard(player, billboard);
    }

    public PacketDisplay updateBillboard(Player player, Display.Billboard billboard) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (billboard != null) PacketUtils.setMetadata(dataWatcher, 15+versionOverwrite(), Byte.class, (byte) billboard.ordinal());

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Brightness */
    public PacketDisplay updateBrightness(Player player) {
        return updateBrightness(player, brightness);
    }

    public PacketDisplay updateBrightness(Player player, int brightness) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (brightness != 0) PacketUtils.setMetadata(dataWatcher, 16+versionOverwrite(), Integer.class, brightness);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* View range */
    public PacketDisplay updateViewRange(Player player) {
        return updateViewRange(player, viewRange);
    }

    public PacketDisplay updateViewRange(Player player, float viewRange) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (viewRange != 0) PacketUtils.setMetadata(dataWatcher, 17+versionOverwrite(), Float.class, viewRange);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Shadow radius */
    public PacketDisplay updateShadowRadius(Player player) {
        return updateShadowRadius(player, shadowRadius);
    }

    public PacketDisplay updateShadowRadius(Player player, float shadowRadius) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (shadowRadius != 0) PacketUtils.setMetadata(dataWatcher, 18+versionOverwrite(), Float.class, shadowRadius);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Shadow strength */
    public void updateShadowStrength(Player player) {
        updateShadowStrength(player, shadowStrength);
    }

    public PacketDisplay updateShadowStrength(Player player, float shadowStrength) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (shadowStrength != 0) PacketUtils.setMetadata(dataWatcher, 19+versionOverwrite(), Float.class, shadowStrength);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Width */
    public PacketDisplay updateWidth(Player player) {
        return updateWidth(player, width);
    }

    public PacketDisplay updateWidth(Player player, float width) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (width != 0) PacketUtils.setMetadata(dataWatcher, 20+versionOverwrite(), Float.class, width);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* Height */
    public PacketDisplay updateHeight(Player player) {
        return updateHeight(player, height);
    }

    public PacketDisplay updateHeight(Player player, float height) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();
        if (height != 0) PacketUtils.setMetadata(dataWatcher, 21+versionOverwrite(), Float.class, height);

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }



    /* SETTER */
    public PacketDisplay setInterpolationDelay(int interpolationDelay) {
        this.interpolationDelay = interpolationDelay;
        return this;
    }

    public PacketDisplay setTransformationInterpolationDelay(int transformationInterpolationDelay) {
        this.transformationInterpolationDelay = transformationInterpolationDelay;
        return this;
    }

    public PacketDisplay setPositionInterpolationDelay(int positionInterpolationDelay) {
        this.positionInterpolationDelay = positionInterpolationDelay;
        return this;
    }

    public PacketDisplay setBrightness(int brightness) {
        this.brightness = brightness;
        return this;
    }

    public PacketDisplay setTranslation(Vector3f translation) {
        this.translation = translation;
        return this;
    }

    public PacketDisplay setScale(Vector3f vector) {
        this.scale = vector;
        return this;
    }

    public PacketDisplay setScale(double scale) {
        return setScale(new Vector3f((float) scale));
    }

    public PacketDisplay setRotationLeft(Quaternionf rotationLeft) {
        this.rotationLeft = rotationLeft;
        return this;
    }

    public PacketDisplay setRotationRight(Quaternionf rotationRight) {
        this.rotationRight = rotationRight;
        return this;
    }

    public PacketDisplay setViewRange(float viewRange) {
        this.viewRange = viewRange;
        return this;
    }

    public PacketDisplay setShadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
        return this;
    }

    public PacketDisplay setShadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
        return this;
    }

    public PacketDisplay setWidth(float width) {
        this.width = width;
        return this;
    }

    public PacketDisplay setHeight(float height) {
        this.height = height;
        return this;
    }

    public PacketDisplay setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    public PacketDisplay setBillboard(byte b) {
        this.billboard = getPacketBillboard(b);
        return this;
    }

    public int versionOverwrite() {
        if(Main.getInstance().isVersionAdapter()) return -1;
        return 0;
    }
}