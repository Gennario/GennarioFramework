package cz.gennario.gennarioframework.world;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import cz.gennario.gennarioframework.utils.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManagerImpl implements WorldManager {
    private final Plugin plugin;
    private final Map<String, World> loadedWorlds = new ConcurrentHashMap<>();

    public WorldManagerImpl(Plugin plugin, Iterable<WorldConfig> permanentConfigs) {
        this.plugin = plugin;
        permanentConfigs.forEach(cfg -> {
            if (cfg.getType() == WorldType.PERMANENT) {
                loadWorldAsync(cfg);
            }
        });
    }

    @Override
    public CompletableFuture<World> loadWorldAsync(WorldConfig config) {
        CompletableFuture<World> future = new CompletableFuture<>();
        FoliaScheduler.runSync(plugin, () -> {
            try {
                String name = config.getName();
                if (loadedWorlds.containsKey(name)) {
                    future.complete(loadedWorlds.get(name));
                    return;
                }

                WorldCreator creator = new WorldCreator(name);
                if (config.getGenerator() != null) {
                    creator.generator(config.getGenerator());
                }
                World world = creator.createWorld();
                loadedWorlds.put(name, world);

                if (config.getGenerator() == null && config.getSchematicPath() != null) {
                    pasteSchematicAsync(world, config.getSchematicPath().toFile(), world.getSpawnLocation());
                }

                future.complete(world);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private void pasteSchematicAsync(World world, File schematicFile, Location location) {
        FoliaScheduler.runAsync(plugin, () -> {
            try (FileInputStream fis = new FileInputStream(schematicFile)) {
                ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(fis);
                Clipboard clipboard = reader.read();

                com.sk89q.worldedit.world.World world1 = FaweAPI.getWorld(world.getName());

                try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world1, -1)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                            .ignoreAirBlocks(true)
                            .build();

                    Operations.completeLegacy(operation);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to paste schematic: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteWorldAsync(String worldName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        FoliaScheduler.runSync(plugin, () -> {
            World world = loadedWorlds.remove(worldName);
            if (world != null) {
                Bukkit.unloadWorld(world, false);
                FoliaScheduler.runAsync(plugin, () -> {
                    try {
                        Path worldDir = world.getWorldFolder().toPath();
                        Files.walk(worldDir)
                                .sorted((a, b) -> b.compareTo(a))
                                .forEach(path -> path.toFile().delete());
                        future.complete(null);
                    } catch (Exception ex) {
                        plugin.getLogger().warning("Failed to delete world files: " + ex.getMessage());
                        future.completeExceptionally(ex);
                    }
                });
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    @Override
    public void preloadPermanentWorlds() {
        // Permanent worlds already loaded in constructor
    }
}