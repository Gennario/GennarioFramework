package cz.gennario.gennarioframework.commands.argument;

import java.util.List;
import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;

/**
 * Boolean argument - accepts true/false, yes/no, 1/0.
 */
public class BooleanArgument extends Argument<Boolean> {

    public BooleanArgument(String name) {
        super(name, Boolean.class);
    }

    @Override
    public Boolean parse(String input) throws ArgumentParseException {
        return switch (input.toLowerCase()) {
            case "true", "yes", "1", "on", "enable", "enabled" -> true;
            case "false", "no", "0", "off", "disable", "disabled" -> false;
            default -> throw new ArgumentParseException(input, "boolean (true/false)");
        };
    }

    @Override
    public String getTypeName() {
        return "boolean";
    }

    @Override
    protected List<String> getDefaultCompletions(TabContext context) {
        return List.of("true", "false");
    }
}
