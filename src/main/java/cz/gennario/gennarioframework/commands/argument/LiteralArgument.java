package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;

import java.util.Arrays;
import java.util.List;

/**
 * Literal argument - only accepts predefined string values.
 * Useful for sub-command-like choices (e.g., "add", "remove", "set").
 */
public class LiteralArgument extends Argument<String> {

    private final List<String> allowedValues;

    public LiteralArgument(String name, String... values) {
        super(name, String.class);
        this.allowedValues = Arrays.asList(values);
    }

    @Override
    public String parse(String input) throws ArgumentParseException {
        for (String val : allowedValues) {
            if (val.equalsIgnoreCase(input)) {
                return val;
            }
        }
        throw new ArgumentParseException(input, "one of: " + String.join(", ", allowedValues));
    }

    @Override
    public String getTypeName() {
        return String.join("|", allowedValues);
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return allowedValues;
    }
}
