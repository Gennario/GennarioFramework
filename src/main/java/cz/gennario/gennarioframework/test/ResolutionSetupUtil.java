package cz.gennario.gennarioframework.test;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public class ResolutionSetupUtil implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, PlayerScreenSettings> playerSettings = new HashMap<>();
    private static final Map<UUID, ResolutionSetupSession> activeSessions = new HashMap<>();

    public static void register(JavaPlugin main) {
        plugin = main;
        Bukkit.getPluginManager().registerEvents(new ResolutionSetupUtil(), plugin);
    }

    // ===== Inner classes =====
    public static class PlayerScreenSettings {
        public final double width, height, distance;
        public PlayerScreenSettings(double w, double h, double d) {
            width = w; height = h; distance = d;
        }
        public String toString() {
            return String.format("%.1fx%.1f at distance %.1f", width, height, distance);
        }
    }

    public static class ResolutionSetupSession {
        public BlockDisplay setupDisplay;
        public SetupPhase phase = SetupPhase.WIDTH_ADJUSTMENT;
        public double currentWidth = 8.0, currentHeight = 6.0;
        public final double baseDistance = 4.0;
        public Location baseLocation;
        public enum SetupPhase { WIDTH_ADJUSTMENT, HEIGHT_ADJUSTMENT, COMPLETED }
        public ResolutionSetupSession(Player player) {
            this.baseLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(baseDistance));
        }
    }

    public static class ScreenBounds {
        public Vector topLeft, topRight, bottomLeft, bottomRight, center, normal;
        public ScreenBounds(Vector tl, Vector tr, Vector bl, Vector br) {
            topLeft = tl; topRight = tr; bottomLeft = bl; bottomRight = br;
            center = tl.clone().add(br).multiply(0.5);
            normal = tr.clone().subtract(tl).crossProduct(bl.clone().subtract(tl)).normalize();
        }
    }

    // ===== Public API =====

    public static boolean hasPlayerSettings(Player player) {
        return playerSettings.containsKey(player.getUniqueId());
    }

    public static PlayerScreenSettings getPlayerSettings(Player player) {
        return playerSettings.get(player.getUniqueId());
    }

    public static boolean isInSetup(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public static void resetPlayerSetup(Player player) {
        UUID id = player.getUniqueId();
        ResolutionSetupSession session = activeSessions.remove(id);
        if (session != null && session.setupDisplay != null && session.setupDisplay.isValid()) {
            session.setupDisplay.remove();
        }
        playerSettings.remove(id);
        player.sendMessage("§7Screen settings reset. Starting new setup...");
        new BukkitRunnable() {
            public void run() {
                if (player.isOnline()) startResolutionSetup(player);
            }
        }.runTaskLater(plugin, 10L);
    }

    public static void startResolutionSetup(Player player) {
        UUID id = player.getUniqueId();
        player.sendMessage("§6=== Screen Resolution Setup ===");
        player.sendMessage("§eUse §fscroll wheel §eto adjust width");
        player.sendMessage("§eWhen satisfied, §fleft-click §eto confirm and move to height");

        ResolutionSetupSession session = new ResolutionSetupSession(player);
        activeSessions.put(id, session);
        createSetupDisplay(player, session);

        new BukkitRunnable() {
            public void run() {
                if (!activeSessions.containsKey(id) || !player.isOnline()) cancel();
                else updateDisplayPosition(player, session);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ===== Interní pomocné metody =====

    private static void createSetupDisplay(Player player, ResolutionSetupSession session) {
        World world = player.getWorld();
        Location displayLoc = calculateDisplayCenter(player, session);
        BlockDisplay display = world.spawn(displayLoc, BlockDisplay.class);
        display.setBlock(Material.WHITE_CONCRETE.createBlockData());
        display.setGlowing(true);
        updateDisplayTransformation(display, session);
        session.setupDisplay = display;
    }

    private static void updateDisplayTransformation(BlockDisplay display, ResolutionSetupSession session) {
        Transformation t = new Transformation(
                new Vector3f((float)-session.currentWidth / 2, (float)-session.currentHeight / 2, 0),
                new AxisAngle4f(),
                new Vector3f((float)session.currentWidth, (float)session.currentHeight, 0.1f),
                new AxisAngle4f()
        );
        display.setTransformation(t);
    }

    private static Location calculateDisplayCenter(Player player, ResolutionSetupSession session) {
        return player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(session.baseDistance));
    }

    private static void updateDisplayPosition(Player player, ResolutionSetupSession session) {
        if (session.setupDisplay != null && session.setupDisplay.isValid()) {
            session.setupDisplay.teleport(calculateDisplayCenter(player, session));
        }
    }

    private static void handleScroll(Player player, ResolutionSetupSession session, boolean scrollUp) {
        double step = 0.2;
        if (session.phase == ResolutionSetupSession.SetupPhase.WIDTH_ADJUSTMENT) {
            session.currentWidth = scrollUp
                    ? Math.min(20.0, session.currentWidth + step)
                    : Math.max(1.0, session.currentWidth - step);
            player.sendMessage("§7Width: §f" + String.format("%.1f", session.currentWidth));
        } else if (session.phase == ResolutionSetupSession.SetupPhase.HEIGHT_ADJUSTMENT) {
            session.currentHeight = scrollUp
                    ? Math.min(15.0, session.currentHeight + step)
                    : Math.max(1.0, session.currentHeight - step);
            player.sendMessage("§7Height: §f" + String.format("%.1f", session.currentHeight));
        }
        updateDisplayTransformation(session.setupDisplay, session);
    }

    private static void handleLeftClick(Player player, ResolutionSetupSession session) {
        if (session.phase == ResolutionSetupSession.SetupPhase.WIDTH_ADJUSTMENT) {
            session.phase = ResolutionSetupSession.SetupPhase.HEIGHT_ADJUSTMENT;
            player.sendMessage("§a✓ Width confirmed: §f" + String.format("%.1f", session.currentWidth));
            player.sendMessage("§eUse scroll to set height, left-click to finish.");
            session.setupDisplay.setBlock(Material.LIME_CONCRETE.createBlockData());
        } else {
            finishSetup(player, session);
        }
    }

    private static void handleRightClick(Player player, ResolutionSetupSession session) {
        if (session.phase == ResolutionSetupSession.SetupPhase.WIDTH_ADJUSTMENT) {
            session.currentWidth = 8.0;
            player.sendMessage("§7Width reset to default");
        } else {
            session.currentHeight = 6.0;
            player.sendMessage("§7Height reset to default");
        }
        updateDisplayTransformation(session.setupDisplay, session);
    }

    private static void finishSetup(Player player, ResolutionSetupSession session) {
        UUID id = player.getUniqueId();
        PlayerScreenSettings settings = new PlayerScreenSettings(session.currentWidth, session.currentHeight, session.baseDistance);
        playerSettings.put(id, settings);
        if (session.setupDisplay != null && session.setupDisplay.isValid()) {
            session.setupDisplay.remove();
        }
        activeSessions.remove(id);
        session.phase = ResolutionSetupSession.SetupPhase.COMPLETED;
        player.sendMessage("§a✓ Setup completed!");
        player.sendMessage("§aYour screen settings: §f" + settings.toString());
        initializePlayerScreen(player, settings);
    }

    private static void initializePlayerScreen(Player player, PlayerScreenSettings settings) {
        ScreenBounds bounds = calculateScreenBounds(player, settings);
        player.sendMessage("§6Screen initialized!");
        createDemoCursor(player, bounds);
    }

    private static ScreenBounds calculateScreenBounds(Player player, PlayerScreenSettings settings) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();
        Vector up = new Vector(0, 1, 0);
        Vector right = dir.clone().crossProduct(up).normalize();
        up = right.clone().crossProduct(dir).normalize();

        Vector center = loc.toVector().add(dir.multiply(settings.distance));
        Vector halfW = right.multiply(settings.width / 2);
        Vector halfH = up.multiply(settings.height / 2);

        return new ScreenBounds(
                center.clone().subtract(halfW).add(halfH),
                center.clone().add(halfW).add(halfH),
                center.clone().subtract(halfW).subtract(halfH),
                center.clone().add(halfW).subtract(halfH)
        );
    }

    private static void createDemoCursor(Player player, ScreenBounds bounds) {
        BlockDisplay cursor = player.getWorld().spawn(bounds.center.toLocation(player.getWorld()), BlockDisplay.class);
        cursor.setBlock(Material.GLOWSTONE.createBlockData());
        cursor.setGlowing(true);
        cursor.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(),
                new Vector3f(0.1f, 0.1f, 0.1f),
                new AxisAngle4f()
        ));
        new BukkitRunnable() {
            public void run() {
                if (cursor.isValid()) cursor.remove();
            }
        }.runTaskLater(plugin, 100L);
    }

    // ===== Eventy =====

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        new BukkitRunnable() {
            public void run() {
                if (p.isOnline() && !hasPlayerSettings(p)) {
                    startResolutionSetup(p);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        if (!activeSessions.containsKey(id)) return;
        e.setCancelled(true);

        ResolutionSetupSession s = activeSessions.get(id);
        int prev = e.getPreviousSlot(), now = e.getNewSlot();
        boolean scrollUp = (now == 0 && prev == 8) || (now > prev && !(now == 8 && prev == 0));
        handleScroll(p, s, scrollUp);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID id = p.getUniqueId();
        if (!activeSessions.containsKey(id)) return;
        Action action = e.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
            handleLeftClick(p, activeSessions.get(id));
        else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
            handleRightClick(p, activeSessions.get(id));

        e.setCancelled(true);
    }
}