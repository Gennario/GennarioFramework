package cz.gennario.gennarioframework.world;

import org.bukkit.World;
import java.util.concurrent.CompletableFuture;

/**
 * Main manager interface for world operations.
 */
public interface WorldManager {
    /**
     * Asynchronously load (or create) a world based on config.
     */
    CompletableFuture<World> loadWorldAsync(WorldConfig config);

    /**
     * Asynchronously delete/unload a world by name.
     */
    CompletableFuture<Void> deleteWorldAsync(String worldName);

    /**
     * Trigger loading of all permanent worlds (called at plugin enable).
     */
    void preloadPermanentWorlds();
}
