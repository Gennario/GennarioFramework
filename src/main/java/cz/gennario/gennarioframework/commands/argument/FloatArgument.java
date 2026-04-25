package cz.gennario.gennarioframework.commands.argument;

import lombok.Getter;

/**
 * Float argument with optional min/max bounds.
 */
@Getter
public class FloatArgument extends Argument<Float> {

    private float min = -Float.MAX_VALUE;
    private float max = Float.MAX_VALUE;

    public FloatArgument(String name) {
        super(name, Float.class);
    }

    public FloatArgument min(float min) {
        this.min = min;
        return this;
    }

    public FloatArgument max(float max) {
        this.max = max;
        return this;
    }

    @Override
    public Float parse(String input) throws ArgumentParseException {
        try {
            float value = Float.parseFloat(input);
            if (value < min || value > max) {
                throw new ArgumentParseException(input, "float in range " + min + " - " + max);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(input, "float", e);
        }
    }

    @Override
    public String getTypeName() {
        return "float";
    }
}
