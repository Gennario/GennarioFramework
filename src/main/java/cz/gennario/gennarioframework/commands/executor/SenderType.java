package cz.gennario.gennarioframework.commands.executor;

/**
 * Defines who can execute a command.
 */
public enum SenderType {
    /** Anyone can execute (player, console, command block, etc.) */
    ALL,
    /** Only players can execute */
    PLAYER,
    /** Only console can execute */
    CONSOLE
}
