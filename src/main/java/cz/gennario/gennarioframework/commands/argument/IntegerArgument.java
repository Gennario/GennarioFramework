package cz.gennario.gennarioframework.commands.argument;

import lombok.Getter;

/**
 * Integer argument with optional min/max bounds.
 */
@Getter
public class IntegerArgument extends Argument<Integer> {

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public IntegerArgument(String name) {
        super(name, Integer.class);
    }

    public IntegerArgument min(int min) {
        this.min = min;
        return this;
    }

    public IntegerArgument max(int max) {
        this.max = max;
        return this;
    }

    public IntegerArgument range(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public Integer parse(String input) throws ArgumentParseException {
        try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                String range = (min == Integer.MIN_VALUE ? "∞" : String.valueOf(min))
                        + " - " + (max == Integer.MAX_VALUE ? "∞" : String.valueOf(max));
                throw new ArgumentParseException(input, "integer in range " + range);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(input, "integer", e);
        }
    }

    @Override
    public String getTypeName() {
        if (min != Integer.MIN_VALUE || max != Integer.MAX_VALUE) {
            return "integer(" + (min == Integer.MIN_VALUE ? "∞" : min) + "-" + (max == Integer.MAX_VALUE ? "∞" : max) + ")";
        }
        return "integer";
    }
}
