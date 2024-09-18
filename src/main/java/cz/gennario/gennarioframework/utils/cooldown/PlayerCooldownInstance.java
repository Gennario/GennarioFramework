package cz.gennario.gennarioframework.utils.cooldown;

import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerCooldownInstance {

    private Player player;
    private Map<String, Date> cooldowns;

    public PlayerCooldownInstance(Player player) {
        this.player = player;
        cooldowns = new HashMap<>();
    }

    public void addCooldown(String key, int time, TimeUnit timeUnit) {
        Date date = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));
        cooldowns.put(key, date);
    }

    public boolean isOnCooldown(String key) {
        if (!cooldowns.containsKey(key)) {
            return false;
        }

        Date date = cooldowns.get(key);
        boolean after = date.after(new Date());
        if(!after) {
            cooldowns.remove(key);
        }
        return after;
    }

    public long getCooldownTime(String key, TimeUnit timeUnit) {
        if (!cooldowns.containsKey(key)) {
            return 0;
        }

        Date date = cooldowns.get(key);
        return timeUnit.convert(date.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

}
