package cz.gennario.gennarioframework.mysql;

import cz.gennario.gennarioframework.Main;
import lombok.Data;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

@Data
public abstract class SQLAutoSaveTask {

    protected SQLTable table;
    private boolean running;

    public SQLAutoSaveTask(SQLTable table, long delay, TimeUnit timeUnit) {
        this.table = table;
        this.running = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (running) {
                    onAutoSaveRun();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), timeUnit.toSeconds(delay) * 20, timeUnit.toSeconds(delay) * 20);
    }

    public abstract void onAutoSaveRun();

}
