package cz.gennario.gennarioframework.commands;

import cz.gennario.gennarioframework.commands.argument.Argument;
import cz.gennario.gennarioframework.commands.context.CommandContext;
import cz.gennario.gennarioframework.commands.executor.CommandExecutor;
import cz.gennario.gennarioframework.commands.executor.SenderType;
import cz.gennario.gennarioframework.commands.platform.CommandPlatform;
import cz.gennario.gennarioframework.commands.platform.BukkitCommandPlatform;
import cz.gennario.gennarioframework.utils.Utils;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * GennarioCommand - Modern, flexible command framework for Spigot/Paper.
 * Supports unlimited nesting, dynamic tab completions, argument parsing,
 * cooldowns, conditions, middleware, and automatic Brigadier on 1.21.8+.
 *
 * <p>Example usage:</p>
 * <pre>
 * GennarioCommand.create("mycommand", plugin)
 *     .description("My awesome command")
 *     .aliases("mc", "mycmd")
 *     .permission("myplugin.mycommand")
 *     .playerOnly()
 *     .cooldown(3000) // 3 seconds
 *     .condition(ctx -> ctx.sender() instanceof Player)
 *     .executes(ctx -> {
 *         ctx.sender().sendMessage("Hello!");
 *     })
 *     .subCommand("reload")
 *         .permission("myplugin.reload")
 *         .executes(ctx -> ctx.sender().sendMessage("Reloaded!"))
 *         .done()
 *     .subCommand("give")
 *         .argument(Argument.player("target"))
 *         .argument(Argument.integer("amount").min(1).max(64))
 *         .executes(ctx -> {
 *             Player target = ctx.arg("target");
 *             int amount = ctx.arg("amount");
 *         })
 *         .done()
 *     .register();
 * </pre>
 */
@Getter
public class GennarioCommand {

    private final String name;
    private final JavaPlugin plugin;

    private String description = "";
    private final List<String> aliases = new ArrayList<>();
    private final List<String> permissions = new ArrayList<>();
    private SenderType senderType = SenderType.ALL;

    private final List<Argument<?>> arguments = new ArrayList<>();
    private final List<GennarioSubCommand> subCommands = new ArrayList<>();

    private CommandExecutor executor;
    private CommandExecutor noArgsExecutor;
    private BiConsumer<CommandSender, String> noPermissionHandler;
    private BiConsumer<CommandSender, String> wrongSenderHandler;
    private BiConsumer<CommandSender, String> cooldownHandler;
    private BiConsumer<CommandSender, Exception> errorHandler;
    private Predicate<CommandContext> condition;

    private long cooldownMs = 0;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private boolean autoHelp = false;
    private CommandMessages messages = new CommandMessages();

    private CommandPlatform platform;

    // --- Static factory ---

    public static GennarioCommand create(String name, JavaPlugin plugin) {
        return new GennarioCommand(name, plugin);
    }

    private GennarioCommand(String name, JavaPlugin plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    // --- Builder methods ---

    public GennarioCommand description(String description) {
        this.description = description;
        return this;
    }

    public GennarioCommand aliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public GennarioCommand permission(String... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
        return this;
    }

    public GennarioCommand senderType(SenderType senderType) {
        this.senderType = senderType;
        return this;
    }

    public GennarioCommand playerOnly() {
        this.senderType = SenderType.PLAYER;
        return this;
    }

    public GennarioCommand consoleOnly() {
        this.senderType = SenderType.CONSOLE;
        return this;
    }

    public GennarioCommand argument(Argument<?> argument) {
        this.arguments.add(argument);
        return this;
    }

    public GennarioCommand cooldown(long milliseconds) {
        this.cooldownMs = milliseconds;
        return this;
    }

    public GennarioCommand condition(Predicate<CommandContext> condition) {
        this.condition = condition;
        return this;
    }

    public GennarioCommand autoHelp(boolean autoHelp) {
        this.autoHelp = autoHelp;
        return this;
    }

    public GennarioCommand autoHelp() {
        this.autoHelp = true;
        return this;
    }

    public GennarioCommand messages(CommandMessages messages) {
        this.messages = messages;
        return this;
    }

    /**
     * Handler called when the command is executed without any arguments and without subcommands matching.
     */
    public GennarioCommand executesEmpty(CommandExecutor executor) {
        this.noArgsExecutor = executor;
        return this;
    }

    /**
     * Main executor for this command (when arguments match or no subcommand found).
     */
    public GennarioCommand executes(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public GennarioCommand onNoPermission(BiConsumer<CommandSender, String> handler) {
        this.noPermissionHandler = handler;
        return this;
    }

    public GennarioCommand onWrongSender(BiConsumer<CommandSender, String> handler) {
        this.wrongSenderHandler = handler;
        return this;
    }

    public GennarioCommand onCooldown(BiConsumer<CommandSender, String> handler) {
        this.cooldownHandler = handler;
        return this;
    }

    public GennarioCommand onError(BiConsumer<CommandSender, Exception> handler) {
        this.errorHandler = handler;
        return this;
    }

    // --- Sub commands ---

    public GennarioSubCommand subCommand(String name) {
        GennarioSubCommand sub = new GennarioSubCommand(name, this, null);
        this.subCommands.add(sub);
        return sub;
    }

    public GennarioCommand addSubCommand(GennarioSubCommand subCommand) {
        this.subCommands.add(subCommand);
        return this;
    }

    // --- Register ---

    /**
     * Registers the command. Automatically detects if Brigadier should be used (1.21.8+) or legacy Bukkit system.
     */
    public GennarioCommand register() {
        boolean useBrigadier = shouldUseBrigadier();

        if (useBrigadier) {
            try {
                Class<?> clazz = Class.forName("cz.gennario.gennarioframework.commands.platform.BrigadierCommandPlatform");
                this.platform = (CommandPlatform) clazz.getConstructor(GennarioCommand.class).newInstance(this);
            } catch (Exception e) {
                plugin.getLogger().warning("[GennarioCommands] Brigadier init failed, falling back to Bukkit: " + e.getMessage());
                this.platform = new BukkitCommandPlatform(this);
            }
        } else {
            this.platform = new BukkitCommandPlatform(this);
        }
        this.platform.register();
        return this;
    }

    /**
     * Unregisters the command.
     */
    public void unregister() {
        if (this.platform != null) {
            this.platform.unregister();
        }
    }

    // --- Internal helpers ---

    private boolean shouldUseBrigadier() {
        try {
            // 1.21.8+ uses brigadier natively - check for Paper's LifecycleEventManager
            Class.forName("io.papermc.paper.command.brigadier.Commands");
            // Also check version
            return Utils.versionIsAfterOrEqual(21, 8);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if sender has permission for this command.
     */
    public boolean hasPermission(CommandSender sender) {
        if (permissions.isEmpty()) return true;
        for (String perm : permissions) {
            if (sender.hasPermission(perm)) return true;
        }
        return false;
    }

    /**
     * Check cooldown for sender. Returns true if on cooldown.
     */
    public boolean isOnCooldown(CommandSender sender) {
        if (cooldownMs <= 0) return false;
        if (!(sender instanceof org.bukkit.entity.Player player)) return false;
        Long last = cooldowns.get(player.getUniqueId());
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < cooldownMs;
    }

    public void applyCooldown(CommandSender sender) {
        if (cooldownMs <= 0) return;
        if (sender instanceof org.bukkit.entity.Player player) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public long getRemainingCooldown(CommandSender sender) {
        if (!(sender instanceof org.bukkit.entity.Player player)) return 0;
        Long last = cooldowns.get(player.getUniqueId());
        if (last == null) return 0;
        return Math.max(0, cooldownMs - (System.currentTimeMillis() - last));
    }
}
