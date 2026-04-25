package cz.gennario.gennarioframework.commands.platform;

/**
 * Platform abstraction for command registration.
 * Allows switching between Bukkit legacy and Brigadier (Paper 1.21.8+).
 */
public interface CommandPlatform {

    /**
     * Register the command on the server.
     */
    void register();

    /**
     * Unregister the command from the server.
     */
    void unregister();

}
