package cz.gennario.gennarioframework.commands.executor;

import cz.gennario.gennarioframework.commands.context.CommandContext;

/**
 * Functional interface for command execution.
 */
@FunctionalInterface
public interface CommandExecutor {

    void execute(CommandContext context);

}
