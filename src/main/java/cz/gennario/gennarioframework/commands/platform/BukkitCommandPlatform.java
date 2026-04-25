package cz.gennario.gennarioframework.commands.platform;

import cz.gennario.gennarioframework.commands.GennarioCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Legacy Bukkit command platform.
 * Uses PluginCommand + CommandMap to register commands.
 * Used on servers before 1.21.8 (non-Brigadier).
 * Supports hot-reload via PlugMan (full register/unregister at runtime).
 */
public class BukkitCommandPlatform implements CommandPlatform {

    private final GennarioCommand command;
    private final CommandDispatcher dispatcher;

    public BukkitCommandPlatform(GennarioCommand command) {
        this.command = command;
        this.dispatcher = new CommandDispatcher(command);
    }

    @Override
    public void register() {
        try {
            JavaPlugin plugin = command.getPlugin();

            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command.getName(), plugin);

            if (command.getDescription() != null && !command.getDescription().isEmpty()) {
                pluginCommand.setDescription(command.getDescription());
            }
            pluginCommand.setAliases(command.getAliases());

            // --- Command executor ---
            pluginCommand.setExecutor((sender, cmd, label, args) ->
                    dispatcher.dispatch(sender, label, args));

            // --- Tab completer ---
            pluginCommand.setTabCompleter((sender, cmd, label, args) ->
                    dispatcher.tabComplete(sender, args));

            // Register in CommandMap
            CommandMap commandMap = getCommandMap();
            commandMap.register(plugin.getPluginMeta().getName(), pluginCommand);

            // Update command tree for all online players
            updateCommandTreeForPlayers();

        } catch (Exception e) {
            throw new RuntimeException("Failed to register command '" + command.getName() + "' via Bukkit platform", e);
        }
    }

    @Override
    public void unregister() {
        try {
            CommandMap commandMap = getCommandMap();

            // Unregister from CommandMap
            org.bukkit.command.Command cmd = commandMap.getCommand(command.getName());
            if (cmd != null) {
                cmd.unregister(commandMap);
            }

            // Remove from the knownCommands map (main name + aliases + prefixed versions)
            String prefix = command.getPlugin().getPluginMeta().getName().toLowerCase();
            removeFromKnownCommands(commandMap, command.getName());
            removeFromKnownCommands(commandMap, prefix + ":" + command.getName());
            for (String alias : command.getAliases()) {
                removeFromKnownCommands(commandMap, alias);
                removeFromKnownCommands(commandMap, prefix + ":" + alias);
            }

            // Update command tree for all online players
            updateCommandTreeForPlayers();

        } catch (Exception e) {
            throw new RuntimeException("Failed to unregister command '" + command.getName() + "'", e);
        }
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
        // Try Paper's Bukkit.getCommandMap() first (available on newer Paper)
        try {
            Method method = Bukkit.class.getMethod("getCommandMap");
            return (CommandMap) method.invoke(null);
        } catch (NoSuchMethodException ignored) {}

        // Fallback: reflection on server's plugin manager
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
}
