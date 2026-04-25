package cz.gennario.gennarioframework.commands.argument;

/**
 * Exception thrown when an argument fails to parse.
 */
public class ArgumentParseException extends Exception {

    private final String input;
    private final String expected;

    public ArgumentParseException(String input, String expected) {
        super("Failed to parse '" + input + "', expected: " + expected);
        this.input = input;
        this.expected = expected;
    }

    public ArgumentParseException(String input, String expected, Throwable cause) {
        super("Failed to parse '" + input + "', expected: " + expected, cause);
        this.input = input;
        this.expected = expected;
    }

    public String getInput() {
        return input;
    }

    public String getExpected() {
        return expected;
    }
}
