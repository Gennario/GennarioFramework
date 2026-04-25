package cz.gennario.gennarioframework.utils.commands;

import org.bukkit.command.CommandSender;

/**
 * @deprecated Use {@link cz.gennario.gennarioframework.commands.executor.CommandExecutor} instead.
 */
@Deprecated
public interface CommandResponse {

    void cmd(CommandSender sender, String label, CommandArg[] commandArgs);

}
