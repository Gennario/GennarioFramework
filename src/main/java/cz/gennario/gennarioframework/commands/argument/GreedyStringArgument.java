package cz.gennario.gennarioframework.commands.argument;

/**
 * Greedy string argument - consumes all remaining arguments as a single string.
 * Must be the last argument in the chain.
 */
public class GreedyStringArgument extends Argument<String> {

    public GreedyStringArgument(String name) {
        super(name, String.class);
    }

    @Override
    public String parse(String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            throw new ArgumentParseException(input, "text");
        }
        return input;
    }

    @Override
    public String getTypeName() {
        return "text...";
    }

    /**
     * Returns true - this argument consumes all remaining args.
     */
    public boolean isGreedy() {
        return true;
    }
}
