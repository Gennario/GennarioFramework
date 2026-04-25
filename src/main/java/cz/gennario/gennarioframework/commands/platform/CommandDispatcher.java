package cz.gennario.gennarioframework.commands.platform;

import cz.gennario.gennarioframework.commands.CommandMessages;
import cz.gennario.gennarioframework.commands.GennarioCommand;
import cz.gennario.gennarioframework.commands.GennarioSubCommand;
import cz.gennario.gennarioframework.commands.argument.Argument;
import cz.gennario.gennarioframework.commands.argument.ArgumentParseException;
import cz.gennario.gennarioframework.commands.argument.GreedyStringArgument;
import cz.gennario.gennarioframework.commands.context.CommandContext;
import cz.gennario.gennarioframework.commands.executor.CommandExecutor;
import cz.gennario.gennarioframework.commands.executor.SenderType;
import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shared command dispatch logic used by both Bukkit and Brigadier platforms.
 * Handles argument parsing, sub-command routing, permission checks, cooldowns, etc.
 */
public class CommandDispatcher {

    private final GennarioCommand command;

    public CommandDispatcher(GennarioCommand command) {
        this.command = command;
    }

    /**
     * Execute the command with the given sender, label, and args.
     * Returns true if the command was handled.
     */
    public boolean dispatch(CommandSender sender, String label, String[] args) {
        CommandMessages messages = command.getMessages();

        // --- Root permission check ---
        if (!command.hasPermission(sender)) {
            handleNoPermission(sender, label, messages);
            return true;
        }

        // --- Sender type check ---
        if (!checkSenderType(sender, command.getSenderType(), label, messages)) {
            return true;
        }

        // --- No args ---
        if (args.length == 0) {
            return handleNoArgs(sender, label, messages);
        }

        // --- Try to match a sub-command ---
        String firstArg = args[0];

        // Auto-help
        if (command.isAutoHelp() && (firstArg.equalsIgnoreCase("help") || firstArg.equalsIgnoreCase("?"))) {
            sendHelp(sender, label);
            return true;
        }

        // Search sub-commands
        for (GennarioSubCommand sub : command.getSubCommands()) {
            if (sub.matches(firstArg)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return dispatchSubCommand(sender, label, sub, subArgs, messages);
            }
        }

        // --- Try root command with arguments ---
        if (command.getExecutor() != null && !command.getArguments().isEmpty()) {
            return executeWithArgs(sender, label, args, command.getArguments(), command.getExecutor(),
                    command.getCondition(), command, messages);
        }

        // --- Root executor without args definition (pass raw args) ---
        if (command.getExecutor() != null) {
            return executeRoot(sender, label, args, messages);
        }

        // --- Unknown sub-command ---
        if (command.isAutoHelp()) {
            sender.sendMessage(messages.format(messages.getUnknownSubCommand(), label,
                    "%input%", firstArg));
            return true;
        }

        return false;
    }

    private boolean dispatchSubCommand(CommandSender sender, String label, GennarioSubCommand sub, String[] args, CommandMessages messages) {
        String fullLabel = label + " " + sub.getName();

        // Permission check
        if (!sub.hasPermission(sender)) {
            handleNoPermission(sender, fullLabel, messages);
            return true;
        }

        // Sender type check
        if (!checkSenderType(sender, sub.getSenderType(), fullLabel, messages)) {
            return true;
        }

        // Cooldown check
        if (sub.isOnCooldown(sender)) {
            long remaining = sub.getRemainingCooldown(sender) / 1000;
            sender.sendMessage(messages.format(messages.getOnCooldown(), fullLabel,
                    "%time%", String.valueOf(remaining)));
            return true;
        }

        // --- No further args ---
        if (args.length == 0) {
            // Has executor with no required args? Execute it.
            if (sub.getExecutor() != null && sub.getArguments().stream().noneMatch(Argument::isRequired)) {
                return executeWithArgs(sender, fullLabel, args, sub.getArguments(), sub.getExecutor(),
                        sub.getCondition(), command, messages);
            }
            // Has nested sub-commands? Show sub-help.
            if (!sub.getSubCommands().isEmpty()) {
                sendSubHelp(sender, fullLabel, sub);
                return true;
            }
            // Has executor (with required args)? Show usage.
            if (sub.getExecutor() != null) {
                sender.sendMessage(messages.format(messages.getUsage(), fullLabel,
                        "%usage%", sub.getArgsUsage()));
                return true;
            }
            // Nothing to do
            sender.sendMessage(messages.format(messages.getUsage(), fullLabel, "%usage%", sub.getArgsUsage()));
            return true;
        }

        // --- Try to match a nested sub-command first ---
        if (!sub.getSubCommands().isEmpty()) {
            for (GennarioSubCommand nested : sub.getSubCommands()) {
                if (nested.matches(args[0])) {
                    String[] nestedArgs = Arrays.copyOfRange(args, 1, args.length);
                    return dispatchSubCommand(sender, fullLabel, nested, nestedArgs, messages);
                }
            }

            // Nested subs exist but none matched - if sub has NO executor, it's an unknown sub-command
            if (sub.getExecutor() == null && sub.getArguments().isEmpty()) {
                sender.sendMessage(messages.format(messages.getUnknownSubCommand(), fullLabel,
                        "%input%", args[0]));
                return true;
            }
        }

        // --- Try executing with arguments ---
        if (sub.getExecutor() != null) {
            return executeWithArgs(sender, fullLabel, args, sub.getArguments(), sub.getExecutor(),
                    sub.getCondition(), command, messages);
        }

        // --- Fallback: unknown sub-command or no executor ---
        if (!sub.getSubCommands().isEmpty()) {
            sender.sendMessage(messages.format(messages.getUnknownSubCommand(), fullLabel,
                    "%input%", args[0]));
        } else {
            sender.sendMessage(messages.format(messages.getUsage(), fullLabel,
                    "%usage%", sub.getArgsUsage()));
        }
        return true;
    }

    /**
     * Send help for a sub-command's nested sub-commands.
     */
    private void sendSubHelp(CommandSender sender, String label, GennarioSubCommand sub) {
        CommandMessages messages = command.getMessages();
        sender.sendMessage(messages.format(messages.getHelpHeader(), label));

        for (GennarioSubCommand nested : sub.getSubCommands()) {
            if (nested.hasPermission(sender)) {
                String usage = nested.getUsage();
                String desc = nested.getDescription().isEmpty() ? "No description" : nested.getDescription();
                sender.sendMessage(messages.format(messages.getHelpEntry(), label,
                        "%usage%", usage, "%description%", desc));
            }
        }

        sender.sendMessage(messages.format(messages.getHelpFooter(), label));
    }

    private boolean executeWithArgs(CommandSender sender, String label, String[] args,
                                    List<Argument<?>> argDefs, CommandExecutor executor,
                                    java.util.function.Predicate<CommandContext> condition,
                                    GennarioCommand root, CommandMessages messages) {
        Map<String, Object> parsedArgs = new LinkedHashMap<>();

        int argIndex = 0;
        for (int i = 0; i < argDefs.size(); i++) {
            Argument<?> argDef = argDefs.get(i);

            if (argIndex >= args.length) {
                if (argDef.isRequired()) {
                    // Missing required argument
                    String usage = buildUsage(argDefs);
                    sender.sendMessage(messages.format(messages.getUsage(), label,
                            "%usage%", usage));
                    return true;
                } else {
                    // Optional - use default
                    parsedArgs.put(argDef.getName(), argDef.getDefaultValue());
                    continue;
                }
            }

            // Greedy string - consume all remaining
            if (argDef instanceof GreedyStringArgument) {
                String joined = String.join(" ", Arrays.copyOfRange(args, argIndex, args.length));
                try {
                    parsedArgs.put(argDef.getName(), argDef.parse(joined));
                } catch (ArgumentParseException e) {
                    sender.sendMessage(messages.format(messages.getWrongArgument(), label,
                            "%value%", joined, "%name%", argDef.getName(), "%type%", argDef.getTypeName()));
                    return true;
                }
                argIndex = args.length;
                continue;
            }

            try {
                Object parsed = argDef.parse(args[argIndex]);
                parsedArgs.put(argDef.getName(), parsed);
                argIndex++;
            } catch (ArgumentParseException e) {
                sender.sendMessage(messages.format(messages.getWrongArgument(), label,
                        "%value%", args[argIndex], "%name%", argDef.getName(), "%type%", argDef.getTypeName()));
                return true;
            }
        }

        CommandContext context = new CommandContext(sender, label, args, root, parsedArgs);

        // Condition check
        if (condition != null && !condition.test(context)) {
            sender.sendMessage(messages.format(messages.getConditionFailed(), label));
            return true;
        }

        // Cooldown check on root
        if (root.isOnCooldown(sender)) {
            long remaining = root.getRemainingCooldown(sender) / 1000;
            sender.sendMessage(messages.format(messages.getOnCooldown(), label,
                    "%time%", String.valueOf(remaining)));
            return true;
        }

        try {
            executor.execute(context);
            root.applyCooldown(sender);
        } catch (Exception e) {
            if (root.getErrorHandler() != null) {
                root.getErrorHandler().accept(sender, e);
            } else {
                sender.sendMessage(messages.format(messages.getErrorOccurred(), label));
                e.printStackTrace();
            }
        }

        return true;
    }

    private boolean executeRoot(CommandSender sender, String label, String[] args, CommandMessages messages) {
        Map<String, Object> rawArgs = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            rawArgs.put("arg" + i, args[i]);
        }
        CommandContext context = new CommandContext(sender, label, args, command, rawArgs);

        if (command.getCondition() != null && !command.getCondition().test(context)) {
            sender.sendMessage(command.getMessages().format(messages.getConditionFailed(), label));
            return true;
        }

        if (command.isOnCooldown(sender)) {
            long remaining = command.getRemainingCooldown(sender) / 1000;
            sender.sendMessage(messages.format(messages.getOnCooldown(), label,
                    "%time%", String.valueOf(remaining)));
            return true;
        }

        try {
            command.getExecutor().execute(context);
            command.applyCooldown(sender);
        } catch (Exception e) {
            if (command.getErrorHandler() != null) {
                command.getErrorHandler().accept(sender, e);
            } else {
                sender.sendMessage(messages.format(messages.getErrorOccurred(), label));
                e.printStackTrace();
            }
        }

        return true;
    }

    private boolean handleNoArgs(CommandSender sender, String label, CommandMessages messages) {
        if (command.getNoArgsExecutor() != null) {
            CommandContext ctx = new CommandContext(sender, label, new String[0], command, new LinkedHashMap<>());
            try {
                command.getNoArgsExecutor().execute(ctx);
            } catch (Exception e) {
                if (command.getErrorHandler() != null) {
                    command.getErrorHandler().accept(sender, e);
                } else {
                    sender.sendMessage(messages.format(messages.getErrorOccurred(), label));
                    e.printStackTrace();
                }
            }
            return true;
        }

        if (command.getExecutor() != null && command.getArguments().isEmpty()) {
            CommandContext ctx = new CommandContext(sender, label, new String[0], command, new LinkedHashMap<>());
            try {
                command.getExecutor().execute(ctx);
                command.applyCooldown(sender);
            } catch (Exception e) {
                if (command.getErrorHandler() != null) {
                    command.getErrorHandler().accept(sender, e);
                } else {
                    sender.sendMessage(messages.format(messages.getErrorOccurred(), label));
                    e.printStackTrace();
                }
            }
            return true;
        }

        if (command.isAutoHelp()) {
            sendHelp(sender, label);
            return true;
        }

        // Show usage
        String usage = buildRootUsage();
        sender.sendMessage(messages.format(messages.getUsage(), label, "%usage%", usage));
        return true;
    }

    // ========================
    // TAB COMPLETION
    // ========================

    /**
     * Handle tab completion.
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0) return Collections.emptyList();

        // First argument - show sub-commands + first argument completions
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            // Sub-commands
            for (GennarioSubCommand sub : command.getSubCommands()) {
                if (sub.hasPermission(sender)) {
                    completions.add(sub.getName());
                }
            }

            // Auto-help
            if (command.isAutoHelp()) {
                completions.add("help");
            }

            // Root arguments (if any)
            if (!command.getArguments().isEmpty()) {
                Argument<?> firstArg = command.getArguments().get(0);
                TabContext ctx = new TabContext(sender, args, 0, new LinkedHashMap<>());
                completions.addAll(firstArg.getCompletions(ctx));
            }

            return filterCompletions(completions, args[0]);
        }

        // Check if first arg matches a sub-command
        String firstArg = args[0];
        for (GennarioSubCommand sub : command.getSubCommands()) {
            if (sub.matches(firstArg) && sub.hasPermission(sender)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return tabCompleteSubCommand(sender, sub, subArgs);
            }
        }

        // Root argument completions
        if (!command.getArguments().isEmpty()) {
            int argIndex = args.length - 1;
            return getArgCompletions(sender, args, command.getArguments(), argIndex, 0);
        }

        return Collections.emptyList();
    }

    private List<String> tabCompleteSubCommand(CommandSender sender, GennarioSubCommand sub, String[] args) {
        if (args.length == 0) return Collections.emptyList();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            // Nested sub-commands
            for (GennarioSubCommand nested : sub.getSubCommands()) {
                if (nested.hasPermission(sender)) {
                    completions.add(nested.getName());
                }
            }

            // Arguments
            if (!sub.getArguments().isEmpty()) {
                Argument<?> firstArg = sub.getArguments().get(0);
                TabContext ctx = new TabContext(sender, args, 0, new LinkedHashMap<>());
                completions.addAll(firstArg.getCompletions(ctx));
            }

            return filterCompletions(completions, args[0]);
        }

        // Check nested sub-commands
        for (GennarioSubCommand nested : sub.getSubCommands()) {
            if (nested.matches(args[0]) && nested.hasPermission(sender)) {
                String[] nestedArgs = Arrays.copyOfRange(args, 1, args.length);
                return tabCompleteSubCommand(sender, nested, nestedArgs);
            }
        }

        // Argument completions
        if (!sub.getArguments().isEmpty()) {
            int argIndex = args.length - 1;
            return getArgCompletions(sender, args, sub.getArguments(), argIndex, 0);
        }

        return Collections.emptyList();
    }

    private List<String> getArgCompletions(CommandSender sender, String[] args, List<Argument<?>> argDefs, int targetIndex, int startOffset) {
        if (targetIndex >= argDefs.size()) return Collections.emptyList();

        // Build previously parsed args for context
        Map<String, Object> previousArgs = new LinkedHashMap<>();
        for (int i = 0; i < targetIndex && i < argDefs.size(); i++) {
            int argArrayIdx = startOffset + i;
            if (argArrayIdx < args.length) {
                try {
                    Object parsed = argDefs.get(i).parse(args[argArrayIdx]);
                    previousArgs.put(argDefs.get(i).getName(), parsed);
                } catch (ArgumentParseException ignored) {
                    previousArgs.put(argDefs.get(i).getName(), args[argArrayIdx]);
                }
            }
        }

        Argument<?> targetArg = argDefs.get(targetIndex);
        TabContext ctx = new TabContext(sender, args, startOffset + targetIndex, previousArgs);
        List<String> completions = targetArg.getCompletions(ctx);

        String current = (startOffset + targetIndex < args.length) ? args[startOffset + targetIndex] : "";
        return filterCompletions(completions, current);
    }

    // ========================
    // HELP
    // ========================

    private void sendHelp(CommandSender sender, String label) {
        CommandMessages messages = command.getMessages();
        sender.sendMessage(messages.format(messages.getHelpHeader(), label));

        for (GennarioSubCommand sub : command.getSubCommands()) {
            if (sub.hasPermission(sender)) {
                String usage = sub.getUsage();
                String desc = sub.getDescription().isEmpty() ? "No description" : sub.getDescription();
                sender.sendMessage(messages.format(messages.getHelpEntry(), label,
                        "%usage%", usage, "%description%", desc));
            }
        }

        sender.sendMessage(messages.format(messages.getHelpFooter(), label));
    }

    // ========================
    // UTILITIES
    // ========================

    private boolean checkSenderType(CommandSender sender, SenderType type, String label, CommandMessages messages) {
        return switch (type) {
            case PLAYER -> {
                if (!(sender instanceof Player)) {
                    if (command.getWrongSenderHandler() != null) {
                        command.getWrongSenderHandler().accept(sender, label);
                    } else {
                        sender.sendMessage(messages.format(messages.getPlayerOnly(), label));
                    }
                    yield false;
                }
                yield true;
            }
            case CONSOLE -> {
                if (!(sender instanceof ConsoleCommandSender)) {
                    if (command.getWrongSenderHandler() != null) {
                        command.getWrongSenderHandler().accept(sender, label);
                    } else {
                        sender.sendMessage(messages.format(messages.getConsoleOnly(), label));
                    }
                    yield false;
                }
                yield true;
            }
            case ALL -> true;
        };
    }

    private void handleNoPermission(CommandSender sender, String label, CommandMessages messages) {
        if (command.getNoPermissionHandler() != null) {
            command.getNoPermissionHandler().accept(sender, label);
        } else {
            sender.sendMessage(messages.format(messages.getNoPermission(), label));
        }
    }

    private String buildUsage(List<Argument<?>> argDefs) {
        StringBuilder sb = new StringBuilder();
        for (Argument<?> arg : argDefs) {
            if (sb.length() > 0) sb.append(" ");
            if (arg.isRequired()) {
                sb.append("<").append(arg.getName()).append(">");
            } else {
                sb.append("[").append(arg.getName()).append("]");
            }
        }
        return sb.toString();
    }

    private String buildRootUsage() {
        StringBuilder sb = new StringBuilder();
        if (!command.getSubCommands().isEmpty()) {
            sb.append("<");
            List<String> names = command.getSubCommands().stream()
                    .map(GennarioSubCommand::getName)
                    .collect(Collectors.toList());
            sb.append(String.join("|", names));
            sb.append(">");
        }
        if (!command.getArguments().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(buildUsage(command.getArguments()));
        }
        return sb.toString();
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input == null || input.isEmpty()) return completions;
        String lower = input.toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
