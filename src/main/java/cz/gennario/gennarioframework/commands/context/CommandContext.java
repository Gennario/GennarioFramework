package cz.gennario.gennarioframework.commands.context;

import cz.gennario.gennarioframework.commands.GennarioCommand;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Context passed to command executors.
 * Provides typed access to parsed arguments, sender info, and metadata.
 */
@Getter
public class CommandContext {

    private final CommandSender sender;
    private final String label;
    private final String[] rawArgs;
    private final GennarioCommand command;
    private final Map<String, Object> parsedArgs;
    private final Map<String, Object> metadata;

    public CommandContext(CommandSender sender, String label, String[] rawArgs,
                         GennarioCommand command, Map<String, Object> parsedArgs) {
        this.sender = sender;
        this.label = label;
        this.rawArgs = rawArgs;
        this.command = command;
        this.parsedArgs = parsedArgs != null ? parsedArgs : new LinkedHashMap<>();
        this.metadata = new LinkedHashMap<>();
    }

    /**
     * Get a parsed argument by name with automatic type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T arg(String name) {
        Object val = parsedArgs.get(name);
        if (val == null) return null;
        return (T) val;
    }

    /**
     * Get a parsed argument with a default value if not present.
     */
    @SuppressWarnings("unchecked")
    public <T> T arg(String name, T defaultValue) {
        Object val = parsedArgs.get(name);
        if (val == null) return defaultValue;
        return (T) val;
    }

    /**
     * Check if an argument was provided.
     */
    public boolean hasArg(String name) {
        return parsedArgs.containsKey(name);
    }

    /**
     * Get argument as string (raw).
     */
    public String argString(String name) {
        Object val = parsedArgs.get(name);
        return val != null ? val.toString() : null;
    }

    /**
     * Get argument as int.
     */
    public int argInt(String name) {
        return arg(name);
    }

    /**
     * Get argument as int with default.
     */
    public int argInt(String name, int defaultValue) {
        Integer val = arg(name);
        return val != null ? val : defaultValue;
    }

    /**
     * Get argument as double.
     */
    public double argDouble(String name) {
        return arg(name);
    }

    public double argDouble(String name, double defaultValue) {
        Double val = arg(name);
        return val != null ? val : defaultValue;
    }

    /**
     * Get argument as boolean.
     */
    public boolean argBool(String name) {
        return arg(name);
    }

    public boolean argBool(String name, boolean defaultValue) {
        Boolean val = arg(name);
        return val != null ? val : defaultValue;
    }

    /**
     * Get sender as Player. Returns null if sender is not a player.
     */
    public Player player() {
        return sender instanceof Player ? (Player) sender : null;
    }

    /**
     * Check if sender is a player.
     */
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    /**
     * Send a message to the sender.
     */
    public void reply(String message) {
        sender.sendMessage(cz.gennario.gennarioframework.utils.Utils.colorize(message));
    }

    /**
     * Store custom metadata in context (useful for middleware/conditions).
     */
    public CommandContext meta(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T meta(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Get all parsed arguments as unmodifiable map.
     */
    public Map<String, Object> allArgs() {
        return Collections.unmodifiableMap(parsedArgs);
    }
}
