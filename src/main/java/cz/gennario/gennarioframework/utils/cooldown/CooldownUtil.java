package cz.gennario.gennarioframework.utils.cooldown;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class CooldownUtil {

    private Map<Player, PlayerCooldownInstance> playerCooldowns;

    public CooldownUtil() {
        playerCooldowns = new HashMap<>();
    }

    public void addCooldown(Player player, String key, int time, TimeUnit timeUnit) {
        if (!playerCooldowns.containsKey(player)) {
            playerCooldowns.put(player, new PlayerCooldownInstance(player));
        }

        playerCooldowns.get(player).addCooldown(key, time, timeUnit);
    }

    public boolean isOnCooldown(Player player, String key) {
        if (!playerCooldowns.containsKey(player)) {
            return false;
        }

        return playerCooldowns.get(player).isOnCooldown(key);
    }

    public long getCooldownTime(Player player, String key, TimeUnit timeUnit) {
        if (!playerCooldowns.containsKey(player)) {
            return 0;
        }

        return playerCooldowns.get(player).getCooldownTime(key, timeUnit);
    }

}
