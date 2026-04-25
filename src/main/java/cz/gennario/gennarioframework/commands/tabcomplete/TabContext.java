package cz.gennario.gennarioframework.commands.tabcomplete;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Context provided to tab completion handlers.
 * Gives access to the sender, current input, and previously resolved arguments.
 */
@Getter
public class TabContext {

    private final CommandSender sender;
    private final String[] args;
    private final int currentIndex;
    private final String currentInput;
    private final Map<String, Object> previousArgs;

    public TabContext(CommandSender sender, String[] args, int currentIndex, Map<String, Object> previousArgs) {
        this.sender = sender;
        this.args = args;
        this.currentIndex = currentIndex;
        this.currentInput = (currentIndex < args.length) ? args[currentIndex] : "";
        this.previousArgs = previousArgs;
    }

    /**
     * Get sender as Player, or null if console.
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
     * Get a previously parsed argument value.
     */
    @SuppressWarnings("unchecked")
    public <T> T previousArg(String name) {
        return (T) previousArgs.get(name);
    }

    /**
     * Check if a previous argument exists.
     */
    public boolean hasPreviousArg(String name) {
        return previousArgs.containsKey(name);
    }
}
