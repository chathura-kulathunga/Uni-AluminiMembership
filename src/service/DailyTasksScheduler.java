package service;

import java.util.concurrent.*;

/**
 *
 * @author Fox C
 */
public class DailyTasksScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Running daily payment tasks...");
            PaymentStatusUpdater.runDailyUpdates();
        }, 0, 1, TimeUnit.DAYS); // run immediately, then every day
    }

    // Optional: stop method if you ever need to stop it
    public static void stop() {
        scheduler.shutdownNow();
    }
}