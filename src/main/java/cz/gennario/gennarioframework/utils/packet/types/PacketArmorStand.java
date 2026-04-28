package cz.gennario.gennarioframework.utils.packet.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;

import java.util.List;

@Setter
@Getter
public class PacketArmorStand extends PacketEntity {

    private boolean small, arms, baseplate, marker;
    private EulerAngle headRotation, bodyRotation, leftArmRotation, rightArmRotation, leftLegRotation, rightLegRotation;

    public PacketArmorStand() {
        super();

        this.small = false;
        this.arms = true;
        this.baseplate = true;
    }

    public void spawn(Player player) {
        sendSpawn(player, EntityType.ARMOR_STAND);
        updateArmorStand(player);
    }

    protected void updateArmorStand(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        super.updateEntity(player, metadata);

        byte flags = 0;
        if (small) flags += (byte) 0x01;
        if (arms) flags += (byte) 0x04;
        if (!baseplate) flags += (byte) 0x08;
        if (marker) flags += (byte) 0x10;

        PacketUtils.addMetadata(metadata, 15, EntityDataTypes.BYTE, flags);

        if (headRotation != null) PacketUtils.addMetadata(metadata, 16, EntityDataTypes.ROTATION, toVec(headRotation));
        if (bodyRotation != null) PacketUtils.addMetadata(metadata, 17, EntityDataTypes.ROTATION, toVec(bodyRotation));
        if (leftArmRotation != null) PacketUtils.addMetadata(metadata, 18, EntityDataTypes.ROTATION, toVec(leftArmRotation));
        if (rightArmRotation != null) PacketUtils.addMetadata(metadata, 19, EntityDataTypes.ROTATION, toVec(rightArmRotation));
        if (leftLegRotation != null) PacketUtils.addMetadata(metadata, 20, EntityDataTypes.ROTATION, toVec(leftLegRotation));
        if (rightLegRotation != null) PacketUtils.addMetadata(metadata, 21, EntityDataTypes.ROTATION, toVec(rightLegRotation));

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    public void updateBody(Player player) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();

        if (headRotation != null) PacketUtils.addMetadata(metadata, 16, EntityDataTypes.ROTATION, toVec(headRotation));
        if (bodyRotation != null) PacketUtils.addMetadata(metadata, 17, EntityDataTypes.ROTATION, toVec(bodyRotation));
        if (leftArmRotation != null) PacketUtils.addMetadata(metadata, 18, EntityDataTypes.ROTATION, toVec(leftArmRotation));
        if (rightArmRotation != null) PacketUtils.addMetadata(metadata, 19, EntityDataTypes.ROTATION, toVec(rightArmRotation));
        if (leftLegRotation != null) PacketUtils.addMetadata(metadata, 20, EntityDataTypes.ROTATION, toVec(leftLegRotation));
        if (rightLegRotation != null) PacketUtils.addMetadata(metadata, 21, EntityDataTypes.ROTATION, toVec(rightLegRotation));

        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
    }

    public PacketArmorStand updateHeadRotation(Player player) {
        if (headRotation == null) return this;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 16, EntityDataTypes.ROTATION, toVec(headRotation));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateBodyRotation(Player player) {
        if (bodyRotation == null) return this;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 17, EntityDataTypes.ROTATION, toVec(bodyRotation));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateLeftArmRotation(Player player) {
        if (leftArmRotation == null) return this;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 18, EntityDataTypes.ROTATION, toVec(leftArmRotation));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateRightArmRotation(Player player) {
        if (rightArmRotation == null) return this;
        return updateRightArmRotation(player, rightArmRotation.getX(), rightArmRotation.getY(), rightArmRotation.getZ());
    }

    public PacketArmorStand updateRightArmRotation(Player player, double rotationX, double rotationY, double rotationZ) {
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 19, EntityDataTypes.ROTATION,
                new Vector3f((float) Math.toDegrees(rotationX), (float) Math.toDegrees(rotationY), (float) Math.toDegrees(rotationZ)));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateLeftLegRotation(Player player) {
        if (leftLegRotation == null) return this;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 20, EntityDataTypes.ROTATION, toVec(leftLegRotation));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateRightLegRotation(Player player) {
        if (rightLegRotation == null) return this;
        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 21, EntityDataTypes.ROTATION, toVec(rightLegRotation));
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateArmorStandFlags(Player player, boolean small, boolean arms, boolean baseplate, boolean marker) {
        byte flags = 0;
        if (small) flags += (byte) 0x01;
        if (arms) flags += (byte) 0x04;
        if (!baseplate) flags += (byte) 0x08;
        if (marker) flags += (byte) 0x10;

        List<EntityData<?>> metadata = PacketUtils.createMetadata();
        PacketUtils.addMetadata(metadata, 15, EntityDataTypes.BYTE, flags);
        PacketUtils.sendMetadataPacket(player, getEntityId(), metadata);
        return this;
    }

    public PacketArmorStand updateSmall(Player player, boolean small) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    public PacketArmorStand updateArms(Player player, boolean arms) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    public PacketArmorStand updateBaseplate(Player player, boolean baseplate) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    public PacketArmorStand updateMarker(Player player, boolean marker) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    public PacketArmorStand setSmall(boolean small) {
        this.small = small;
        return this;
    }

    public PacketArmorStand setArms(boolean arms) {
        this.arms = arms;
        return this;
    }

    public PacketArmorStand setBaseplate(boolean baseplate) {
        this.baseplate = baseplate;
        return this;
    }

    public PacketArmorStand setHeadRotation(EulerAngle headRotation) {
        this.headRotation = headRotation;
        return this;
    }

    public PacketArmorStand setBodyRotation(EulerAngle bodyRotation) {
        this.bodyRotation = bodyRotation;
        return this;
    }

    public PacketArmorStand setLeftArmRotation(EulerAngle leftArmRotation) {
        this.leftArmRotation = leftArmRotation;
        return this;
    }

    public PacketArmorStand setLeftLegRotation(EulerAngle leftLegRotation) {
        this.leftLegRotation = leftLegRotation;
        return this;
    }

    public PacketArmorStand setRightArmRotation(EulerAngle rightArmRotation) {
        this.rightArmRotation = rightArmRotation;
        return this;
    }

    public PacketArmorStand setRightLegRotation(EulerAngle rightLegRotation) {
        this.rightLegRotation = rightLegRotation;
        return this;
    }

    private static Vector3f toVec(EulerAngle angle) {
        return new Vector3f(
                (float) Math.toDegrees(angle.getX()),
                (float) Math.toDegrees(angle.getY()),
                (float) Math.toDegrees(angle.getZ())
        );
    }
}
