package cz.gennario.gennarioframework.world;

import cz.gennario.gennarioframework.Main;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class WorldExample {

    private WorldManager worldManager;

    public void test() {
        // 1) Připravím seznam trvale načítaných světů (PERMANENT)
        List<WorldConfig> permanent = Arrays.asList(
                new WorldConfigBuilder()
                        .name("hub")
                        .type(WorldType.PERMANENT)
                        //.generator(getDefaultWorldGenerator("hub", ""))  // čistý bukkit gen
                        .build(),
                new WorldConfigBuilder()
                        .name("minigame")
                        .type(WorldType.PERMANENT)
                        .generator(null)                               // void world
                        .schematic(Path.of("arena.schem"))
                        .build()
        );

        // 2) Vytvořím manager a onEnable mu předám permanent-configs
        worldManager = new WorldManagerImpl(Main.getInstance(), permanent);

        // 3) (volitelné) ruční preload – ale implementace to již dělá v konstruktoru
        worldManager.preloadPermanentWorlds();
    }

    // Příklad příkazů pro runtime načítání/smazání:
    public void createTemporary(String name, String schematicPath) {
            WorldConfig cfg = new WorldConfigBuilder()
                    .name(name)
                    .generator(null)
                    .schematic(Path.of(schematicPath))
                    .type(WorldType.TEMPORARY)
                    .build();
            worldManager.loadWorldAsync(cfg).thenAccept(world ->
                    System.out.println("Svět " + world.getName() + " byl načten.")
            );
    }

}
