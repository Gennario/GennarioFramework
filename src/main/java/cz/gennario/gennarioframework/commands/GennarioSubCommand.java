package cz.gennario.gennarioframework.commands;

import cz.gennario.gennarioframework.commands.argument.Argument;
import cz.gennario.gennarioframework.commands.context.CommandContext;
import cz.gennario.gennarioframework.commands.executor.CommandExecutor;
import cz.gennario.gennarioframework.commands.executor.SenderType;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a sub-command with unlimited nesting capability.
 * Supports its own arguments, permissions, conditions, tab completions, etc.
 */
@Getter
public class GennarioSubCommand {

    private final String name;
    private final GennarioCommand root;
    private final GennarioSubCommand parent;

    private String description = "";
    private final List<String> aliases = new ArrayList<>();
    private final List<String> permissions = new ArrayList<>();
    private SenderType senderType = SenderType.ALL;

    private final List<Argument<?>> arguments = new ArrayList<>();
    private final List<GennarioSubCommand> subCommands = new ArrayList<>();

    private CommandExecutor executor;
    private Predicate<CommandContext> condition;

    private long cooldownMs = 0;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GennarioSubCommand(String name, GennarioCommand root, GennarioSubCommand parent) {
        this.name = name;
        this.root = root;
        this.parent = parent;
    }

    // --- Builder methods ---

    public GennarioSubCommand description(String description) {
        this.description = description;
        return this;
    }

    public GennarioSubCommand aliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public GennarioSubCommand permission(String... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
        return this;
    }

    public GennarioSubCommand senderType(SenderType senderType) {
        this.senderType = senderType;
        return this;
    }

    public GennarioSubCommand playerOnly() {
        this.senderType = SenderType.PLAYER;
        return this;
    }

    public GennarioSubCommand consoleOnly() {
        this.senderType = SenderType.CONSOLE;
        return this;
    }

    public GennarioSubCommand argument(Argument<?> argument) {
        this.arguments.add(argument);
        return this;
    }

    public GennarioSubCommand cooldown(long milliseconds) {
        this.cooldownMs = milliseconds;
        return this;
    }

    public GennarioSubCommand condition(Predicate<CommandContext> condition) {
        this.condition = condition;
        return this;
    }

    public GennarioSubCommand executes(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    // --- Nested sub commands ---

    public GennarioSubCommand subCommand(String name) {
        GennarioSubCommand sub = new GennarioSubCommand(name, this.root, this);
        this.subCommands.add(sub);
        return sub;
    }

    public GennarioSubCommand addSubCommand(GennarioSubCommand subCommand) {
        this.subCommands.add(subCommand);
        return this;
    }

    /**
     * Go back to the parent (either root GennarioCommand or parent GennarioSubCommand).
     * Returns the root command if this is a top-level sub-command.
     */
    public GennarioCommand done() {
        if (parent != null) {
            // We're nested - but since we return GennarioCommand at the end, walk up to root
            return root;
        }
        return root;
    }

    /**
     * Go back to the parent sub-command for deeper nesting.
     */
    public GennarioSubCommand back() {
        return parent;
    }

    // --- Permission check ---

    public boolean hasPermission(CommandSender sender) {
        if (permissions.isEmpty()) return true;
        for (String perm : permissions) {
            if (sender.hasPermission(perm)) return true;
        }
        return false;
    }

    /**
     * Check if name or alias matches.
     */
    public boolean matches(String input) {
        if (name.equalsIgnoreCase(input)) return true;
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(input)) return true;
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

    /**
     * Build full usage string for this sub-command (name + arguments).
     * Used in help entries where the label does NOT include this sub-command's name.
     */
    public String getUsage() {
        StringBuilder sb = new StringBuilder(name);
        String argsUsage = getArgsUsage();
        if (!argsUsage.isEmpty()) {
            sb.append(" ").append(argsUsage);
        }
        return sb.toString();
    }

    /**
     * Build arguments-only usage string (without the sub-command name).
     * Used when the label already contains this sub-command's name (e.g. in usage messages).
     */
    public String getArgsUsage() {
        StringBuilder sb = new StringBuilder();
        for (Argument<?> arg : arguments) {
            if (sb.length() > 0) sb.append(" ");
            if (arg.isRequired()) {
                sb.append("<").append(arg.getName()).append(">");
            } else {
                sb.append("[").append(arg.getName()).append("]");
            }
        }
        return sb.toString();
    }
}
