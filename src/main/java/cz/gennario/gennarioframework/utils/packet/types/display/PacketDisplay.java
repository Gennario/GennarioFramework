package cz.gennario.gennarioframework.utils.packet.types.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;

import java.util.List;

@Getter
public abstract class PacketDisplay extends PacketEntity {

    private int interpolationDelay, transformationInterpolationDelay, positionInterpolationDelay, brightness;
    private org.joml.Vector3f translation, scale;
    private Quaternionf rotationLeft, rotationRight;
    private float viewRange, shadowRadius, shadowStrength, width, height;
    private Display.Billboard billboard;
    private int glowColorOverride = -1; // -1 = no override (default)

    public PacketDisplay() {
        super();
    }

    protected List<EntityData<?>> getDisplay(Player player, EntityType entityType) {
        sendSpawn(player, entityType);
        return updateDisplay(player);
    }

    protected List<EntityData<?>> updateDisplay(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        super.updateEntity(player, metadata);

        int o = versionOverwrite(player);

        if (interpolationDelay != 0) PacketUtils.addMetadata(metadata, 8+o, EntityDataTypes.INT, interpolationDelay);
        if (transformationInterpolationDelay != 0) PacketUtils.addMetadata(metadata, 9+o, EntityDataTypes.INT, transformationInterpolationDelay);
        if (positionInterpolationDelay != 0) PacketUtils.addMetadata(metadata, 10+o, EntityDataTypes.INT, positionInterpolationDelay);

        if (translation != null) PacketUtils.addMetadata(metadata, 11+o, EntityDataTypes.VECTOR3F, toVec(translation));
        if (scale != null) PacketUtils.addMetadata(metadata, 12+o, EntityDataTypes.VECTOR3F, toVec(scale));

        if (rotationLeft != null) PacketUtils.addMetadata(metadata, 13+o, EntityDataTypes.QUATERNION, toQuat(rotationLeft));
        if (rotationRight != null) PacketUtils.addMetadata(metadata, 14+o, EntityDataTypes.QUATERNION, toQuat(rotationRight));

        if (billboard != null) PacketUtils.addMetadata(metadata, 15+o, EntityDataTypes.BYTE, (byte) billboard.ordinal());
        if (brightness != 0) PacketUtils.addMetadata(metadata, 16+o, EntityDataTypes.INT, brightness);
        if (viewRange != 0) PacketUtils.addMetadata(metadata, 17+o, EntityDataTypes.FLOAT, viewRange);
        if (shadowRadius != 0) PacketUtils.addMetadata(metadata, 18+o, EntityDataTypes.FLOAT, shadowRadius);
        if (shadowStrength != 0) PacketUtils.addMetadata(metadata, 19+o, EntityDataTypes.FLOAT, shadowStrength);
        if (width != 0) PacketUtils.addMetadata(metadata, 20+o, EntityDataTypes.FLOAT, width);
        if (height != 0) PacketUtils.addMetadata(metadata, 21+o, EntityDataTypes.FLOAT, height);
        if (glowColorOverride != -1) PacketUtils.addMetadata(metadata, 22+o, EntityDataTypes.INT, glowColorOverride);

        return metadata;
    }

    protected Display.Billboard getPacketBillboard(byte b) {
        if (b == 1) return Display.Billboard.VERTICAL;
        if (b == 2) return Display.Billboard.HORIZONTAL;
        if (b == 3) return Display.Billboard.CENTER;
        return Display.Billboard.FIXED;
    }

    @Override
    public void delete(Player player) {
        PacketUtils.sendDestroyPacket(player, getEntityId());
    }

    @Override
    public void teleport(Player player, Location location) {
        PacketUtils.sendTeleportPacket(player, getEntityId(), location);
        setLocation(location);
    }

    public void teleportWithoutOverwrite(Player player, Location location) {
        PacketUtils.sendTeleportPacket(player, getEntityId(), location);
    }

    public void moveHere(Player player) {
        moveLocation(player, player.getLocation());
    }

    public void moveLocation(Player player, Location location) {
        setLocation(location);
        PacketUtils.sendTeleportPacket(player, getEntityId(), location);
    }

    public PacketDisplay updateInterpolationDelay(Player player) {
        return updateInterpolationDelay(player, interpolationDelay);
    }

    public PacketDisplay updateInterpolationDelay(Player player, int interpolationDelay) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (interpolationDelay != 0) PacketUtils.addMetadata(metadata, 8+versionOverwrite(player), EntityDataTypes.INT, interpolationDelay);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateTransformationInterpolationDelay(Player player) {
        return updateTransformationInterpolationDelay(player, transformationInterpolationDelay);
    }

    public PacketDisplay updateTransformationInterpolationDelay(Player player, int transformationInterpolationDelay) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (transformationInterpolationDelay != 0) PacketUtils.addMetadata(metadata, 9+versionOverwrite(player), EntityDataTypes.INT, transformationInterpolationDelay);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updatePositionInterpolationDelay(Player player) {
        return updatePositionInterpolationDelay(player, positionInterpolationDelay);
    }

    public PacketDisplay updatePositionInterpolationDelay(Player player, int positionInterpolationDelay) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (positionInterpolationDelay != 0) PacketUtils.addMetadata(metadata, 10+versionOverwrite(player), EntityDataTypes.INT, positionInterpolationDelay);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateTranslation(Player player) {
        return updateTranslation(player, translation);
    }

    public PacketDisplay updateTranslation(Player player, org.joml.Vector3f translation) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (translation != null) PacketUtils.addMetadata(metadata, 11+versionOverwrite(player), EntityDataTypes.VECTOR3F, toVec(translation));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateScale(Player player) {
        return updateScale(player, scale);
    }

    public PacketDisplay updateScale(Player player, double scale) {
        return updateScale(player, new org.joml.Vector3f((float) scale));
    }

    public PacketDisplay updateScale(Player player, org.joml.Vector3f scale) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (scale != null) PacketUtils.addMetadata(metadata, 12+versionOverwrite(player), EntityDataTypes.VECTOR3F, toVec(scale));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateRotationLeft(Player player) {
        return updateRotationLeft(player, rotationLeft);
    }

    public PacketDisplay updateRotationLeft(Player player, Quaternionf rotationLeft) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (rotationLeft != null) PacketUtils.addMetadata(metadata, 13+versionOverwrite(player), EntityDataTypes.QUATERNION, toQuat(rotationLeft));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateRotationRight(Player player) {
        return updateRotationRight(player, rotationRight);
    }

    public PacketDisplay updateRotationRight(Player player, Quaternionf rotationRight) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (rotationRight != null) PacketUtils.addMetadata(metadata, 14+versionOverwrite(player), EntityDataTypes.QUATERNION, toQuat(rotationRight));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateBillboard(Player player) {
        return updateBillboard(player, billboard);
    }

    public PacketDisplay updateBillboard(Player player, Display.Billboard billboard) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (billboard != null) PacketUtils.addMetadata(metadata, 15+versionOverwrite(player), EntityDataTypes.BYTE, (byte) billboard.ordinal());
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateBrightness(Player player) {
        return updateBrightness(player, brightness);
    }

    public PacketDisplay updateBrightness(Player player, int brightness) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (brightness != 0) PacketUtils.addMetadata(metadata, 16+versionOverwrite(player), EntityDataTypes.INT, brightness);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateViewRange(Player player) {
        return updateViewRange(player, viewRange);
    }

    public PacketDisplay updateViewRange(Player player, float viewRange) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (viewRange != 0) PacketUtils.addMetadata(metadata, 17+versionOverwrite(player), EntityDataTypes.FLOAT, viewRange);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateShadowRadius(Player player) {
        return updateShadowRadius(player, shadowRadius);
    }

    public PacketDisplay updateShadowRadius(Player player, float shadowRadius) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (shadowRadius != 0) PacketUtils.addMetadata(metadata, 18+versionOverwrite(player), EntityDataTypes.FLOAT, shadowRadius);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public void updateShadowStrength(Player player) {
        updateShadowStrength(player, shadowStrength);
    }

    public PacketDisplay updateShadowStrength(Player player, float shadowStrength) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (shadowStrength != 0) PacketUtils.addMetadata(metadata, 19+versionOverwrite(player), EntityDataTypes.FLOAT, shadowStrength);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateWidth(Player player) {
        return updateWidth(player, width);
    }

    public PacketDisplay updateWidth(Player player, float width) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (width != 0) PacketUtils.addMetadata(metadata, 20+versionOverwrite(player), EntityDataTypes.FLOAT, width);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateHeight(Player player) {
        return updateHeight(player, height);
    }

    public PacketDisplay updateHeight(Player player, float height) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        if (height != 0) PacketUtils.addMetadata(metadata, 21+versionOverwrite(player), EntityDataTypes.FLOAT, height);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketDisplay updateGlowColorOverride(Player player) {
        return updateGlowColorOverride(player, glowColorOverride);
    }

    /** Sets the glow outline color. Pass -1 to remove the override (default team color). */
    public PacketDisplay updateGlowColorOverride(Player player, int argbColor) {
        this.glowColorOverride = argbColor;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 22+versionOverwrite(player), EntityDataTypes.INT, argbColor);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    /** Sets the glow outline color using separate RGB components (fully opaque). */
    public PacketDisplay updateGlowColorOverride(Player player, int red, int green, int blue) {
        return updateGlowColorOverride(player, (0xFF << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    /** Removes the glow color override (reverts to team color). */
    public PacketDisplay removeGlowColorOverride(Player player) {
        return updateGlowColorOverride(player, -1);
    }

    public PacketDisplay updateTransform(Player player, org.bukkit.util.Transformation transform) {
        return updateTransform(player, transform.getTranslation(), transform.getLeftRotation(), transform.getScale(), transform.getRightRotation());
    }

    public PacketDisplay updateTransform(Player player, org.joml.Vector3f translation, Quaternionf rotationLeft, org.joml.Vector3f scale, Quaternionf rotationRight) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        int o = versionOverwrite(player);

        if (translation != null) PacketUtils.addMetadata(metadata, 11+o, EntityDataTypes.VECTOR3F, toVec(translation));
        if (scale != null) PacketUtils.addMetadata(metadata, 12+o, EntityDataTypes.VECTOR3F, toVec(scale));
        if (rotationLeft != null) PacketUtils.addMetadata(metadata, 13+o, EntityDataTypes.QUATERNION, toQuat(rotationLeft));
        if (rotationRight != null) PacketUtils.addMetadata(metadata, 14+o, EntityDataTypes.QUATERNION, toQuat(rotationRight));

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    // ── Setters ───────────────────────────────────────────────────────────────

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

    public PacketDisplay setTranslation(org.joml.Vector3f translation) {
        this.translation = translation;
        return this;
    }

    public PacketDisplay setScale(org.joml.Vector3f vector) {
        this.scale = vector;
        return this;
    }

    public PacketDisplay setScale(double scale) {
        return setScale(new org.joml.Vector3f((float) scale));
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

    /** Sets glow color override (-1 = no override). Applied on next spawn()/update(). */
    public PacketDisplay setGlowColorOverride(int argbColor) {
        this.glowColorOverride = argbColor;
        return this;
    }

    /** Sets glow color override using separate RGB components (fully opaque). */
    public PacketDisplay setGlowColorOverride(int red, int green, int blue) {
        return setGlowColorOverride((0xFF << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    public int versionOverwrite() {
        return PacketUtils.getDisplayMetadataOffset();
    }

    protected int versionOverwrite(Player player) {
        return PacketUtils.getDisplayMetadataOffset(player);
    }

    // ── Conversion helpers ────────────────────────────────────────────────────

    protected static Vector3f toVec(org.joml.Vector3f v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    protected static Quaternion4f toQuat(Quaternionf q) {
        return new Quaternion4f(q.x, q.y, q.z, q.w);
    }
}
