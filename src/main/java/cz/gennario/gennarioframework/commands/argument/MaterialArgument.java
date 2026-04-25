package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Material argument - resolves to a Bukkit Material.
 */
public class MaterialArgument extends Argument<Material> {

    public MaterialArgument(String name) {
        super(name, Material.class);
    }

    @Override
    public Material parse(String input) throws ArgumentParseException {
        try {
            return Material.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ArgumentParseException(input, "material name", e);
        }
    }

    @Override
    public String getTypeName() {
        return "material";
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return Arrays.stream(Material.values())
                .filter(m -> !m.isLegacy())
                .map(m -> m.name().toLowerCase())
                .collect(Collectors.toList());
    }
}
