package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

/**
 * World argument - resolves to a Bukkit World.
 */
public class WorldArgument extends Argument<World> {

    public WorldArgument(String name) {
        super(name, World.class);
    }

    @Override
    public World parse(String input) throws ArgumentParseException {
        World world = Bukkit.getWorld(input);
        if (world == null) {
            throw new ArgumentParseException(input, "world name");
        }
        return world;
    }

    @Override
    public String getTypeName() {
        return "world";
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());
    }
}
