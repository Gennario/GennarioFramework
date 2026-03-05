package cz.gennario.gennarioframework.mysql;

import cz.gennario.gennarioframework.Main;
import cz.gennario.gennarioframework.utils.FoliaScheduler;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public abstract class SQLAutoSaveTask {

    protected SQLTable table;
    private boolean running;

    public SQLAutoSaveTask(SQLTable table, long delay, TimeUnit timeUnit) {
        this.table = table;
        this.running = true;
        long ticks = timeUnit.toSeconds(delay) * 20;
        FoliaScheduler.runAsyncTimer(Main.getInstance(), () -> {
            if (running) {
                onAutoSaveRun();
            }
        }, ticks, ticks);
    }

    public abstract void onAutoSaveRun();

}
