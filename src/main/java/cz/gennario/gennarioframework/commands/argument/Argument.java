package cz.gennario.gennarioframework.commands.argument;

import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for command arguments with parsing, validation, and tab completion.
 *
 * @param <T> The parsed type of this argument
 */
@Getter
public abstract class Argument<T> {

    private final String name;
    private final Class<T> type;
    private boolean required = true;
    private T defaultValue;
    private String description = "";

    // Tab completion
    private Function<TabContext, List<String>> tabCompleter;
    private List<String> staticTabComplete;

    protected Argument(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Parse the raw string argument into the target type.
     * @return The parsed value
     * @throws ArgumentParseException if parsing fails
     */
    public abstract T parse(String input) throws ArgumentParseException;

    /**
     * Get the type name for error messages (e.g., "integer", "player", etc.)
     */
    public abstract String getTypeName();

    // --- Builder methods ---

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A optional() {
        this.required = false;
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A optional(T defaultValue) {
        this.required = false;
        this.defaultValue = defaultValue;
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A description(String description) {
        this.description = description;
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A tabComplete(Function<TabContext, List<String>> completer) {
        this.tabCompleter = completer;
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A tabComplete(Supplier<List<String>> completer) {
        this.tabCompleter = ctx -> completer.get();
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A tabComplete(String... values) {
        this.staticTabComplete = Arrays.asList(values);
        return (A) this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Argument<T>> A tabComplete(List<String> values) {
        this.staticTabComplete = values;
        return (A) this;
    }

    /**
     * Get tab completions for this argument.
     */
    public List<String> getCompletions(TabContext context) {
        if (tabCompleter != null) {
            return tabCompleter.apply(context);
        }
        if (staticTabComplete != null) {
            return staticTabComplete;
        }
        return getDefaultCompletions(context);
    }

    /**
     * Override this for type-specific default completions (e.g., player names, materials).
     */
    protected List<String> getDefaultCompletions(TabContext context) {
        return List.of("<" + name + ">");
    }

    // === Static factory methods for common argument types ===

    public static StringArgument string(String name) {
        return new StringArgument(name);
    }

    public static GreedyStringArgument greedyString(String name) {
        return new GreedyStringArgument(name);
    }

    public static IntegerArgument integer(String name) {
        return new IntegerArgument(name);
    }

    public static DoubleArgument doubleArg(String name) {
        return new DoubleArgument(name);
    }

    public static FloatArgument floatArg(String name) {
        return new FloatArgument(name);
    }

    public static LongArgument longArg(String name) {
        return new LongArgument(name);
    }

    public static BooleanArgument bool(String name) {
        return new BooleanArgument(name);
    }

    public static PlayerArgument player(String name) {
        return new PlayerArgument(name);
    }

    public static OfflinePlayerArgument offlinePlayer(String name) {
        return new OfflinePlayerArgument(name);
    }

    public static MaterialArgument material(String name) {
        return new MaterialArgument(name);
    }

    public static EntityTypeArgument entityType(String name) {
        return new EntityTypeArgument(name);
    }

    public static WorldArgument world(String name) {
        return new WorldArgument(name);
    }

    public static EnumArgument<?> enumArg(String name, Class<? extends Enum<?>> enumClass) {
        return new EnumArgument<>(name, enumClass);
    }

    public static LiteralArgument literal(String name, String... values) {
        return new LiteralArgument(name, values);
    }

    /**
     * Create a custom argument with your own parser.
     */
    public static <T> CustomArgument<T> custom(String name, Class<T> type, Function<String, T> parser) {
        return new CustomArgument<>(name, type, parser);
    }
}
