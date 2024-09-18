package cz.gennario.gennarioframework.utils.packet.types;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import cz.gennario.gennarioframework.utils.Utils;
import cz.gennario.gennarioframework.utils.packet.PacketUtils;
import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;

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
        /* DATA WATCHER */
        /*PacketContainer packetContainer = PacketUtils.spawnEntityPacket(EntityType.ARMOR_STAND, getLocation(), getEntityId(), getVelocity());
        if (getRotationYaw() != -1 || getRotationPitch() != -1) {
            if (getRotationPitch() != -1) entityPacketContainer.getBytes().write(0, (byte) (getRotationPitch() * 256.0F / 360.0F));
            if (getRotationYaw() != -1) entityPacketContainer.getBytes().write(1, (byte) (getRotationYaw() * 256.0F / 360.0F));
        }*/

        PacketUtils.sendPacket(player, getEntity(EntityType.ARMOR_STAND));

        updateArmorStand(player);
    }

    protected void updateArmorStand(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        super.updateEntity(player, dataWatcher);

        byte flags = 0;
        if (small) flags += (byte) 0x01;
        if (arms) flags += (byte) 0x04;
        if (!baseplate) flags += (byte) 0x08;
        if (marker) flags += (byte) 0x08;

        if (Utils.versionIsAfter(16)) {
            PacketUtils.setMetadata(dataWatcher, 15, Byte.class, flags);
        } else if(Utils.versionIsAfterOrEqual(15)) {
            PacketUtils.setMetadata(dataWatcher, 14, Byte.class, flags);
        } else {
            PacketUtils.setMetadata(dataWatcher, 11, Byte.class, flags);
        }

        if (headRotation != null) {
            int id = 16;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 12;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(headRotation.getX()), (float) Math.toDegrees(headRotation.getY()), (float) Math.toDegrees(headRotation.getZ())));
        }
        if (bodyRotation != null) {
            int id = 17;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 13;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(bodyRotation.getX()), (float) Math.toDegrees(bodyRotation.getY()), (float) Math.toDegrees(bodyRotation.getZ())));
        }
        if (leftArmRotation != null) {
            int id = 18;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 14;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(leftArmRotation.getX()), (float) Math.toDegrees(leftArmRotation.getY()), (float) Math.toDegrees(leftArmRotation.getZ())));
        }
        if (rightArmRotation != null) {
            int id = 19;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 15;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(rightArmRotation.getX()), (float) Math.toDegrees(rightArmRotation.getY()), (float) Math.toDegrees(rightArmRotation.getZ())));
        }
        if (leftArmRotation != null) {
            int id = 20;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 16;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(leftLegRotation.getX()), (float) Math.toDegrees(leftLegRotation.getY()), (float) Math.toDegrees(leftLegRotation.getZ())));
        }
        if (rightLegRotation != null) {
            int id = 21;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 17;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(rightLegRotation.getX()), (float) Math.toDegrees(rightLegRotation.getY()), (float) Math.toDegrees(rightLegRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
    }

    /* UPDATE BODY */
    public void updateBody(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (headRotation != null) {
            int id = 16;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 12;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(headRotation.getX()), (float) Math.toDegrees(headRotation.getY()), (float) Math.toDegrees(headRotation.getZ())));
        }
        if (bodyRotation != null) {
            int id = 17;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 13;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(bodyRotation.getX()), (float) Math.toDegrees(bodyRotation.getY()), (float) Math.toDegrees(bodyRotation.getZ())));
        }
        if (leftArmRotation != null) {
            int id = 18;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 14;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(leftArmRotation.getX()), (float) Math.toDegrees(leftArmRotation.getY()), (float) Math.toDegrees(leftArmRotation.getZ())));
        }
        if (rightArmRotation != null) {
            int id = 19;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 15;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(rightArmRotation.getX()), (float) Math.toDegrees(rightArmRotation.getY()), (float) Math.toDegrees(rightArmRotation.getZ())));
        }
        if (leftArmRotation != null) {
            int id = 20;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 16;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(leftLegRotation.getX()), (float) Math.toDegrees(leftLegRotation.getY()), (float) Math.toDegrees(leftLegRotation.getZ())));
        }
        if (rightLegRotation != null) {
            int id = 21;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 17;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(rightLegRotation.getX()), (float) Math.toDegrees(rightLegRotation.getY()), (float) Math.toDegrees(rightLegRotation.getZ())));
        }

        PacketContainer packet1 = PacketUtils.applyMetadata(getEntityId(), dataWatcher);
        PacketUtils.sendPacket(player, packet1);
    }

    /* UPDATE HEAD ROTATION */
    public PacketArmorStand updateHeadRotation(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (headRotation != null) {
            int id = 16;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 12;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(headRotation.getX()), (float) Math.toDegrees(headRotation.getY()), (float) Math.toDegrees(headRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE BODY ROTATION */
    public PacketArmorStand updateBodyRotation(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (bodyRotation != null) {
            int id = 17;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 13;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(bodyRotation.getX()), (float) Math.toDegrees(bodyRotation.getY()), (float) Math.toDegrees(bodyRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE LEFT ARM ROTATION */
    public PacketArmorStand updateLeftArmRotation(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (leftArmRotation != null) {
            int id = 18;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 14;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(leftArmRotation.getX()), (float) Math.toDegrees(leftArmRotation.getY()), (float) Math.toDegrees(leftArmRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE RIGHT ARM ROTATION */
    public PacketArmorStand updateRightArmRotation(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (rightArmRotation != null) {
            int id = 19;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 15;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(rightArmRotation.getX()), (float) Math.toDegrees(rightArmRotation.getY()), (float) Math.toDegrees(rightArmRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE LEFT LEG ROTATION */
    public PacketArmorStand updateLeftLegRotation(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (leftArmRotation != null) {
            int id = 20;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 16;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(leftLegRotation.getX()), (float) Math.toDegrees(leftLegRotation.getY()), (float) Math.toDegrees(leftLegRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE RIGHT LEG ROTATION */
    public PacketArmorStand updateRightLegRotation(Player player) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        if (rightLegRotation != null) {
            int id = 21;
            if (Utils.versionIsBeforeOrEqual(16)) id = id - 1;
            if (Utils.versionIsBeforeOrEqual(14)) id = 17;
            PacketUtils.setMetadata(dataWatcher, id, Vector3F.getMinecraftClass(), new Vector3F((float) Math.toDegrees(rightLegRotation.getX()), (float) Math.toDegrees(rightLegRotation.getY()), (float) Math.toDegrees(rightLegRotation.getZ())));
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* FLAGS */
    public PacketArmorStand updateArmorStandFlags(Player player, boolean small, boolean arms, boolean baseplate, boolean marker) {
        WrappedDataWatcher dataWatcher = PacketUtils.getDataWatcher();

        byte flags = 0;
        if (small) flags += (byte) 0x01;
        if (arms) flags += (byte) 0x04;
        if (!baseplate) flags += (byte) 0x08;
        if (marker) flags += (byte) 0x08;

        if (Utils.versionIsAfter(16)) {
            PacketUtils.setMetadata(dataWatcher, 15, Byte.class, flags);
        } else if(Utils.versionIsAfterOrEqual(15)) {
            PacketUtils.setMetadata(dataWatcher, 14, Byte.class, flags);
        } else {
            PacketUtils.setMetadata(dataWatcher, 11, Byte.class, flags);
        }

        PacketUtils.sendPacket(player, PacketUtils.applyMetadata(getEntityId(), dataWatcher));
        return this;
    }

    /* UPDATE SMALL */
    public PacketArmorStand updateSmall(Player player, boolean small) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    /* UPDATE ARMS */
    public PacketArmorStand updateArms(Player player, boolean arms) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    /* UPDATE BASEPLATE */
    public PacketArmorStand updateBaseplate(Player player, boolean baseplate) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    /* UPDATE MARKER */
    public PacketArmorStand updateMarker(Player player, boolean marker) {
        return updateArmorStandFlags(player, small, arms, baseplate, marker);
    }

    /* MOVE LOCATION */
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

    public PacketArmorStand setMarker(boolean marker) {
        this.marker = marker;
        return this;
    }


}
