package cz.gennario.gennarioframework.utils.packet.backend;

public interface PacketBackend {

    boolean isAvailable();

    default void init() {}

    default void shutdown() {}
}
