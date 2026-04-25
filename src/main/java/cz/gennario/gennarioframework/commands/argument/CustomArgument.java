package cz.gennario.gennarioframework.commands.argument;

import java.util.function.Function;

/**
 * Custom argument - allows defining your own parser for any type.
 *
 * <p>Example:</p>
 * <pre>
 * Argument.custom("gamemode", GameMode.class, input -> {
 *     return switch(input.toLowerCase()) {
 *         case "survival", "0" -> GameMode.SURVIVAL;
 *         case "creative", "1" -> GameMode.CREATIVE;
 *         default -> null;
 *     };
 * })
 * </pre>
 */
public class CustomArgument<T> extends Argument<T> {

    private final Function<String, T> parser;
    private String typeName = "custom";

    public CustomArgument(String name, Class<T> type, Function<String, T> parser) {
        super(name, type);
        this.parser = parser;
    }

    public CustomArgument<T> typeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    @Override
    public T parse(String input) throws ArgumentParseException {
        T result = parser.apply(input);
        if (result == null) {
            throw new ArgumentParseException(input, typeName);
        }
        return result;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }
}
