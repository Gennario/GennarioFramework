package cz.gennario.gennarioframework.utils.packet.types;

import cz.gennario.gennarioframework.utils.packet.entity.PacketEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PacketSlime extends PacketEntity {

    private int size;

    public PacketSlime(int size, Location location) {
        super();

        this.size = size;

        setLocation(location);
        setEntityType(EntityType.SLIME);
        setInvisible(true);
    }

    public void spawn(Player player) {
        this.spawnEntity(player);
        updateSlimeSize(player, size);
        updateRotation(player);
    }

}
