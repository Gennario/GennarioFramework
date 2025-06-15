package cz.gennario.gennarioframework.world;

import org.bukkit.generator.ChunkGenerator;

import java.nio.file.Path;

/**
 * Fluent Builder for WorldConfig.
 */
public class WorldConfigBuilder {
    private String name;
    private WorldType type = WorldType.TEMPORARY;
    private ChunkGenerator generator = null;
    private Path schematicPath = null;

    public WorldConfigBuilder name(String name) {
        this.name = name;
        return this;
    }

    public WorldConfigBuilder type(WorldType type) {
        this.type = type;
        return this;
    }

    public WorldConfigBuilder generator(ChunkGenerator generator) {
        this.generator = generator;
        return this;
    }

    public WorldConfigBuilder schematic(Path schematicPath) {
        this.schematicPath = schematicPath;
        return this;
    }

    public WorldConfig build() {
        return new WorldConfig(name, type, generator, schematicPath);
    }
}
