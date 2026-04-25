package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EntityType argument - resolves to a Bukkit EntityType.
 */
public class EntityTypeArgument extends Argument<EntityType> {

    public EntityTypeArgument(String name) {
        super(name, EntityType.class);
    }

    @Override
    public EntityType parse(String input) throws ArgumentParseException {
        try {
            return EntityType.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ArgumentParseException(input, "entity type", e);
        }
    }

    @Override
    public String getTypeName() {
        return "entity_type";
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return Arrays.stream(EntityType.values())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toList());
    }
}
