package cz.gennario.gennarioframework.world;

import org.bukkit.generator.ChunkGenerator;

import java.nio.file.Path;

/**
 * Configuration for creating or loading a world.
 */
public class WorldConfig {
    private final String name;
    private final WorldType type;
    private final ChunkGenerator generator;
    private final Path schematicPath;

    public WorldConfig(String name, WorldType type, ChunkGenerator generator, Path schematicPath) {
        this.name = name;
        this.type = type;
        this.generator = generator;
        this.schematicPath = schematicPath;
    }

    public String getName() { return name; }
    public WorldType getType() { return type; }
    public ChunkGenerator getGenerator() { return generator; }
    public Path getSchematicPath() { return schematicPath; }
}
