package cz.gennario.gennarioframework.commands.platform;

import cz.gennario.gennarioframework.commands.GennarioCommand;
import cz.gennario.gennarioframework.commands.GennarioSubCommand;
import cz.gennario.gennarioframework.commands.argument.*;
import cz.gennario.gennarioframework.commands.tabcomplete.TabContext;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Brigadier command platform for Paper 1.21.8+.
 * Registers commands directly into the Minecraft Brigadier dispatcher at runtime,
 * allowing hot-reload via PlugMan or similar plugin managers.
 * <p>
 * All execution is delegated to {@link CommandDispatcher} which handles argument parsing,
 * permissions, cooldowns, conditions, etc. Brigadier is used only for the command tree
 * structure and tab completions.
 * </p>
 * Falls back to legacy Bukkit if Brigadier registration fails.
 */
@SuppressWarnings("UnstableApiUsage")
public class BrigadierCommandPlatform implements CommandPlatform {

    private final GennarioCommand command;
    private final CommandDispatcher dispatcher;
    private boolean registered = false;

    public BrigadierCommandPlatform(GennarioCommand command) {
        this.command = command;
        this.dispatcher = new CommandDispatcher(command);
    }

    @Override
    public void register() {
        try {
            // Build the Brigadier command tree
            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(command.getName());

            // Root permission
            if (!command.getPermissions().isEmpty()) {
                builder.requires(source -> command.hasPermission(source.getSender()));
            }

            // Root executor (no args) - delegate to dispatcher
            builder.executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                dispatcher.dispatch(sender, command.getName(), new String[0]);
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            });

            // Build sub-command tree
            for (GennarioSubCommand sub : command.getSubCommands()) {
                builder.then(buildSubCommand(sub, new ArrayList<>()));
            }

            // Build root arguments tree
            if (!command.getArguments().isEmpty()) {
                builder.then(buildArgumentChain(command.getArguments(), 0, new ArrayList<>()));
            }

            // Register directly into the Brigadier dispatcher (runtime, PlugMan-safe)
            com.mojang.brigadier.CommandDispatcher<CommandSourceStack> brigadierDispatcher = getBrigadierDispatcher();
            RootCommandNode<CommandSourceStack> root = brigadierDispatcher.getRoot();

            // Register main command
            root.addChild(builder.build());

            // Register aliases as redirects to the main command
            CommandNode<CommandSourceStack> mainNode = root.getChild(command.getName());
            if (mainNode != null) {
                for (String alias : command.getAliases()) {
                    LiteralArgumentBuilder<CommandSourceStack> aliasBuilder = Commands.literal(alias)
                            .redirect(mainNode);
                    if (!command.getPermissions().isEmpty()) {
                        aliasBuilder.requires(source -> command.hasPermission(source.getSender()));
                    }
                    root.addChild(aliasBuilder.build());
                }
            }

            // Also register into the Bukkit CommandMap so the server knows about it
            registerBukkitFallback();

            registered = true;

            // Update command tree for all online players (so tab-complete works immediately)
            updateCommandTreeForPlayers();

        } catch (Exception e) {
            // Fallback to Bukkit platform if Brigadier fails
            command.getPlugin().getLogger().warning("[GennarioCommands] Brigadier runtime registration failed for '" + command.getName() + "', falling back to Bukkit. Error: " + e.getMessage());
            new BukkitCommandPlatform(command).register();
        }
    }

    @Override
    public void unregister() {
        if (!registered) return;
        try {
            com.mojang.brigadier.CommandDispatcher<CommandSourceStack> brigadierDispatcher = getBrigadierDispatcher();
            RootCommandNode<CommandSourceStack> root = brigadierDispatcher.getRoot();

            // Remove main command and aliases from the Brigadier root node
            removeChild(root, command.getName());
            for (String alias : command.getAliases()) {
                removeChild(root, alias);
            }

            // Remove from Bukkit CommandMap
            unregisterBukkitFallback();

            registered = false;

            // Update command tree for all online players
            updateCommandTreeForPlayers();

        } catch (Exception e) {
            command.getPlugin().getLogger().warning("[GennarioCommands] Failed to unregister Brigadier command '" + command.getName() + "': " + e.getMessage());
        }
    }

    // ==========================================
    // RUNTIME BRIGADIER ACCESS (Reflection)
    // ==========================================

    /**
     * Get the Minecraft Brigadier CommandDispatcher via CraftServer reflection.
     * Works on Paper 1.21.8+ where CommandSourceStack is available.
     */
    @SuppressWarnings("unchecked")
    private com.mojang.brigadier.CommandDispatcher<CommandSourceStack> getBrigadierDispatcher() throws Exception {
        Object craftServer = Bukkit.getServer();

        // CraftServer -> MinecraftServer (getServer())
        Method getServerMethod = craftServer.getClass().getMethod("getServer");
        Object minecraftServer = getServerMethod.invoke(craftServer);

        // MinecraftServer -> Commands (vanillaCommandDispatcher or getCommands())
        Object commandsObj;
        try {
            Method getCommands = minecraftServer.getClass().getMethod("getCommands");
            commandsObj = getCommands.invoke(minecraftServer);
        } catch (NoSuchMethodException e) {
            // Fallback: try field access
            Field field = minecraftServer.getClass().getDeclaredField("vanillaCommandDispatcher");
            field.setAccessible(true);
            commandsObj = field.get(minecraftServer);
        }

        // Commands -> CommandDispatcher (getDispatcher())
        Method getDispatcher = commandsObj.getClass().getMethod("getDispatcher");
        return (com.mojang.brigadier.CommandDispatcher<CommandSourceStack>) getDispatcher.invoke(commandsObj);
    }

    /**
     * Remove a child node from RootCommandNode by name using reflection.
     * RootCommandNode doesn't have a removeChild method, so we modify the internal map.
     */
    @SuppressWarnings("unchecked")
    private void removeChild(RootCommandNode<?> root, String name) {
        try {
            // CommandNode has a 'children' map, 'literals' map, and 'arguments' map
            for (String fieldName : new String[]{"children", "literals", "arguments"}) {
                try {
                    Field field = CommandNode.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Map<String, ?> map = (Map<String, ?>) field.get(root);
                    map.remove(name);
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Exception e) {
            command.getPlugin().getLogger().warning("[GennarioCommands] Failed to remove command node '" + name + "': " + e.getMessage());
        }
    }

    /**
     * Register a simple Bukkit command entry so the server recognizes the command name.
     * This is needed because some server internals check the Bukkit CommandMap even for Brigadier commands.
     */
    private void registerBukkitFallback() {
        try {
            JavaPlugin plugin = command.getPlugin();
            CommandMap commandMap = getCommandMap();

            // Create a simple BukkitCommand that delegates to our dispatcher
            org.bukkit.command.defaults.BukkitCommand bukkitCmd = new org.bukkit.command.defaults.BukkitCommand(
                    command.getName(),
                    command.getDescription() != null ? command.getDescription() : "",
                    "/" + command.getName(),
                    command.getAliases()
            ) {
                @Override
                public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                    return dispatcher.dispatch(sender, label, args);
                }

                @Override
                public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                    return dispatcher.tabComplete(sender, args);
                }
            };

            commandMap.register(plugin.getPluginMeta().getName(), bukkitCmd);
        } catch (Exception e) {
            // Non-critical - Brigadier registration is the primary mechanism
            command.getPlugin().getLogger().fine("[GennarioCommands] Bukkit fallback registration skipped: " + e.getMessage());
        }
    }

    /**
     * Unregister from Bukkit CommandMap.
     */
    private void unregisterBukkitFallback() {
        try {
            CommandMap commandMap = getCommandMap();

            // Unregister main name
            org.bukkit.command.Command cmd = commandMap.getCommand(command.getName());
            if (cmd != null) {
                cmd.unregister(commandMap);
            }

            // Also try to remove from the known commands map
            removeFromKnownCommands(commandMap, command.getName());
            String prefix = command.getPlugin().getPluginMeta().getName().toLowerCase();
            removeFromKnownCommands(commandMap, prefix + ":" + command.getName());
            for (String alias : command.getAliases()) {
                removeFromKnownCommands(commandMap, alias);
                removeFromKnownCommands(commandMap, prefix + ":" + alias);
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private void removeFromKnownCommands(CommandMap commandMap, String name) {
        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);
            knownCommands.remove(name.toLowerCase());
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("deprecation")
    private CommandMap getCommandMap() throws Exception {
        try {
            Method method = Bukkit.class.getMethod("getCommandMap");
            return (CommandMap) method.invoke(null);
        } catch (NoSuchMethodException ignored) {}

        Object pluginManager = Bukkit.getServer().getPluginManager();
        Field field = pluginManager.getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(pluginManager);
    }

    /**
     * Send updated command tree to all online players.
     * This ensures tab-completions are immediately visible after register/unregister.
     */
    private void updateCommandTreeForPlayers() {
        Bukkit.getScheduler().runTask(command.getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        });
    }

    // ==========================================
    // BRIGADIER TREE BUILDING
    // ==========================================

    /**
     * Build a Brigadier literal node for a sub-command.
     * All execution is delegated to dispatcher with the full path of sub-command names as args.
     *
     * @param sub       the sub-command
     * @param parentPath the path of parent sub-command names (e.g. ["settings"] for /world settings)
     */
    private LiteralArgumentBuilder<CommandSourceStack> buildSubCommand(GennarioSubCommand sub, List<String> parentPath) {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal(sub.getName());

        // Permission
        if (!sub.getPermissions().isEmpty()) {
            literal.requires(source -> sub.hasPermission(source.getSender()));
        }

        // Build the full arg path: parentPath + this sub name
        List<String> fullPath = new ArrayList<>(parentPath);
        fullPath.add(sub.getName());

        // Executor (no further args) - dispatch with full path
        literal.executes(ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            dispatcher.dispatch(sender, command.getName(), fullPath.toArray(new String[0]));
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        });

        // Nested sub-commands
        for (GennarioSubCommand nested : sub.getSubCommands()) {
            literal.then(buildSubCommand(nested, fullPath));
        }

        // Arguments for this sub-command
        if (!sub.getArguments().isEmpty()) {
            literal.then(buildArgumentChain(sub.getArguments(), 0, fullPath));
        }

        return literal;
    }

    /**
     * Build a chain of Brigadier argument nodes.
     * When executed, collects all raw string values and dispatches through CommandDispatcher.
     *
     * @param args      the argument definitions
     * @param index     current index in the args list
     * @param prefixArgs prefix args (sub-command names) to prepend before argument values
     */
    private RequiredArgumentBuilder<CommandSourceStack, ?> buildArgumentChain(
            List<Argument<?>> args, int index, List<String> prefixArgs) {

        Argument<?> arg = args.get(index);
        ArgumentType<?> brigadierType = toBrigadierType(arg);

        RequiredArgumentBuilder<CommandSourceStack, ?> builder =
                Commands.argument(arg.getName(), brigadierType);

        // Suggestions - use our argument's completions
        builder.suggests((ctx, suggestionsBuilder) -> {
            String remaining = suggestionsBuilder.getRemaining().toLowerCase();
            CommandSender sender = ctx.getSource().getSender();

            // Build previous args map for context-aware tab complete
            Map<String, Object> previousArgs = new LinkedHashMap<>();
            for (int i = 0; i < index; i++) {
                try {
                    Object val = ctx.getArgument(args.get(i).getName(), Object.class);
                    previousArgs.put(args.get(i).getName(), String.valueOf(val));
                } catch (Exception ignored) {}
            }

            TabContext tabCtx = new TabContext(sender, new String[0], index, previousArgs);
            List<String> completions = arg.getCompletions(tabCtx);

            for (String completion : completions) {
                if (completion.toLowerCase().startsWith(remaining)) {
                    suggestionsBuilder.suggest(completion);
                }
            }

            return suggestionsBuilder.buildFuture();
        });

        if (index + 1 < args.size()) {
            // Chain next argument
            builder.then(buildArgumentChain(args, index + 1, prefixArgs));

            // If next args are optional, also allow execution here (partial args)
            if (!args.get(index + 1).isRequired()) {
                builder.executes(ctx -> {
                    String[] rawArgs = collectRawArgs(ctx, args, index, prefixArgs);
                    CommandSender sender = ctx.getSource().getSender();
                    dispatcher.dispatch(sender, command.getName(), rawArgs);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });
            }
        } else {
            // Last argument - execute
            builder.executes(ctx -> {
                String[] rawArgs = collectRawArgs(ctx, args, index, prefixArgs);
                CommandSender sender = ctx.getSource().getSender();
                dispatcher.dispatch(sender, command.getName(), rawArgs);
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            });
        }

        return builder;
    }

    /**
     * Collect raw string arguments from Brigadier context and prepend sub-command path prefix.
     * This produces the same String[] args that Bukkit would give us, so the dispatcher can handle it uniformly.
     */
    private String[] collectRawArgs(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
                                     List<Argument<?>> args, int lastIndex, List<String> prefixArgs) {
        List<String> result = new ArrayList<>(prefixArgs);
        for (int i = 0; i <= lastIndex; i++) {
            try {
                Object val = ctx.getArgument(args.get(i).getName(), Object.class);
                result.add(String.valueOf(val));
            } catch (Exception ignored) {}
        }
        return result.toArray(new String[0]);
    }

    /**
     * Convert our Argument type to a Brigadier ArgumentType.
     * Numeric and boolean types use native Brigadier types for proper validation.
     * All other types use StringArgumentType with custom suggestions.
     */
    private ArgumentType<?> toBrigadierType(Argument<?> arg) {
        if (arg instanceof IntegerArgument intArg) {
            return IntegerArgumentType.integer(intArg.getMin(), intArg.getMax());
        } else if (arg instanceof DoubleArgument dblArg) {
            return DoubleArgumentType.doubleArg(dblArg.getMin(), dblArg.getMax());
        } else if (arg instanceof FloatArgument fltArg) {
            return FloatArgumentType.floatArg(fltArg.getMin(), fltArg.getMax());
        } else if (arg instanceof LongArgument) {
            return LongArgumentType.longArg();
        } else if (arg instanceof BooleanArgument) {
            // Use string so our dispatcher parses it (supports yes/no/on/off, not just true/false)
            return StringArgumentType.word();
        } else if (arg instanceof GreedyStringArgument) {
            return StringArgumentType.greedyString();
        } else {
            // For all other types (player, material, enum, custom, etc.)
            // use string + custom suggestions - our dispatcher will parse them
            return StringArgumentType.word();
        }
    }
}
