package cz.gennario.gennarioframework.commands.argument;

/**
 * String argument - accepts any single word.
 */
public class StringArgument extends Argument<String> {

    public StringArgument(String name) {
        super(name, String.class);
    }

    @Override
    public String parse(String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            throw new ArgumentParseException(input, "a text value");
        }
        return input;
    }

    @Override
    public String getTypeName() {
        return "string";
    }
}
