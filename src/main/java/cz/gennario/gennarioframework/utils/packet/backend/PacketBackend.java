package cz.gennario.gennarioframework.utils.packet.backend;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public interface PacketBackend {

    PacketBackendMode mode();

    boolean isAvailable();

    default void init() {
    }

    default void shutdown() {
    }

    void sendPacket(Player player, PacketContainer packet) throws Exception;
}


