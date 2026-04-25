package cz.gennario.gennarioframework.commands.argument;

import lombok.Getter;

/**
 * Double argument with optional min/max bounds.
 */
@Getter
public class DoubleArgument extends Argument<Double> {

    private double min = -Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;

    public DoubleArgument(String name) {
        super(name, Double.class);
    }

    public DoubleArgument min(double min) {
        this.min = min;
        return this;
    }

    public DoubleArgument max(double max) {
        this.max = max;
        return this;
    }

    public DoubleArgument range(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public Double parse(String input) throws ArgumentParseException {
        try {
            double value = Double.parseDouble(input);
            if (value < min || value > max) {
                throw new ArgumentParseException(input, "decimal number in range " + min + " - " + max);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(input, "decimal number", e);
        }
    }

    @Override
    public String getTypeName() {
        return "decimal";
    }
}
