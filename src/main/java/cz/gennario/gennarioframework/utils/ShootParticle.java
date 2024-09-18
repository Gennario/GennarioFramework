package cz.gennario.gennarioframework.utils;

import com.cryptomorin.xseries.particles.Particles;
import cz.gennario.gennarioframework.Main;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class ShootParticle {

    public static void shootParticle(int pointDistance, int amount, int offset, int size, Location location, boolean random, Particle particle, Particle.DustOptions dustOptions) {
        shootParticle(pointDistance, amount, offset, size, location, random, particle, dustOptions, Main.getInstance().getServer().getOnlinePlayers().toArray(new Player[0]));
    }

    public static void shootParticle(int pointDistance, int amount, int offset, int size, Location location, boolean random, Particle particle, Particle.DustOptions dustOptions, Player... players) {
        Location clone = location.clone();

        location.setPitch(0);
        if (random) location.setYaw(Particles.randInt(0, 360));

        List<Location> locations = BezierCurveUtil.bezierCurveDisplay2(location, location.clone().add(location.getDirection().multiply(pointDistance)), offset, amount);
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                Location remove = locations.remove(0);
                if (remove != null) {
                    if (dustOptions != null) {
                        for (Player player : players) {
                            player.spawnParticle(particle, remove, 1, dustOptions);
                        }
                    } else {
                        for (Player player : players) {
                            player.spawnParticle(particle, remove, 1);
                        }
                    }
                }

                if (i >= pointDistance) {
                    cancel();
                }

                i++;
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

}
