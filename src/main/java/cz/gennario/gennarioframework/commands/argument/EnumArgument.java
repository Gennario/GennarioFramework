package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum argument - resolves to any enum type.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumArgument<E extends Enum<?>> extends Argument<E> {

    private final Class<? extends Enum<?>> enumClass;

    public EnumArgument(String name, Class<? extends Enum<?>> enumClass) {
        super(name, (Class<E>) enumClass);
        this.enumClass = enumClass;
    }

    @Override
    public E parse(String input) throws ArgumentParseException {
        try {
            Class rawClass = enumClass;
            return (E) Enum.valueOf(rawClass, input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ArgumentParseException(input, enumClass.getSimpleName() + " value", e);
        }
    }

    @Override
    public String getTypeName() {
        return enumClass.getSimpleName().toLowerCase();
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toList());
    }
}
