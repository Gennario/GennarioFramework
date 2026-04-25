package cz.gennario.gennarioframework.commands.argument;

/**
 * Long argument with optional min/max bounds.
 */
public class LongArgument extends Argument<Long> {

    private long min = Long.MIN_VALUE;
    private long max = Long.MAX_VALUE;

    public LongArgument(String name) {
        super(name, Long.class);
    }

    public LongArgument min(long min) {
        this.min = min;
        return this;
    }

    public LongArgument max(long max) {
        this.max = max;
        return this;
    }

    @Override
    public Long parse(String input) throws ArgumentParseException {
        try {
            long value = Long.parseLong(input);
            if (value < min || value > max) {
                throw new ArgumentParseException(input, "long in range " + min + " - " + max);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(input, "long number", e);
        }
    }

    @Override
    public String getTypeName() {
        return "long";
    }
}
