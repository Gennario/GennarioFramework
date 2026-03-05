package cz.gennario.gennarioframework.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Scheduler abstraction layer that supports both Folia and Spigot/Paper.
 * Automatically detects whether the server is running Folia and uses the appropriate scheduler API.
 *
 * <p>On Folia, tasks are scheduled via the regionized scheduler APIs:
 * <ul>
 *   <li>{@code Bukkit.getGlobalRegionScheduler()} for global tasks</li>
 *   <li>{@code Bukkit.getAsyncScheduler()} for async tasks</li>
 *   <li>{@code Entity.getScheduler()} for entity-bound tasks</li>
 *   <li>{@code Bukkit.getRegionScheduler()} for location-bound tasks</li>
 * </ul>
 *
 * <p>On Spigot/Paper, tasks are scheduled via the classic {@code BukkitScheduler}.
 */
public final class FoliaScheduler {

    private static final boolean IS_FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    private FoliaScheduler() {
    }

    /**
     * @return true if the server is running Folia
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    // =====================================================================
    // GLOBAL / SYNC TASKS
    // =====================================================================

    /**
     * Run a task on the global region (Folia) or main thread (Spigot/Paper).
     */
    public static WrappedTask runSync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            return new WrappedTask(Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run()));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    /**
     * Run a task on the global region (Folia) or main thread (Spigot/Paper) after a delay.
     *
     * @param delayTicks delay in ticks
     */
    public static WrappedTask runSyncLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            // Folia's runDelayed requires delay >= 1
            long delay = Math.max(1, delayTicks);
            return new WrappedTask(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
        }
    }

    /**
     * Run a repeating task on the global region (Folia) or main thread (Spigot/Paper).
     *
     * @param delayTicks  initial delay in ticks
     * @param periodTicks period in ticks
     */
    public static WrappedTask runSyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            long delay = Math.max(1, delayTicks);
            return new WrappedTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, periodTicks));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    // =====================================================================
    // ASYNC TASKS
    // =====================================================================

    /**
     * Run an async task.
     */
    public static WrappedTask runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            return new WrappedTask(Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run()));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
        }
    }

    /**
     * Run an async task after a delay.
     *
     * @param delayTicks delay in ticks (converted to milliseconds for Folia: ticks * 50)
     */
    public static WrappedTask runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            return new WrappedTask(Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks * 50L, TimeUnit.MILLISECONDS));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
        }
    }

    /**
     * Run a repeating async task.
     *
     * @param delayTicks  initial delay in ticks (converted to ms for Folia)
     * @param periodTicks period in ticks (converted to ms for Folia)
     */
    public static WrappedTask runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            return new WrappedTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), Math.max(delayTicks * 50L, 1), periodTicks * 50L, TimeUnit.MILLISECONDS));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks));
        }
    }

    // =====================================================================
    // ENTITY-BOUND TASKS (for player.openInventory, player.teleport, etc.)
    // =====================================================================

    /**
     * Run a task tied to an entity's region (Folia) or on the main thread (Spigot/Paper).
     * Use this for operations like player.openInventory(), player.teleport(), etc.
     *
     * @param entity   the entity to bind the task to
     * @param task     the task to run
     * @param retired  runnable to execute if the entity is removed before the task runs (can be null)
     */
    public static WrappedTask runForEntity(Plugin plugin, Entity entity, Runnable task, Runnable retired) {
        if (IS_FOLIA) {
            return new WrappedTask(entity.getScheduler().run(plugin, scheduledTask -> task.run(), retired));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    /**
     * Run a task tied to an entity's region after a delay.
     *
     * @param entity     the entity
     * @param task       the task
     * @param retired    runnable if entity is removed
     * @param delayTicks delay in ticks
     */
    public static WrappedTask runForEntityLater(Plugin plugin, Entity entity, Runnable task, Runnable retired, long delayTicks) {
        if (IS_FOLIA) {
            long delay = Math.max(1, delayTicks);
            return new WrappedTask(entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), retired, delay));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
        }
    }

    /**
     * Run a repeating task tied to an entity's region.
     *
     * @param entity     the entity
     * @param task       the task
     * @param retired    runnable if entity is removed
     * @param delayTicks initial delay
     * @param periodTicks period
     */
    public static WrappedTask runForEntityTimer(Plugin plugin, Entity entity, Runnable task, Runnable retired, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            long delay = Math.max(1, delayTicks);
            return new WrappedTask(entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), retired, delay, periodTicks));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    // =====================================================================
    // LOCATION-BOUND TASKS
    // =====================================================================

    /**
     * Run a task on the region that owns the given location (Folia) or main thread (Spigot/Paper).
     */
    public static WrappedTask runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            return new WrappedTask(Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run()));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    /**
     * Run a task on the region that owns the given location after a delay.
     */
    public static WrappedTask runAtLocationLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            long delay = Math.max(1, delayTicks);
            return new WrappedTask(Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
        }
    }

    /**
     * Run a repeating task on the region that owns the given location (Folia) or main thread (Spigot/Paper).
     * Use this for tasks that need to access world data (blocks, entities at a location).
     *
     * @param plugin      the plugin
     * @param location    the location whose region thread to run on
     * @param task        the task
     * @param delayTicks  initial delay in ticks
     * @param periodTicks period in ticks
     */
    public static WrappedTask runAtLocationTimer(Plugin plugin, Location location, Runnable task, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            long delay = Math.max(1, delayTicks);
            return new WrappedTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> task.run(), delay, periodTicks));
        } else {
            return new WrappedTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
        }
    }

    // =====================================================================
    // WRAPPED TASK (for cancellation support)
    // =====================================================================

    /**
     * A wrapper around both Folia's ScheduledTask and Bukkit's BukkitTask,
     * providing a unified cancel() method.
     */
    public static class WrappedTask {
        private Object handle;

        public WrappedTask(Object handle) {
            this.handle = handle;
        }

        /**
         * Cancel this scheduled task.
         */
        public void cancel() {
            if (handle == null) return;

            if (IS_FOLIA) {
                if (handle instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
                    foliaTask.cancel();
                }
            } else {
                if (handle instanceof org.bukkit.scheduler.BukkitTask bukkitTask) {
                    bukkitTask.cancel();
                }
            }
        }

        /**
         * @return true if this task has been cancelled
         */
        public boolean isCancelled() {
            if (handle == null) return true;

            if (IS_FOLIA) {
                if (handle instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
                    return foliaTask.isCancelled();
                }
            } else {
                if (handle instanceof org.bukkit.scheduler.BukkitTask bukkitTask) {
                    return bukkitTask.isCancelled();
                }
            }
            return true;
        }

        /**
         * @return the underlying task handle
         */
        public Object getHandle() {
            return handle;
        }
    }
}
